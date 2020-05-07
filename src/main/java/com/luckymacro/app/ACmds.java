package com.luckymacro.app;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.Arrays;

public class ACmds {
    private final ArrayList<ACmd> asms;

    ACmds(ACmd a) {
        ArrayList<ACmd> al;
        al = new ArrayList<>();
        al.add(a);
        asms = al;
    }

    ACmds(ACmds as) {
        asms = as.asms;
    }

    ACmds(ACmd a1, ACmd a2) {
        ArrayList<ACmd> al;
        al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        asms = al;
    }

    ACmds(ACmds as, ACmd a) {
        ArrayList<ACmd> al;
        al = new ArrayList<>(as.asms);
        al.add(a);
        asms = al;
    }

    ACmds(ACmd a, ACmds as) {
        ArrayList<ACmd> al;
        al = new ArrayList<>();
        al.add(a);
        al.addAll(as.asms);
        asms = al;
    }

    ACmds(ACmds as1, ACmds as2) {
        ArrayList<ACmd> al;
        al = new ArrayList<>(as1.asms);
        al.addAll(as2.asms);
        asms = al;
    }

    public ACmds add(ACmd a) {
        return new ACmds(this, a);
    }

    public ACmds add(ACmds as) {
        return new ACmds(this, as);
    }

    public ArrayList<ACmd> toArrayList() {
        return (ArrayList<ACmd>)asms.clone();
    }
}
