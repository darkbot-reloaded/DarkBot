package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.API;
import eu.darkbot.api.utils.NativeAction;

public class ChatGui extends Gui implements API.Singleton {

    public void writeChat(String text) {
        if (show(true)) {
            Main.API.pasteText(text,
                    NativeAction.Mouse.CLICK.of(x + 50, (int) (getY2() - 9)),
                    NativeAction.Key.CLICK.after(13));
        }
    }
}
