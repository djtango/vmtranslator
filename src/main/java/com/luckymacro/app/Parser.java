package com.luckymacro.app;

public class Parser {
    public static String parse( String lines ) {
        // add
        String l3 = "@SP\nM=M-1\nA=M\nD=M\nM=0\n@SP\nM=M-1\nA=M\nM=M+D\n@SP\nM=M+1\n";
        String[] cmds = lines.split("\\n");
        String cmd1 = cmds[0];
        String cmd2 = cmds[1];
        return push(cmd1) + push(cmd2) + l3;
    }

    private static final String incSp = "@SP\nM=M+1\n";
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
}
