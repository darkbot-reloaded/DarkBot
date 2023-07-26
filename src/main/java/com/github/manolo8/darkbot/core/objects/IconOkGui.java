package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.API;

public class IconOkGui extends Gui implements API.Singleton {
    @Override
    public void update(){
        if (address == 0) return;
        super.update();
        width = (int) Main.API.readMemoryDouble(address + 0x1F8);
        height = (int) Main.API.readMemoryDouble(address + 0x200);
        visible = Main.API.readMemoryLong(address + 0x180) != 0; // isVisible, sprite object is set
    }

    public boolean clickOkRewardsPopup(int i) {
        if (i == 0) i = 1;
        if (this.isVisible()) {
            this.click(180, 136 + (13 * i));
            return true;
        }
        return false;
    }

    //This option used when the popup is an error
    //TODO: read sprite object if success or error icon used
    public boolean clickCloseOkPopup() {
        if (this.isVisible()) {
            this.click(190, 150);
            return true;
        }
        return false;
    }
}
