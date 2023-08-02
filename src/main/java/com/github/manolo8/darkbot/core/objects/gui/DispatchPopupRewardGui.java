package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.core.objects.Gui;
import eu.darkbot.api.API;

public class DispatchPopupRewardGui extends Gui implements API.Singleton {
    @Override
    public boolean show(boolean value) {
        if (trySetShowing(value)) {
            if (value) throw new UnsupportedOperationException("Cannot show(true) on dispatch rewards gui");
            else click(width - 5, 5);
        }
        return value == visible && isAnimationDone();
    }

}
