package com.luckymacro.app;

public class ACmd extends AbstractACmd {
    public final String value;

    ACmd(String asm) {
        // validation can go here
        value = asm;
    }

    public String accept(CmdBuilder cb) {
        return cb.ACmdToAsm(this);
    }
}
