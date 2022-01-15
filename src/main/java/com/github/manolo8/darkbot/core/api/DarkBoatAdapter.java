package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkBoat;

public class DarkBoatAdapter extends GameAPIImpl<
        DarkBoat,
        DarkBoat,
        DarkBoat,
        ByteUtils.StringReader,
        DarkBoat,
        GameAPI.NoOpDirectInteraction> {

    private DarkBoatAdapter(StartupParams params, DarkBoat darkboat) {
        super(params,
                darkboat,
                darkboat,
                darkboat,
                new ByteUtils.StringReader(darkboat),
                darkboat,
                new GameAPI.NoOpDirectInteraction(),
                GameAPI.Capability.LOGIN,
                GameAPI.Capability.INITIALLY_SHOWN,
                GameAPI.Capability.CREATE_WINDOW_THREAD);
    }

    public static DarkBoatAdapter of(StartupParams params) {
        return new DarkBoatAdapter(params, new DarkBoat());
    }

    @Override
    public String getVersion() {
        return "darkboat-" + window.getVersion();
    }

}