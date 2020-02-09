package com.github.manolo8.darkbot.core.itf;

public abstract class UpdatableAuto extends Updatable {

    @Override
    public void update(long address) {
        super.update(address);
        update();
    }
}
