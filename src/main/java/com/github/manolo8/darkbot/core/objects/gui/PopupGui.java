package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Gui;
import eu.darkbot.api.API;

class PopupGui extends Gui implements API.Singleton {
    @Override
    public void update() {
        if (address == 0) return;
        super.update();
        width = (int) Main.API.readMemoryDouble(address + 0x1F8);
        height = (int) Main.API.readMemoryDouble(address + 0x200);
        visible = Main.API.readMemoryLong(address + 0x180) != 0; // isVisible, sprite object is set
    }
}
