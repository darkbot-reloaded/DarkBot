package com.github.manolo8.darkbot.core.def;

import com.github.manolo8.darkbot.core.manager.BotManager;

public interface Manager {

    void install(BotManager botManager);

    void stop();

}
