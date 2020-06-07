package com.luckymacro.app;
import java.util.ArrayList;

// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm

public class VMTranslator {
    public static void main( String[] args ) {
        String input = args[0];
        boolean isDirectory = DirectoryReader.isDirectory(input);
        if (isDirectory) {
            ArrayList<String> files = DirectoryReader.listFiles(input);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < files.size(); i += 1) {
                String filename = files.get(i);
                String fileContents = FileReader.read(filename);
                String parsedVMCode = Parser.parse(filename, fileContents);
                sb.append("// " + filename + "\n");
                sb.append(parsedVMCode + "\n");
            }
            String output = Parser.bootstrap() + sb.toString();
            FileWriter.write(input + ".asm", output);
        } else {
            String[] paths = input.split("\\.vm");
            String filename = paths[0];
            String fileContents = FileReader.read(filename + ".vm");
            String output = Parser.parse(filename, fileContents);
            FileWriter.write(filename + ".asm", output);
        }
    }
}
