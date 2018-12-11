package com.github.manolo8.darkbot.core.entities;

public class Box extends Entity {

    private int tries;
    private boolean collected;

    public Box(long address, int id) {
        super(address, id);
    }

    public void trying() {
        tries++;
    }

    public boolean ignore() {
        return tries > 5 || collected;
    }

    public void collected() {
        collected = true;
    }
}
