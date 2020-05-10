package com.luckymacro.app;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.Arrays;

public class ACmds {
    private final ArrayList<AbstractACmd> asms;

    ACmds(AbstractACmd a) {
        ArrayList<AbstractACmd> al;
        al = new ArrayList<>();
        al.add(a);
        asms = al;
    }

    ACmds(ACmds as) {
        asms = as.asms;
    }

    ACmds(AbstractACmd a1, AbstractACmd a2) {
        ArrayList<AbstractACmd> al;
        al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        asms = al;
    }

    ACmds(ACmds as, AbstractACmd a) {
        ArrayList<AbstractACmd> al;
        al = new ArrayList<>(as.asms);
        al.add(a);
        asms = al;
    }

    ACmds(AbstractACmd a, ACmds as) {
        ArrayList<AbstractACmd> al;
        al = new ArrayList<>();
        al.add(a);
        al.addAll(as.asms);
        asms = al;
    }

    ACmds(ACmds as1, ACmds as2) {
        ArrayList<AbstractACmd> al;
        al = new ArrayList<>(as1.asms);
        al.addAll(as2.asms);
        asms = al;
    }

    public ACmds add(AbstractACmd a) {
        return new ACmds(this, a);
    }

    public ACmds add(ACmds as) {
        return new ACmds(this, as);
    }

    public ArrayList<AbstractACmd> toArrayList() {
        return (ArrayList<AbstractACmd>)asms.clone();
    }
}
