package com.github.manolo8.darkbot.core.api.adapters;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPIImpl;
import com.github.manolo8.darkbot.core.manager.HookAdapter;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkBoat;

@Deprecated
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
                Capability.LOGIN,
                Capability.INITIALLY_SHOWN,
                Capability.CREATE_WINDOW_THREAD,
                Capability.ALL_KEYBINDS_SUPPORT,
                // Dark Hook!
                Capability.DIRECT_LIMIT_FPS,
                Capability.DIRECT_MOVE_SHIP,
                Capability.DIRECT_COLLECT_BOX,
                Capability.DIRECT_REFINE, Capability.DIRECT_CALL_METHOD);
    }

    @Override
    public String getVersion() {
        return "darkboat-" + window.getVersion() + "&hook-" + direct.getVersion();
    }

    @Override
    public void setMaxFps(int maxFps) {
        int version = window.getVersion();
        if (version >= 8) window.setMaxFps(maxFps);
        else System.out.println("FPS limiting in darkboat is only available in version 8+, you are using version " + version);
    }

    @Override
    public boolean hasCapability(Capability capability) {
        HookAdapter.Flag flag = HookAdapter.Flag.of(capability);
        if (flag != null && !direct.isEnabled(flag)) return false;

        return super.hasCapability(capability);
    }

}