package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.api.BackpageAdapter;
import com.github.manolo8.darkbot.core.api.DarkMemAdapter;
import com.github.manolo8.darkbot.core.api.TanosAdapter;
import com.github.manolo8.darkbot.core.api.KekkaPlayerAdapter;
import com.github.manolo8.darkbot.core.api.NoopAPIAdapter;
import com.github.manolo8.darkbot.utils.OSUtil;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import eu.darkbot.api.config.annotations.Configuration;

import java.lang.reflect.Type;

@Configuration("browser_api")
public enum BrowserApi {
    KEKKA_PLAYER(KekkaPlayerAdapter.class),
    TANOS_API(TanosAdapter.class),
    BACKPAGE_ONLY(BackpageAdapter.class),
    NO_OP_API(NoopAPIAdapter.class),
    DARK_MEM_API(DarkMemAdapter.class);
    //DARK_CEF_API(DarkCefAdapter.class);

    public final Class<? extends IDarkBotAPI> clazz;

    BrowserApi(Class<? extends IDarkBotAPI> clazz) {
        this.clazz = clazz;
    }

    public static class BrowserApiDeserializer implements JsonDeserializer<BrowserApi> {

        @Override
        public BrowserApi deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String s = json.getAsString();
            if (s.startsWith("DARK_BOAT"))
                return OSUtil.getDefaultAPI();
            return BrowserApi.valueOf(s);
        }
    }
}


