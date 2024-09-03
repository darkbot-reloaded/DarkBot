package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.API;

class PopupGui extends SizableGui implements API.Singleton {
    @Override
    public void update() {
        if (address == 0) return;
        super.update();
        visible = Main.API.readLong(address + 0x180) != 0; // isVisible, sprite object is set
    }
}
