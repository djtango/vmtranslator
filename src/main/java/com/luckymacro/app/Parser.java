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

    private static final String incSp = "@SP\nM=M+1\n";
    private static final String decSp = "@SP\nM=M-1\n";
    private static final String derefSp = "@SP\nA=M\n";
    private static final String readSpToD = "A=M\nD=M\n";
    private static final String deleteStackEntry = "M=0\n";
    private static final String setSpToD = derefSp + "M=D\n";
    private static final String readSpToMAndAddToD = "A=M\nM=M+D\n";
    private static final String popD = decSp + readSpToD + deleteStackEntry;

    private static String readVariableToD(String memSegment, String segAddr) {
        return String.format("@%1$s\nD=A\n", segAddr);
    }

    private static String push(String cmd) {
        String[] words = cmd.split("\\s");
        String memSegment = words[1];
        String segAddr = words[2];
        return readVariableToD(memSegment, segAddr) + setSpToD + incSp;
    }

    private static String add() {
        return decSp +
            readSpToD +
            deleteStackEntry +
            decSp +
            readSpToMAndAddToD +
            incSp;
    }

    private static String eq(State state) {
        int counter = state.getCmdCount();
        String trueBranchSym = "VMeqSetTrueSym" + counter;
        String falseBranchSym = "VMeqSetFalseSym" + counter;
        String endOfEqSym = "VMeqEndOfFunctionSym" + counter;
        state.incCmdCount();
        String readSpContentsAndSubFromD = "A=M\nD=M-D\n";
        String registerSetTrue = "(" + trueBranchSym + ")\n";
        String registerSetFalse = "(" + falseBranchSym + ")\n";
        String registerEnd = "("+ endOfEqSym +")\n";
        String setDToTrue = "D=-1\n";
        String setDToFalse = "D=0\n";
        String gotoEnd = "@" + endOfEqSym + "\n0;JMP\n";
        String gotoFalseBranch = "@" + falseBranchSym + "\n0;JMP\n";
        String gotoTrueBranchIfZero = "@" + trueBranchSym + "\nD;JEQ\n";
        return decSp +
            readSpToD +
            deleteStackEntry +
            decSp +
            readSpContentsAndSubFromD +
            gotoTrueBranchIfZero +
            gotoFalseBranch +
            registerSetFalse +
            setDToFalse +
            gotoEnd +
            registerSetTrue +
            setDToTrue +
            gotoEnd +
            registerEnd +
            setSpToD +
            incSp;
    }


    private static ASym sym(String s) {
        return new ASym(s);
    }
    private static ACmd a(String s) {
        return new ACmd(s);
    }

    private static String lt(State state) {
        // a is less than b if a - b < 0

        ACmds decSp = new ACmds(a("@SP"), a("M=M-1"));
        ACmds readSpToD = new ACmds(a("A=M"), a("D=M"));
        ACmd deleteStackEntry = a("M=0");
        ACmds popD =
            new
            ACmds(decSp)
            .add(readSpToD)
            .add(deleteStackEntry);
        ACmds readSpToMAndSubDToD = new ACmds(a("A=M"), a("D=M-D"));
        ACmds setSpToD = new ACmds(a("@SP")).add(a("A=M")).add(a("M=D"));
        ACmds incSp = new ACmds(a("@SP"), a("M=M+1"));
        ACmds gotoTrueBranchIfLessThanZero =
            new ACmds(sym("@LTTRUEBRANCH%1$s"), a("D;JLT"));
        ACmds gotoFalseBranch =
            new ACmds(sym("@LTFALSEBRANCH%1$s"), a("0;JMP"));
        ACmds falseBranch = new
            ACmds(sym("(LTFALSEBRANCH%1$s)"))
            .add(a("D=0"))
            .add(sym("@LTEND%1$s"))
            .add(a("0;JMP"));
        ACmds trueBranch = new
            ACmds(sym("(LTTRUEBRANCH%1$s)"))
            .add(a("D=-1"))
            .add(sym("@LTEND%1$s"))
            .add(a("0;JMP"));
        ASym registerEnd = sym("(LTEND%1$s)");
        ACmds ifDLessThanZeroThenTrueElseFalse =
            new
            ACmds(gotoTrueBranchIfLessThanZero)
            .add(gotoFalseBranch)
            .add(falseBranch)
            .add(trueBranch)
            .add(registerEnd)
            .add(setSpToD)
            .add(incSp);
        ACmds asm =
            new
            ACmds(popD)
            .add(decSp)
            .add(readSpToMAndSubDToD)
            .add(ifDLessThanZeroThenTrueElseFalse);
        return new CmdBuilder(state).load(asm).finish();
    }

    private static String gt() {
        return "";
    }

    private static String sub() {
        String readSpToMAndSubD = "A=M\nM=M-D\n";
        return decSp +
            readSpToD +
            deleteStackEntry +
            decSp +
            readSpToMAndSubD +
            incSp;
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
            case "gt": result = gt(); break;
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
