package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.API;

public class DispatchGui extends Gui implements API.Singleton {
    public boolean openRetrieverTab() {
        if (show(true)) {
            click(80, 70);
        }
        return isAnimationDone();
    }

    public boolean openAvailableTab() {
        if (show(true)) {
            click(80, 100);
        }
        return isAnimationDone();
    }

    public boolean clickFirstItem() {
        if (show(true)) {
            Time.sleep(25);
            click(300, 150);
        }
        return isAnimationDone();
    }

    public boolean clickHire() {
        if (show(true)) {
            click(700, 375);
        }
        return isAnimationDone();
    }

    public boolean openInProgressTab() {
        if (show(true)) {
            click(200, 100);
        }
        return isAnimationDone();
    }

    public boolean clickCollect(int i) {
        if (openInProgressTab()) {
            Time.sleep(25);
            click(260, 160 + (41 * i));
        }
        return isAnimationDone();
    }

}
