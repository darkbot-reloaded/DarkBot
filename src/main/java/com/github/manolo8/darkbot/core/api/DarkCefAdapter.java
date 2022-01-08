package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkCef;
import eu.darkbot.api.DarkMem;

public class DarkCefAdapter extends GameAPIImpl<DarkCef, DarkCef, DarkMem, ByteUtils.StringReader, DarkCef> {

    public DarkCefAdapter(StartupParams params, DarkCef CEF, DarkMem MEM) {
        super(params, CEF, CEF, MEM, new ByteUtils.StringReader(MEM), CEF,
                GameAPI.Capability.LOGIN, GameAPI.Capability.INITIALLY_SHOWN, GameAPI.Capability.CREATE_WINDOW_THREAD);
    }

    public static DarkCefAdapter of(StartupParams params) {
        return new DarkCefAdapter(params, DarkCef.getInstance(), new DarkMem());
    }

}