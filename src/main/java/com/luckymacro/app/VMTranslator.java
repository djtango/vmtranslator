package com.luckymacro.app;

public class VMTranslator {
    public static void main( String[] args ) {
        String filename = args[0];
        System.out.println( FileReader.read(filename) );
    }
}
