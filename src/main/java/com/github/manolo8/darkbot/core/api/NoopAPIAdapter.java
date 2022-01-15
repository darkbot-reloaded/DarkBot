package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.utils.StartupParams;

/**
 * No-operation API adapter. Will do nothing. Useful for testing purposes, and as fallback if no API is loaded.
 */
public class NoopAPIAdapter extends GameAPIImpl<
        GameAPI.NoOpWindow,
        GameAPI.NoOpHandler,
        GameAPI.NoOpMemory,
        GameAPI.NoOpStringReader,
        GameAPI.NoOpInteraction,
        GameAPI.NoOpDirectInteraction> {

    public NoopAPIAdapter(StartupParams params) {
        super(params,
                new GameAPI.NoOpWindow(),
                new GameAPI.NoOpHandler(),
                new GameAPI.NoOpMemory(),
                new GameAPI.NoOpStringReader(),
                new GameAPI.NoOpInteraction(),
                new GameAPI.NoOpDirectInteraction());
    }

    public static NoopAPIAdapter of(StartupParams params) {
        return new NoopAPIAdapter(params);
    }

    @Override
    public String getVersion() {
        return "no-op";
    }

}
