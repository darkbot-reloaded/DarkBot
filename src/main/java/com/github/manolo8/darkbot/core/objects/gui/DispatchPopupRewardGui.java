package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.core.objects.Gui;
import eu.darkbot.api.API;

public class DispatchPopupRewardGui extends Gui implements API.Singleton {
    @Override
    public boolean show(boolean value) {
        if (!value && visible && isAnimationDone()) {
            click(width, 5);
        }
        return value == visible && isAnimationDone();
    }

}
