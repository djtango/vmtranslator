package com.luckymacro.app;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.Arrays;

class CmdBuilder {
    public List<String> out;
    private int cmdCount;
    private String filename;
    private StringBuilder sb;
    private CmdBuilder self;

    CmdBuilder(Parser.State state) {
        cmdCount = state.getCmdCount();
        filename = state.getFilename();
        state.incCmdCount();
        out = new ArrayList<String>();
    }

    CmdBuilder() {
        out = new ArrayList<String>();
    }

    public String ACmdToAsm (ACmd acmd) {
        return acmd.value;
    }

    public String ASymToAsm (ASym asym) {
        return String.format(asym.value, cmdCount);
    }

    public String AStaticSymToAsm (AStaticSym assym) {
        String stripExtension = filename.split("\\.vm")[0];
        String staticFilename = stripExtension.replaceAll("\\/", ".");
        return String.format("@%1$s.%2$s", staticFilename, assym.value);
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
        return sb.toString();
    }
}
