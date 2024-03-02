package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Gui;

public class SizableGui extends Gui {
    public void update() {
        if (address == 0) return;
        super.update();
        width = (int) Main.API.readDouble(address + 0x1F8);
        height = (int) Main.API.readDouble(address + 0x200);
    }
}
