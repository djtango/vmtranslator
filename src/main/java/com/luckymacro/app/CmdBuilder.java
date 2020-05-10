package com.luckymacro.app;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.Arrays;

class CmdBuilder {
    public List<String> out;
    public Parser.State cmdState;
    private StringBuilder sb;
    private CmdBuilder self;

    CmdBuilder(Parser.State state) {
        cmdState = state;
        out = new ArrayList<String>();
    }

    public String ACmdToAsm (ACmd acmd) {
        return acmd.value;
    }

    public String ASymToAsm (ASym asym) {
        int lt = cmdState.getCmdCount();
        return String.format(asym.value, lt);
    }

    public CmdBuilder load( ACmds as ) {
        self = this;
        as.toArrayList().forEach(new Consumer <AbstractACmd>() {
            @Override
            public void accept(AbstractACmd a) {
                // dispatch between different children
                String asm = a.accept(self);
                out.add(asm);
            }
        });
        return this;
    }

    public String finish () {
        sb = new StringBuilder();
        out.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                sb.append(s + "\n");
            }
        });
        cmdState.incCmdCount();
        return sb.toString();
    }
}
