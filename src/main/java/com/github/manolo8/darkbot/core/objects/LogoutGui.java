package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;

public class LogoutGui extends Gui {

    private final SettingsProxy settingsProxy;
    private long lastShown;

    public long getLastShown() {
        return lastShown;
    }

    public LogoutGui(SettingsProxy settingsProxy) {
        this.settingsProxy = settingsProxy;
    }

    @Override
    public void update() {
        super.update();
        // Due to bugs in logout gui, make it so isAnimationDone will only be
        // true a while after tweening, instead of instantly, by updating time
        if (isTweening) time = System.currentTimeMillis();
    }

    @Override
    public boolean show(boolean value) {
        if (value) lastShown = System.currentTimeMillis();
        if (value) {
            Character charCode = settingsProxy.getCharCode(SettingsProxy.KeyBind.LOGOUT);
            if (charCode != null){
                if (trySetShowing(true)) {
                    Main.API.keyboardClick(charCode);
                    return false;
                }
                return visible && isAnimationDone();
            } else {
                settingsProxy.pressKeybind(SettingsProxy.KeyBind.LOGOUT); // to trigger keybinds reset
            }
        }
        return super.show(value);
    }
}
