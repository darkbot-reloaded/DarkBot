package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.utils.StartupParams;

/**
 * Api that will login and only run background tasks, no ship movement
 */
public class BackpageAdapter extends GameAPIImpl<
        GameAPI.NoOpWindow,
        GameAPI.NoOpHandler,
        GameAPI.NoOpMemory,
        GameAPI.NoOpStringReader,
        GameAPI.NoOpInteraction,
        GameAPI.NoOpDirectInteraction> {

    private final StatsManager statsManager;

    public BackpageAdapter(BotInstaller botInstaller, StartupParams params, StatsManager statsManager) {
        super(botInstaller,
                params,
                new GameAPI.NoOpWindow(),
                new GameAPI.NoOpHandler(),
                new GameAPI.NoOpMemory(),
                new GameAPI.NoOpStringReader(),
                new GameAPI.NoOpInteraction(),
                new GameAPI.NoOpDirectInteraction(),
                GameAPI.Capability.LOGIN, GameAPI.Capability.BACKGROUND_ONLY);

        this.statsManager = statsManager;
    }

    @Override
    public String getVersion() {
        return "background-only";
    }

    @Override
    protected void setData() {
        statsManager.instance = "https://" + this.loginData.getUrl() + "/";
        statsManager.sid = this.loginData.getSid();
    }
}
