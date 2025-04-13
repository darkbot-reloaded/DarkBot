package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.core.objects.Gui;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.util.Timer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssemblyGui extends Gui implements API.Singleton {
    private final BotAPI bot;
    private final Timer guiUsed = Timer.getRandom(19_000, 1000);

    @Override
    public void update() {
        super.update();
        // Last gui usage >20s ago, close gui
        if (bot.isRunning() && guiUsed.isInactive()) {
            this.show(false);
        }
    }
}
