package com.github.manolo8.darkbot.config.actions;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.Nullable;

public interface Value<R> {

    @Nullable R get(Main api);

    static <T> T get(Value<T> val, Main main) {
        if (val == null) return null;
        return val.get(main);
    }

    static boolean allows(Value<Condition.Result> val, PluginAPI api) {
        Condition.Result res = get(val, api);
        return res != null && res.allows();
    }

    static <T> T get(Value<T> val, PluginAPI api) {
        return get(val, api.requireInstance(Main.class));
    }

}
