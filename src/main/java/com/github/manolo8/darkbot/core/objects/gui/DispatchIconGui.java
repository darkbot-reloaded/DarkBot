package com.github.manolo8.darkbot.core.objects.gui;

import eu.darkbot.api.API;

public class DispatchIconGui extends PopupGui implements API.Singleton {
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
