package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.api.DarkBoatAdapter;
import com.github.manolo8.darkbot.core.api.DarkCefAdapter;
import com.github.manolo8.darkbot.core.api.DarkMemAdapter;
import com.github.manolo8.darkbot.core.api.NoopAPIAdapter;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.config.annotations.Configuration;

import java.util.function.Function;


@Configuration("browser_api")
public enum BrowserApi {
    DARK_BOAT(DarkBoatAdapter::of),
    NO_OP_API(NoopAPIAdapter::of),
    DARK_MEM_API(DarkMemAdapter::of),
    DARK_CEF_API(DarkCefAdapter::of);

    private final Function<StartupParams, IDarkBotAPI> constructor;

    public IDarkBotAPI getInstance(StartupParams params) {
        return this.constructor.apply(params);
    }

    BrowserApi(Function<StartupParams, IDarkBotAPI> constructor) {
        this.constructor = constructor;
    }

}