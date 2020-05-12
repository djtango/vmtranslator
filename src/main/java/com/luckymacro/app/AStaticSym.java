package com.luckymacro.app;

public class AStaticSym extends AbstractACmd {
    public final String value;

    AStaticSym(String index) {
        value = index;
    }

    public String accept(CmdBuilder cb) {
        return cb.AStaticSymToAsm(this);
    }
}
