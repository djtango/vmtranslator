package com.luckymacro.app;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParserTest {
    // String[] lines = {"a", "b", "c"};
    String lines = "a\nb\nc";
    String result = "@constant7\nD=M\n@constant8\nD=D+M\n";
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(Parser.parse(lines) == result);
    }
}
