package com.luckymacro.app;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.Arrays;

public class Parser {
    public static String parse( String vmcode ) {
        ParserState state;
        state = new ParserState();
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

    private static class ParserState {
        public int eq;
        private ParserState () {
            eq = 0;
        }

        private int getEq() {
            return eq;
        }

        private int incEq() {
            return eq += 1;
        }
    }

    private static class CmdBuilder {
        public List<String> out;
        public ParserState cmdState;
        private StringBuilder sb;
        CmdBuilder(ParserState state) {
            cmdState = state;
            out = new ArrayList<String>();
        }

        public CmdBuilder n( String line ) {
            out.add(line);
            return this;
        }

        public CmdBuilder n( String[] lines ) {
            for (int i=0; i < lines.length; i +=1) {
                String line = lines[i];
                out.add(line);
            }
            return this;
        }

        public CmdBuilder acmds( ACmds as ) {
            as.toArrayList().forEach(new Consumer <ACmd>() {
                @Override
                public void accept(ACmd a) {
                    out.add(a.value);
                }
            });
            return this;
        }

        public CmdBuilder n ( ArrayList<String> lines ) {
            lines.forEach(new Consumer <String>() {
                @Override
                public void accept(String s) {
                    out.add(s);
                }
            });
            return this;
        }

        public String finish () {
            sb = new StringBuilder();
            out.forEach(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    sb.append(s + "\n");
                }
            });
            return sb.toString();
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

    private static String eq(ParserState state) {
        int counter;
        counter = state.getEq();
        String trueBranchSym = "VMeqSetTrueSym" + counter;
        String falseBranchSym = "VMeqSetFalseSym" + counter;
        String endOfEqSym = "VMeqEndOfFunctionSym" + counter;
        state.incEq();
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

    private static ACmd a(String s) {
        return new ACmd(s);
    }

    private static String lt(ParserState state) {
        // a is less than b if a - b < 0

        ACmds decSp = new ACmds(a("@SP"), a("M=M-1"));
        ACmds readSpToD = new ACmds(a("A=M"), a("D=M"));
        ACmd deleteStackEntry = a("M=0");
        ACmds popD =
            new ACmds(decSp)
            .add(readSpToD)
            .add(deleteStackEntry);
        ACmds readSpToMAndSubDToD = new ACmds(a("A=M"), a("D=D-M"));
        ACmds asm =
            new ACmds(popD)
            .add(decSp)
            .add(readSpToMAndSubDToD);
        // think about parameterizing an ACmd form for symbols
        return new CmdBuilder(state).acmds(asm).finish();
        // return new CmdBuilder(state)
        //     .n(popD)
        //     .n(decSp)
        //     .n(readSpToMAndSubDToD)
        //     .finish();
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

    private static String dispatch(ParserState state, String cmd) {
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
