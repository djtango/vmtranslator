package com.luckymacro.app;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParserTest {
    String[] lines = {"a", "b", "c"};
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(Parser.parse(lines) == lines);
    }
}
