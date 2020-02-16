package com.luckymacro.app;

public class Parser {
    public static String parse( String vmcode ) {
        String[] lines = vmcode.split("\\n");
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < lines.length; i += 1) {
            String line = lines[i];
            if (lineToKeep(line)) {
                sb.append("// " + line + "\n");
                sb.append(dispatch(line));
            }
        }
        return sb.toString();
    }

    private static final String incSp = "@SP\nM=M+1\n";
    private static final String decSp = "@SP\nM=M-1\n";
    private static final String derefSp = "@SP\nA=M\n";
    private static final String readSpToD = "A=M\nD=M\n";
    private static final String deleteStackEntry = "M=0\n";

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
        String readSpToMAndAddToD = "A=M\nM=M+D\n";
        return decSp +
            readSpToD +
            deleteStackEntry +
            decSp +
            readSpToMAndAddToD +
            incSp;
    }

    private static String eq() {
        String readSpToMAndSubFromD = "A=M\nM=M-D\n";
        String ifEqThen1Else0 = "D=M\n@1\nD;JEQ\n@0";
        return decSp +
            readSpToD +
            deleteStackEntry +
            decSp +
            readSpToMAndSubFromD +
            ifEqThen1Else0 +
            incSp;
    }

    private static String lt() {
        return "";
    }

    private static String gt() {
        return "";
    }

    private static String sub() {
        String readSpToMAndSubFromD = "A=M\nM=M-D\n";
        return decSp +
            readSpToD +
            deleteStackEntry +
            decSp +
            readSpToMAndAddToD +
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

    private static String dispatch(String cmd) {
        String[] words = cmd.split("\\s");
        String command = words[0];
        String result = "";
        switch (command) {
            case "push": result = push(cmd); break;
            case "eq": result = eq(); break;
            case "lt": result = lt(); break;
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
