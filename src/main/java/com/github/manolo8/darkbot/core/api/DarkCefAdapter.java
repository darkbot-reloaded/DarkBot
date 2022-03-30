package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkCef;
import eu.darkbot.api.DarkMem;
import eu.darkbot.api.utils.Inject;

public class DarkCefAdapter extends GameAPIImpl<
        DarkCef,
        DarkCef,
        DarkMem,
        ByteUtils.StringReader,
        DarkCef,
        GameAPI.NoOpDirectInteraction> {

    @Inject
    public DarkCefAdapter(BotInstaller botInstaller, StartupParams params, DarkMem mem) {
        this(botInstaller, params, DarkCef.getInstance(), mem);
    }

    protected DarkCefAdapter(BotInstaller botInstaller, StartupParams params, DarkCef cef, DarkMem mem) {
        super(botInstaller,
                params,
                cef,
                cef,
                mem,
                new ByteUtils.StringReader(mem),
                cef,
                new GameAPI.NoOpDirectInteraction(),
                GameAPI.Capability.LOGIN,
                GameAPI.Capability.INITIALLY_SHOWN, GameAPI.Capability.CREATE_WINDOW_THREAD);
    }

}