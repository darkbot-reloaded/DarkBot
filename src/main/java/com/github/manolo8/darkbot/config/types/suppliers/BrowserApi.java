package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.config.ConfigManager;
import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.api.DarkBoatAdapter;
import com.github.manolo8.darkbot.core.api.DarkCefAdapter;
import com.github.manolo8.darkbot.core.api.DarkMemAdapter;
import com.github.manolo8.darkbot.core.api.NativeApiAdapter;
import com.github.manolo8.darkbot.core.api.NoopApiAdapter;
import com.github.manolo8.darkbot.utils.StartupParams;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public enum BrowserApi {
    DARK_BOAT("Darkboat API (Recommended)", "Currently API, no browser, auto login, by Punisher", DarkBoatAdapter::new),
    NATIVE_API("Native API (unreleased)", "WIP API, never released. Several implementations by zBlock, wakatoa & Tanoshizo.", NativeApiAdapter::new),
    NO_OP_API("No-operation API (For testing)", "API that will do nothing. Useful for testing, default if error on load. By Popcorn.", NoopApiAdapter::new),
    DARK_MEM_API("DarkMem API (WIP)", "Memory-reading only API that can read from any running process. By Popcorn, based on darkboat", DarkMemAdapter::new),
    DARK_CEF_API("DarkCef API (WIP)", "Run the client in CEF browser, read using DarkMemAPI. By Popcorn, based on darkmem", DarkCefAdapter::new);

    private final String name, description;
    private final BiFunction<StartupParams, BooleanSupplier, IDarkBotAPI> constructor;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public IDarkBotAPI getInstance(StartupParams params, ConfigManager config) {
        return this.constructor.apply(params, () -> config.getConfig().BOT_SETTINGS.API_CONFIG.FULLY_HIDE_API);
    }

    BrowserApi(String name, String description, BiFunction<StartupParams, BooleanSupplier, IDarkBotAPI> constructor) {
        this.name = name;
        this.description = description;
        this.constructor = constructor;
    }

    public static class Supplier extends OptionList<BrowserApi> {
        private static final List<String> OPTIONS =
                Arrays.stream(BrowserApi.values()).map(BrowserApi::getName).collect(Collectors.toList());

        @Override
        public BrowserApi getValue(String text) {
            for (BrowserApi bapi : BrowserApi.values()) {
                if (bapi.getName().equals(text)) return bapi;
            }
            return null;
        }

        @Override
        public String getText(BrowserApi value) {
            return value.getName();
        }

        @Override
        public String getTooltipFromVal(BrowserApi value) {
            return value.getDescription();
        }

        @Override
        public List<String> getOptions() {
            return OPTIONS;
        }
    }
}