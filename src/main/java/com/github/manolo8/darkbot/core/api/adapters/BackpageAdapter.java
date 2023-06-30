package com.github.manolo8.darkbot.core.api.adapters;

import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPIImpl;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.utils.StartupParams;

/**
 * Api that will login and only run background tasks, no ship movement
 */
public class BackpageAdapter extends GameAPIImpl<
        NoopAPIAdapter.NoOpWindow,
        NoopAPIAdapter.NoOpHandler,
        NoopAPIAdapter.NoOpMemory,
        NoopAPIAdapter.NoOpExtraMemoryReader,
        NoopAPIAdapter.NoOpInteraction,
        NoopAPIAdapter.NoOpDirectInteraction> {

    private final StatsManager statsManager;

    public BackpageAdapter(StartupParams params, StatsManager statsManager) {
        super(params,
                new NoopAPIAdapter.NoOpWindow(),
                new NoopAPIAdapter.NoOpHandler(),
                new NoopAPIAdapter.NoOpMemory(),
                new NoopAPIAdapter.NoOpExtraMemoryReader(),
                new NoopAPIAdapter.NoOpInteraction(),
                new NoopAPIAdapter.NoOpDirectInteraction(),
                Capability.LOGIN, Capability.BACKGROUND_ONLY);

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
