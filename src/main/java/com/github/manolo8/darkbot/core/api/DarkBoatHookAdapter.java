package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.manager.HookAdapter;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.BetterLogUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkBoat;

public class DarkBoatHookAdapter extends GameAPIImpl<
        DarkBoat,
        DarkBoat,
        DarkBoat,
        ByteUtils.ExtraMemoryReader,
        DarkBoat,
        HookAdapter> {

    public DarkBoatHookAdapter(StartupParams params, DarkBoat darkboat, HookAdapter hookAdapter, BotInstaller botInstaller) {
        super(params,
                darkboat,
                darkboat,
                darkboat,
                new ByteUtils.ExtraMemoryReader(darkboat, botInstaller),
                darkboat,
                hookAdapter,
                GameAPI.Capability.LOGIN,
                GameAPI.Capability.INITIALLY_SHOWN,
                GameAPI.Capability.CREATE_WINDOW_THREAD,
                // Dark Hook!
                GameAPI.Capability.DIRECT_LIMIT_FPS,
                GameAPI.Capability.DIRECT_MOVE_SHIP,
                GameAPI.Capability.DIRECT_COLLECT_BOX,
                GameAPI.Capability.DIRECT_REFINE, GameAPI.Capability.DIRECT_CALL_METHOD);
    }

    @Override
    public String getVersion() {
        return "darkboat-" + window.getVersion() + "&hook-" + direct.getVersion();
    }

    @Override
    public void setMaxFps(int maxFps) {
        int version = window.getVersion();
        if (version >= 8) window.setMaxFps(maxFps);
        else BetterLogUtils.getInstance().PrintLn("FPS limiting in darkboat is only available in version 8+, you are using version " + version);
    }

    @Override
    public boolean hasCapability(GameAPI.Capability capability) {
        HookAdapter.Flag flag = HookAdapter.Flag.of(capability);
        if (flag != null && !direct.isEnabled(flag)) return false;

        return super.hasCapability(capability);
    }

}