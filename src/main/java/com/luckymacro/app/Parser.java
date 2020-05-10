package com.luckymacro.app;

public class Parser {
    public static String parse( String vmcode ) {
        State state;
        state = new State();
        String[] lines = vmcode.split("\\n");
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < lines.length; i += 1) {
            String line = lines[i];
            if (lineToKeep(line)) {
                sb.append("// " + line + "\n");
                sb.append(dispatch(state, line));
            }
        }
        return sb.toString();
    }

    public static class State {
        private int cmdCount;
        private State () {
            cmdCount = 0;
        }

        public int getCmdCount() {
            return cmdCount;
        }

        public int incCmdCount() {
            return cmdCount += 1;
        }
    }

    private static String cmdToString (ACmds as) {
        return new CmdBuilder().load(as).finish();
    }

    private static String cmdToString (State state, ACmds as) {
        return new CmdBuilder(state).load(as).finish();
    }

    private static ASym sym(String s) {
        return new ASym(s);
    }
    private static ACmd a(String s) {
        return new ACmd(s);
    }

    private static final ACmd sp = a("@SP");
    private static final ACmd local = a("@LCL");
    private static final ACmd arg = a("@ARG");
    private static final ACmd aThis = a("@THIS");
    private static final ACmd aThat = a("@THAT");
    private static final ACmd temp = a("@5");

    private static final ACmds incSp = new ACmds(sp, a("M=M+1"));
    private static final ACmds decSp = new ACmds(sp, a("M=M-1"));
    private static final ACmds derefSp = new ACmds(sp, a("A=M"));
    private static final ACmds readSpToD = new ACmds(a("A=M"), a("D=M"));
    private static final ACmd deleteStackEntry = a("M=0");
    private static final ACmds pushD = new ACmds(derefSp).add(a("M=D")).add(incSp);
    private static final ACmds readSpToMAndAddToD = new ACmds(a("A=M"), a("M=M+D"));
    private static final ACmds popD = new ACmds(decSp).add(readSpToD).add(deleteStackEntry);

    private static ACmds pushConstant(String index) {
        return new ACmds(a(String.format("@%1$s", index)), a("D=A")).add(pushD);
    }

    private static ACmds pushVirtualSegment(ACmd base, String index) {
        return new
            ACmds(a("@" + index))
            .add(a("D=A"))
            .add(base)
            .add(a("A=M+D"))
            .add(a("D=M"))
            .add(pushD);
    }

    private static String absoluteTempIndex(String index) {
        int idx = Integer.parseInt(index);
        if (idx > 7) {
            System.out.println("ERROR: TEMP index overflow");
        }
        int TEMP = 5;
        return Integer.toString(TEMP + idx);
    }

    private static ACmds pushTempSegment(String index) {
        ACmd atTempIdx = a("@" + absoluteTempIndex(index));
        return new
            ACmds(atTempIdx)
            .add(a("D=M"))
            .add(pushD);
    }

    private static String push(String cmd) {
        String[] words = cmd.split("\\s");
        String segment = words[1];
        String index = words[2];
        ACmds result = new ACmds(a("//"));
        switch (segment) {
            case "constant": result = pushConstant(index); break;
            case "local": result = pushVirtualSegment(local, index); break;
            case "argument": result = pushVirtualSegment(arg, index); break;
            case "this": result = pushVirtualSegment(aThis, index); break;
            case "that": result = pushVirtualSegment(aThat, index); break;
            case "temp": result = pushTempSegment(index); break;
        }
        return cmdToString(result);
    }

    private static ACmds storeSegmentPointer(ACmd atSegment, String index) {
        return new
            ACmds(a("@" + index))
            .add(a("D=A"))
            .add(atSegment)
            .add(a("D=D+M"))
            .add(a("@R13"))
            .add(a("M=D"));
    }

    private static ACmds popVirtualSegment(ACmd atSegment, String index) {
        ACmds freeIndex = new ACmds(a("@R13"), a("M=0"));
        ACmds readStoredPointer = new ACmds(a("@R13"), a("A=M"));
        return new
            ACmds(storeSegmentPointer(atSegment, index))
            .add(popD)
            .add(readStoredPointer)
            .add(a("M=D"))
            .add(freeIndex);
    }

    private static ACmds popTempSegment(String index) {
        int idx = Integer.parseInt(index);
        if (idx > 7) {
            System.out.println("ERROR: TEMP index overflow");
        }
        int TEMP = 5;
        String absoluteIndex = Integer.toString(TEMP + idx);
        ACmd atTempIdx = a("@" + absoluteIndex);
        return new ACmds(popD).add(atTempIdx).add(a("M=D"));
    }

    private static String pop(String cmd) {
        String[] words = cmd.split("\\s");
        String segment = words[1];
        String index = words[2];
        ACmds result = new ACmds(a("//"));
        switch (segment) {
            case "local": result = popVirtualSegment(local, index); break;
            case "argument": result = popVirtualSegment(arg, index); break;
            case "this": result = popVirtualSegment(aThis, index); break;
            case "that": result = popVirtualSegment(aThat, index); break;
            case "temp": result = popTempSegment(index); break;
        }
        return cmdToString(result);
    }

    private static String add() {
        return cmdToString(
                new
                ACmds(decSp)
                .add(readSpToD)
                .add(deleteStackEntry)
                .add(decSp)
                .add(readSpToMAndAddToD)
                .add(incSp)
                );
    }

    private static ASym register(String sym) {
        return sym("(" + sym + "%1$s)");
    }

    private static ASym atSym(String sym) {
        return sym("@" + sym + "%1$s");
    }

    private static String eq(State state) {
        String trueBranchSym = "VMeqSetTrueSym";
        String falseBranchSym = "VMeqSetFalseSym";
        String endOfEqSym = "VMeqEndOfFunctionSym";
        ACmds readSpContentsAndSubFromD = new ACmds(a("A=M"), a("D=M-D"));
        ASym registerSetTrue = register(trueBranchSym);
        ASym registerSetFalse = register(falseBranchSym);
        ASym registerEnd = register(endOfEqSym);
        ACmd setDToTrue = a("D=-1");
        ACmd setDToFalse = a("D=0");
        ACmds gotoEnd = new ACmds(atSym(endOfEqSym), a("0;JMP"));
        ACmds gotoFalseBranch = new ACmds(atSym(falseBranchSym), a("0;JMP"));
        ACmds gotoTrueBranchIfZero =  new ACmds(atSym(trueBranchSym), a("D;JEQ"));
        return cmdToString(state,
                new
                ACmds(decSp)
                .add(readSpToD)
                .add(deleteStackEntry)
                .add(decSp)
                .add(readSpContentsAndSubFromD)
                .add(gotoTrueBranchIfZero)
                .add(gotoFalseBranch)
                .add(registerSetFalse)
                .add(setDToFalse)
                .add(gotoEnd)
                .add(registerSetTrue)
                .add(setDToTrue)
                .add(gotoEnd)
                .add(registerEnd)
                .add(pushD)
                );
    }


    private static String lt(State state) {
        // a is less than b if a - b < 0

        String trueSym = "LTTRUEBRANCH";
        String falseSym = "LTFALSEBRANCH";
        String endSym = "LTEND";
        ACmds popDSubDFromM = new ACmds(decSp).add(derefSp).add(a("D=M-D"));
        ACmds gotoTrueBranchIfLessThanZero =
            new ACmds(atSym(trueSym), a("D;JLT"));
        ACmds gotoFalseBranch =
            new ACmds(atSym(falseSym), a("0;JMP"));
        ACmds falseBranch = new
            ACmds(register(falseSym))
            .add(a("D=0"))
            .add(atSym(endSym))
            .add(a("0;JMP"));
        ACmds trueBranch = new
            ACmds(register(trueSym))
            .add(a("D=-1"))
            .add(atSym(endSym))
            .add(a("0;JMP"));
        ASym registerEnd = register(endSym);
        ACmds ifDLessThanZeroThenTrueElseFalse =
            new
            ACmds(gotoTrueBranchIfLessThanZero)
            .add(gotoFalseBranch)
            .add(falseBranch)
            .add(trueBranch)
            .add(registerEnd)
            .add(pushD);
        ACmds asm =
            new
            ACmds(popD)
            .add(popDSubDFromM)
            .add(ifDLessThanZeroThenTrueElseFalse);
        return new CmdBuilder(state).load(asm).finish();
    }

    private static String gt(State state) {
        // a is greater than b if a - b > 0
        // TODO abstract IF

        String trueSym = "IFTRUEBRANCH";
        String falseSym = "IFFALSEBRANCH";
        String endSym = "IFEND";
        ACmds popSubDFromM = new ACmds(decSp).add(derefSp).add(a("D=M-D"));
        ACmds gotoEnd = new ACmds(atSym(endSym), a("0;JMP"));
        ACmds gotoTrueBranchIfGreaterThanZero = new ACmds(atSym(trueSym), a("D;JGT"));
        ACmds gotoFalseBranch = new ACmds(atSym(falseSym), a("0;JMP"));
        ACmds trueBranch = new ACmds(register(trueSym)).add(a("D=-1")).add(gotoEnd);
        ACmds falseBranch = new ACmds(register(falseSym)).add(a("D=0")).add(gotoEnd);
        ACmds ifDGreaterThanZeroThenTrueElseFalse = new
            ACmds(gotoTrueBranchIfGreaterThanZero)
            .add(gotoFalseBranch)
            .add(falseBranch)
            .add(trueBranch)
            .add(register(endSym))
            .add(pushD);

        return cmdToString(state,
            new
            ACmds(popD)
            .add(popSubDFromM)
            .add(ifDGreaterThanZeroThenTrueElseFalse)
            );
    }

    private static String sub() {
        ACmds readSpToMAndSubD = new ACmds(a("A=M"), a("M=M-D"));
        return cmdToString(new
                ACmds(decSp)
                .add(readSpToD)
                .add(deleteStackEntry)
                .add(decSp)
                .add(readSpToMAndSubD)
                .add(incSp)
                );
    }

    private static String neg() {
        return cmdToString(new
                ACmds(popD)
                .add(a("D=-D"))
                .add(pushD)
                );
    }

    private static String and() {
        return cmdToString(new
                ACmds(popD)
                .add(decSp)
                .add(derefSp)
                .add(a("D=M&D"))
                .add(pushD)
                );
    }

    private static String or() {
        return cmdToString(new
                ACmds(popD)
                .add(decSp)
                .add(derefSp)
                .add(a("D=M|D"))
                .add(pushD)
                );
    }

    private static String not() {
        return cmdToString(new
                ACmds(popD)
                .add(a("D=!D"))
                .add(pushD)
                );
    }

    private static String dispatch(State state, String cmd) {
        String[] words = cmd.split("\\s");
        String command = words[0];
        String result = "";
        switch (command) {
            case "push": result = push(cmd); break;
            case "pop": result = pop(cmd); break;
            case "eq": result = eq(state); break;
            case "lt": result = lt(state); break;
            case "gt": result = gt(state); break;
            case "add": result = add(); break;
            case "sub": result = sub(); break;
            case "neg": result = neg(); break;
            case "and": result = and(); break;
            case "or": result = or(); break;
            case "not": result = not(); break;
        }
        return result;
    }

    public static boolean lineToKeep(String line) {
        boolean isCommentLine = line.matches("^\\r*\\/\\/[^\n]*");
        boolean isEmptyLine = line.matches("^[\\s\\r]*$");
        return !(isCommentLine || isEmptyLine);
    }
}
