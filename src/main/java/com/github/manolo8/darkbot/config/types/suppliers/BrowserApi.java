package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.api.*;
import eu.darkbot.api.config.annotations.Configuration;


@Configuration("browser_api")
public enum BrowserApi {
    DARK_BOAT(KekkaPlayerAdapter.class),
    DARK_BOAT_HOOK(KekkaPlayerHookAdapter.class),
    BACKPAGE_ONLY(BackpageAdapter.class),
    NO_OP_API(NoopAPIAdapter.class),
    DARK_MEM_API(DarkMemAdapter.class);
    //DARK_CEF_API(DarkCefAdapter.class);

    public final Class<? extends IDarkBotAPI> clazz;

    BrowserApi(Class<? extends IDarkBotAPI> clazz) {
        this.clazz = clazz;
    }

}