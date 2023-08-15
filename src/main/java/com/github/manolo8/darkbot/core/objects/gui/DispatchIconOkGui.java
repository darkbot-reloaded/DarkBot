package com.github.manolo8.darkbot.core.objects.gui;

import eu.darkbot.api.API;

public class DispatchIconOkGui extends PopupGui implements API.Singleton {
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
