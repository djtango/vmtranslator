package com.luckymacro.app;

public class Parser {
    public static String parse( String lines ) {
        String[] cmds = lines.split("\\n");
        String cmd1 = cmds[0];
        String cmd2 = cmds[1];
        return push(cmd1) + push(cmd2) + add();
    }

    private static final String incSp = "@SP\nM=M+1\n";
    private static final String decSp = "@SP\nM=M-1\n";
    private static final String derefSp = "@SP\nA=M\n";

    private static String readVariableToD(String memSegment, String segAddr) {
        return String.format("@%1$s\nD=A\n", segAddr);
    }

    private static String push(String cmd) {
        String[] words = cmd.split("\\s");
        String memSegment = words[1];
        String segAddr = words[2];
        String setSpToD = derefSp + "M=D\n";
        return readVariableToD(memSegment, segAddr) + setSpToD + incSp;
    }

    private static String add() {
        String readSpToD = "A=M\nD=M\n";
        String deleteStackEntry = "M=0\n";
        String readSpToMAndAddToD = "A=M\nM=M+D\n";
        return decSp +
            readSpToD +
            deleteStackEntry +
            decSp +
            readSpToMAndAddToD +
            incSp;
    }
}
