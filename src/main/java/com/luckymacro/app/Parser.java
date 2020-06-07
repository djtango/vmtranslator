package com.luckymacro.app;

public class Parser {
    public static String parse( String filename, String vmcode ) {
        State state;
        state = new State(filename);
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
        private String filename;
        private State (String fname) {
            filename = fname;
            cmdCount = 0;
        }

        public int getCmdCount() {
            return cmdCount;
        }

        public int incCmdCount() {
            return cmdCount += 1;
        }

        public String getFilename() {
            return filename;
        }
    }

    private static String[] splitws (String s) {
        return s.split("\\s");
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

    private static AStaticSym sSym(String index) {
        return new AStaticSym(index);
    }

    private static final ACmd sp = a("@SP");
    private static final ACmd local = a("@LCL");
    private static final ACmd arg = a("@ARG");
    private static final ACmd aThis = a("@THIS");
    private static final ACmd aThat = a("@THAT");
    private static final ACmd temp = a("@5");
    private static final ACmd temp2 = a("@6");
    private static final ACmd temp3 = a("@7");

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

    private static ACmds pushPointer(String index) {
        int idx = Integer.parseInt(index);
        if (idx > 1 || idx < 0) {
            System.out.println("ERROR: invalid pointer index supplied");
        }
        int THIS = 3;
        String absoluteIndex = Integer.toString(THIS + idx);
        ACmd atPtrIdx = a("@" + absoluteIndex);
        return new
            ACmds(atPtrIdx)
            .add(a("D=M"))
            .add(pushD);
    }

    private static ACmds pushStatic(String index) {
        return new
            ACmds(sSym(index))
            .add(a("D=M"))
            .add(pushD);
    }

    private static ACmds pushTempSegment(String index) {
        ACmd atTempIdx = a("@" + absoluteTempIndex(index));
        return new
            ACmds(atTempIdx)
            .add(a("D=M"))
            .add(pushD);
    }

    private static String push(State state, String cmd) {
        String[] words = splitws(cmd);
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
            case "pointer": result = pushPointer(index); break;
            case "static": result = pushStatic(index); break;
        }
        return cmdToString(state, result);
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

    private static ACmds popPointer(String index) {
        int idx = Integer.parseInt(index);
        if (idx > 1 || idx < 0) {
            System.out.println("ERROR: invalid pointer index supplied");
        }
        int THIS = 3;
        String absoluteIndex = Integer.toString(THIS + idx);
        ACmd atPtrIdx = a("@" + absoluteIndex);
        return new ACmds(popD).add(atPtrIdx).add(a("M=D"));
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

    private static ACmds popStatic(String index) {
        return new ACmds(popD).add(sSym(index)).add(a("M=D"));
    }

    private static String pop(State state, String cmd) {
        String[] words = splitws(cmd);
        String segment = words[1];
        String index = words[2];
        ACmds result = new ACmds(a("//"));
        switch (segment) {
            case "local": result = popVirtualSegment(local, index); break;
            case "argument": result = popVirtualSegment(arg, index); break;
            case "this": result = popVirtualSegment(aThis, index); break;
            case "that": result = popVirtualSegment(aThat, index); break;
            case "temp": result = popTempSegment(index); break;
            case "pointer": result = popPointer(index); break;
            case "static": result = popStatic(index); break;
        }
        return cmdToString(state, result);
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

    private static ASym register(String symStr) {
        return sym("(" + symStr + "%1$s)");
    }

    private static ASym atSym(String symStr) {
        return sym("@" + symStr + "%1$s");
    }

    private static ACmds set(ACmd x, ACmds expr) {
        return new ACmds(expr).add(x).add(a("M=D"));
    }

    private static ACmds xSubY(ACmd x, int y) {
        String yStr = Integer.toString(y);
        return new ACmds(x).add(a("D=M")).add(a("@" + yStr)).add(a("D=D-A"));
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

    private static String label(String cmd) {
        // TODO scope of the label is the function in which it is defined
        String[] words = splitws(cmd);
        String labelStr = words[1];
        return cmdToString(new ACmds(a("("+ labelStr +")")));
    }

    private static String ifGoto(String cmd) {
        String[] words = splitws(cmd);
        String labelStr = words[1];
        return cmdToString(new
                ACmds(popD)
                .add(a("@" + labelStr))
                .add(a("D;JNE"))
                );
    }

    private static ACmds aGoto(ACmd aLabel) {
        return new ACmds(aLabel).add(a("0;JMP"));
    }
    private static ACmds aGoto(ACmds aLabel) {
        return new ACmds(aLabel).add(a("0;JMP"));
    }

    private static String _goto(String cmd) {
        String[] words = splitws(cmd);
        String labelStr = words[1];
        ACmd aLabel = a("@"+labelStr);
        return cmdToString(aGoto(aLabel));
    }

    private static String call(State state, String cmd) {
        String[] words = splitws(cmd);
        String fn = words[1];
        int nArgs = Integer.parseInt(words[2]);
        String returnAddressSym = "RETURN$" + fn;
        ACmds saveReturnAddress = new ACmds(atSym(returnAddressSym)).add(a("D=A")).add(pushD);
        ACmds pushLocal = new ACmds(local, a("D=M")).add(pushD);
        ACmds pushArg = new ACmds(arg, a("D=M")).add(pushD);
        ACmds pushThis = new ACmds(aThis, a("D=M")).add(pushD);
        ACmds pushThat = new ACmds(aThat, a("D=M")).add(pushD);
        ACmds setArgToSpSubNSub5 = set(arg, xSubY(sp, (5 + nArgs)));
        ACmds setLclToSp = set(local, new ACmds(sp).add(a("D=M")));
        return cmdToString(state,
                new
                ACmds(saveReturnAddress)
                .add(pushLocal)
                .add(pushArg)
                .add(pushThis)
                .add(pushThat)
                .add(setArgToSpSubNSub5)
                .add(setLclToSp)
                .add(aGoto(a("@"+fn)))
                .add(register(returnAddressSym))
                );
    }

    private static String function(String cmd) {
        String[] words = splitws(cmd);
        String fn = words[1];
        int nArgs = Integer.parseInt(words[2]);
        ACmds result = new ACmds(a("(" + fn + ")"));
        for (int i = 0; nArgs > i; i += 1) {
            result = result.add(pushConstant("0"));
        }
        return cmdToString(result);
    }

    private static String _return() {
        ACmds setFrameToLocal = set(temp3, new ACmds(local).add(a("D=M")));
        ACmds derefDToD = new ACmds(a("A=D")).add(a("D=M"));
        ACmd RET = a("@6");
        ACmds getReturnAddr = set(RET, xSubY(temp3, 5).add(derefDToD));
        ACmds setReturnValueToArg0 = new ACmds(popD).add(arg).add(a("A=M")).add(a("M=D"));
        ACmds setSPToArg1 = set(sp, new ACmds(arg).add(a("D=M")).add(a("D=D+1")));
        ACmds setThatToFrameSub1 = set(aThat, xSubY(temp3, 1).add(derefDToD));
        ACmds setThisToFrameSub2 = set(aThis, xSubY(temp3, 2).add(derefDToD));
        ACmds setArgToFrameSub3 = set(arg, xSubY(temp3, 3).add(derefDToD));
        ACmds setLclToFrameSub4 = set(local, xSubY(temp3, 4).add(derefDToD));
        ACmds gotoRet = aGoto(new ACmds(RET).add(a("A=M")));
        return cmdToString(new
                ACmds(setFrameToLocal)
                .add(getReturnAddr)
                .add(setReturnValueToArg0)
                .add(setSPToArg1)
                .add(setThatToFrameSub1)
                .add(setThisToFrameSub2)
                .add(setArgToFrameSub3)
                .add(setLclToFrameSub4)
                .add(gotoRet)
                );
    }

    public static String bootstrap() {
        State state = new State("__Bootstrap__");
        ACmds aInitSP = new
            ACmds(a("@256"))
            .add(a("D=A"))
            .add(a("@SP"))
            .add(a("M=D"));
        String initSp = new CmdBuilder().load(aInitSP).finish();
        String callSysInit = call(state, "call Sys.init 0");
        return initSp + callSysInit;
    }


    private static String dispatch(State state, String cmd) {
        String[] words = splitws(cmd);
        String command = words[0];
        String result = "";
        switch (command) {
            case "push": result = push(state, cmd); break;
            case "pop": result = pop(state, cmd); break;
            case "label": result = label(cmd); break;
            case "goto": result = _goto(cmd); break;
            case "if-goto": result = ifGoto(cmd); break;
            case "call": result = call(state, cmd); break;
            case "function": result = function(cmd); break;
            case "return": result = _return(); break;
            case "eq": result = eq(state); break;
            case "lt": result = lt(state); break;
            case "gt": result = gt(state); break;
            case "add": result = add(); break;
            case "sub": result = sub(); break;
            case "neg": result = neg(); break;
            case "and": result = and(); break;
            case "or": result = or(); break;
            case "not": result = not(); break;
            default: result = "unrecognised input: " + cmd; break;
        }
        return result;
    }

    public static boolean lineToKeep(String line) {
        boolean isCommentLine = line.matches("^\\r*\\/\\/[^\n]*");
        boolean isEmptyLine = line.matches("^[\\s\\r]*$");
        return !(isCommentLine || isEmptyLine);
    }
}
