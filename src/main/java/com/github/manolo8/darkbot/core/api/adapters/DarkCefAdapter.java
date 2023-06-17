package com.github.manolo8.darkbot.core.api.adapters;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPIImpl;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkCef;
import eu.darkbot.api.DarkMem;
import eu.darkbot.api.utils.Inject;

// Unused, never actually had a working implementation
@Deprecated
public class DarkCefAdapter extends GameAPIImpl<
        DarkCef,
        DarkCef,
        DarkMem,
        ByteUtils.ExtraMemoryReader,
        DarkCef,
        NoopAPIAdapter.NoOpDirectInteraction> {

    @Inject
    public DarkCefAdapter(StartupParams params, DarkMem mem, BotInstaller botInstaller) {
        this(params, DarkCef.getInstance(), mem, botInstaller);
    }

    protected DarkCefAdapter(StartupParams params, DarkCef cef, DarkMem mem, BotInstaller botInstaller) {
        super(params,
                cef,
                cef,
                mem,
                new ByteUtils.ExtraMemoryReader(mem, botInstaller),
                cef,
                new NoopAPIAdapter.NoOpDirectInteraction(),
                Capability.LOGIN,
                Capability.INITIALLY_SHOWN, Capability.CREATE_WINDOW_THREAD);
    }

}