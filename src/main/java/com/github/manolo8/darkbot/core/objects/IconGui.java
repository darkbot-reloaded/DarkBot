package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.API;

public class IconGui extends Gui implements API.Singleton {
    @Override
    public void update() {
        if (address == 0) return;
        super.update();
        width = (int) Main.API.readMemoryDouble(address + 0x1F8);
        height = (int) Main.API.readMemoryDouble(address + 0x200);
        visible = Main.API.readMemoryLong(address + 0x180) != 0; // isVisible, sprite object is set
    }

    public boolean clickAcceptPopup() {
        if (this.isVisible()) {
            this.click(100, 170);
            return true;
        }
        return false;
    }

    public boolean clickDeclinePopup() {
        if (this.isVisible()) {
            this.click(270, 170);
            return true;
        }
        return false;
    }
}
