package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.config.ConfigManager;
import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.api.DarkBoatAdapter;
import com.github.manolo8.darkbot.core.api.DarkCefAdapter;
import com.github.manolo8.darkbot.core.api.DarkMemAdapter;
import com.github.manolo8.darkbot.core.api.NativeApiAdapter;
import com.github.manolo8.darkbot.core.api.NoopApiAdapter;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.config.annotations.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@Configuration("browser_api")
public enum BrowserApi {
    DARK_BOAT(DarkBoatAdapter::new),
    NATIVE_API(NativeApiAdapter::new),
    NO_OP_API(NoopApiAdapter::new),
    DARK_MEM_API(DarkMemAdapter::new),
    DARK_CEF_API(DarkCefAdapter::new);

    private final BiFunction<StartupParams, BooleanSupplier, IDarkBotAPI> constructor;

    public IDarkBotAPI getInstance(StartupParams params, ConfigManager config) {
        return this.constructor.apply(params, () -> config.getConfig().BOT_SETTINGS.API_CONFIG.FULLY_HIDE_API);
    }

    BrowserApi(BiFunction<StartupParams, BooleanSupplier, IDarkBotAPI> constructor) {
        this.constructor = constructor;
    }

}