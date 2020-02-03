package com.luckymacro.app;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParserTest {
    String lines = "push constant 7\n" +
        "push constant 8\n" +
        "add\n";
    // push constant 7
    String l1 = "@7\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
    // push constant 8
    String l2 = "@8\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
    // add
    String l3 = "@SP\nM=M-1\nA=M\nD=M\nM=0\n@SP\nM=M-1\nA=M\nM=M+D\n@SP\nM=M+1\n";
    String expt = l1 + l2 + l3;
    @Test
    public void shouldAnswerWithTrue() {
        String result = Parser.parse(lines);
        assertTrue(result.contentEquals(expt));
    }
}
