package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.modules.DisconnectModule;
import eu.darkbot.api.extensions.Module;
import eu.darkbot.shared.legacy.LegacyModuleAPI;

public class LegacyModules implements LegacyModuleAPI {

    @Override
    public Module getDisconnectModule(Long pause, String reason) {
        return new DisconnectModule(pause, reason);
    }

}
