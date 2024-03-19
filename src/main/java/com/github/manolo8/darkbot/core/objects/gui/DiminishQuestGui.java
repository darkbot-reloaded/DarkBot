package com.github.manolo8.darkbot.core.objects.gui;

import eu.darkbot.api.API;

public class DiminishQuestGui extends SizableGui implements API.Singleton {
    public void accept() {
        if (address == 0) return;
        if (show(true)) click((int) getWidth()/2, (int) getHeight()-10);
    }
}
