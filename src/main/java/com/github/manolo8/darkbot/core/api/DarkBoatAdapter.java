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
        DarkBoatAdapter.DarkBoatDirectInteraction> {

    public DarkBoatAdapter(StartupParams params, DarkBoatDirectInteraction di, DarkBoat darkboat) {
        super(params,
                darkboat,
                darkboat,
                darkboat,
                new ByteUtils.StringReader(darkboat),
                darkboat,
                di,
                GameAPI.Capability.LOGIN,
                GameAPI.Capability.INITIALLY_SHOWN,
                GameAPI.Capability.CREATE_WINDOW_THREAD, GameAPI.Capability.DIRECT_LIMIT_FPS);
    }

    @Override
    public String getVersion() {
        return "darkboat-" + window.getVersion();
    }

    public static class DarkBoatDirectInteraction extends GameAPI.NoOpDirectInteraction {
        private final DarkBoat darkboat;

        public DarkBoatDirectInteraction(DarkBoat darkboat) {
            this.darkboat = darkboat;
        }

        @Override
        public void setMaxFps(int maxFps) {
            int version = darkboat.getVersion();
            if (version >= 8) darkboat.setMaxFps(maxFps);
            else System.out.println("FPS limiting in darkboat is only available in version 8+, you are using version " + version);
        }
    }

}