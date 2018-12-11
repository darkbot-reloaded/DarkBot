package com.github.manolo8.darkbot.core.utils.pet;

import com.github.manolo8.darkbot.core.objects.Gui;

public abstract class PetModule {

    protected Gui gui;
    protected Gui.PixelHelper helper;

    public void update(Gui gui) {
        this.gui = gui;
    }

    protected void updateHelper() {
        helper = gui.createHelper(30, 150);
    }

    public boolean isEnabled() {
        updateHelper();
        return gui.show(true) && isEnabled0();
    }

    public boolean enable() {
        updateHelper();
        return gui.show(true) && showComboBox() && enable0();
    }

    protected boolean showComboBox() {
        for (int i = 0; i < helper.size; i++) {

            int c = helper.pixels[i];

            if (c == 16763904
                    && helper.add(i, 0, -5) == 5330521
                    && helper.add(i, -5, 2) == 5854544
                    && helper.add(i, 4, -2) == 4277057
            ) {
                helper.click(i);
                return false;
            }

        }

        return true;
    }

    protected abstract boolean isEnabled0();

    protected abstract boolean enable0();

}
