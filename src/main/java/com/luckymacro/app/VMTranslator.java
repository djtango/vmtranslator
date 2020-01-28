package com.luckymacro.app;
// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm

// Pushes and adds two constants.
// push constant 7
// push constant 8
// add
//
// tmp0=7
// tmp1=8
// @constant7
// D=M
// @constant8
// D=D+M

public class VMTranslator {
    public static void main( String[] args ) {
        String filename = args[0];
        String fileContents = FileReader.read(filename);
        String output = Parser.parse(fileContents);
        FileWriter.write(filename + ".asm", output);
    }
}
