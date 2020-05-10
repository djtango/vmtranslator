package com.luckymacro.app;

public class ASym extends AbstractACmd {
    public final String value;
    ASym(String asm) {
        value = asm;
    }

    public String accept(CmdBuilder cb) {
        return cb.ASymToAsm(this);
    }
}
