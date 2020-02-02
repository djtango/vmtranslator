package com.luckymacro.app;

public class Parser {
    public static String parse( String lines ) {
        // push constant 7
        String l1 = "@7\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        // push constant 8
        String l2 = "@8\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        // add
        String l3 = "@SP\nM=M-1\nA=M\nD=M\nM=0\n@SP\nM=M-1\nA=M\nM=M+D\n@SP\nM=M+1\n";
        return l1 + l2 + l3;
    }
}
