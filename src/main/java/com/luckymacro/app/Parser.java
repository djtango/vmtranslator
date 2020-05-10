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

    private static final ACmds incSp = new ACmds(a("@SP"), a("M=M+1"));
    private static final ACmds decSp = new ACmds(a("@SP"), a("M=M-1"));
    private static final ACmds derefSp = new ACmds(a("@SP"), a("A=M"));
    private static final ACmds readSpToD = new ACmds(a("A=M"), a("D=M"));
    private static final ACmd deleteStackEntry = a("M=0");
    private static final ACmds pushD = new ACmds(derefSp).add(a("M=D")).add(incSp);
    private static final ACmds readSpToMAndAddToD = new ACmds(a("A=M"), a("M=M+D"));
    private static final ACmds popD = new ACmds(decSp).add(readSpToD).add(deleteStackEntry).add(decSp);

    private static ACmds readVariableToD(String memSegment, String segAddr) {
        return new ACmds(a(String.format("@%1$s", segAddr)), a("D=A"));
    }

    private static String push(String cmd) {
        String[] words = cmd.split("\\s");
        String memSegment = words[1];
        String segAddr = words[2];
        return cmdToString(
                new
                ACmds(readVariableToD(memSegment, segAddr)).add(pushD));
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
        ACmds readSpToMAndSubDToD = new ACmds(a("A=M"), a("D=M-D"));
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
            .add(readSpToMAndSubDToD)
            .add(ifDLessThanZeroThenTrueElseFalse);
        return new CmdBuilder(state).load(asm).finish();
    }

    private static String gt(State state) {
        // a is greater than b if a - b > 0

        String trueSym = "IFTRUEBRANCH";
        String falseSym = "IFFALSEBRANCH";
        String endSym = "IFEND";
        ACmds popSubDFromM = new ACmds(derefSp, a("D=M-D"));
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
        return "";
    }

    private static String and() {
        return "";
    }

    private static String or() {
        return "";
    }

    private static String not() {
        return "";
    }

    private static String dispatch(State state, String cmd) {
        String[] words = cmd.split("\\s");
        String command = words[0];
        String result = "";
        switch (command) {
            case "push": result = push(cmd); break;
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
