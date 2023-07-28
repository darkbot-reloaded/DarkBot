package com.github.manolo8.darkbot.core.api.adapters;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPIImpl;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkBoat;

@Deprecated
public class DarkBoatAdapter extends GameAPIImpl<
        DarkBoat,
        DarkBoat,
        DarkBoat,
        ByteUtils.ExtraMemoryReader,
        DarkBoat,
        DarkBoatAdapter.DarkBoatDirectInteraction> {

    public DarkBoatAdapter(StartupParams params, DarkBoatDirectInteraction di, DarkBoat darkboat, BotInstaller botInstaller) {
        super(params,
                darkboat,
                darkboat,
                darkboat,
                new ByteUtils.ExtraMemoryReader(darkboat, botInstaller),
                darkboat,
                di,
                Capability.LOGIN,
                Capability.INITIALLY_SHOWN,
                Capability.ALL_KEYBINDS_SUPPORT,
                Capability.CREATE_WINDOW_THREAD,
                Capability.DIRECT_LIMIT_FPS);
    }

    @Override
    public String getVersion() {
        return "darkboat-" + window.getVersion();
    }

    public static class DarkBoatDirectInteraction extends NoopAPIAdapter.NoOpDirectInteraction {
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