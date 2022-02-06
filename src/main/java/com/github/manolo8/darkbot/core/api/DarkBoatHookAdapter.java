package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.manager.HookManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkBoat;

public class DarkBoatHookAdapter extends GameAPIImpl<
        DarkBoat,
        DarkBoat,
        DarkBoat,
        ByteUtils.StringReader,
        DarkBoat,
        HookManager> {

    public DarkBoatHookAdapter(StartupParams params, DarkBoat darkboat, HookManager hookManager) {
        super(params,
                darkboat,
                darkboat,
                darkboat,
                new ByteUtils.StringReader(darkboat),
                darkboat,
                hookManager,
                GameAPI.Capability.LOGIN,
                GameAPI.Capability.INITIALLY_SHOWN,
                GameAPI.Capability.CREATE_WINDOW_THREAD,
                // Dark Hook!
                GameAPI.Capability.DIRECT_LIMIT_FPS,
                GameAPI.Capability.DIRECT_COLLECT_BOX,
                GameAPI.Capability.DIRECT_MOVE_SHIP,
                GameAPI.Capability.DIRECT_REFINE,
                GameAPI.Capability.DIRECT_CALL_METHOD);
    }

    @Override
    public String getVersion() {
        return "darkboat&hook-" + window.getVersion();
    }

    @Override
    public boolean hasCapability(GameAPI.Capability capability) {
        if (capability == GameAPI.Capability.DIRECT_LIMIT_FPS && !direct.isHookEnabled()) return false;
        if (capability == GameAPI.Capability.DIRECT_MOVE_SHIP && !direct.isTravelEnabled()) return false;
        if (capability == GameAPI.Capability.DIRECT_COLLECT_BOX && !direct.isCollectEnabled()) return false;
        if (capability == GameAPI.Capability.DIRECT_REFINE && !direct.isRefineEnabled()) return false;
        if (capability == GameAPI.Capability.DIRECT_CALL_METHOD && !direct.isHookEnabled()) return false;

        return super.hasCapability(capability);
    }
}