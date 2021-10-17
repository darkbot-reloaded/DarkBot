package com.github.manolo8.darkbot.config.actions;

import com.github.manolo8.darkbot.Main;
import org.jetbrains.annotations.Nullable;

public interface Value<R> {

    @Nullable R get(Main main);

    static <T> T get(Value<T> val, Main main) {
        if (val == null) return null;
        return val.get(main);
    }

    static boolean allows(Value<Condition.Result> val, Main main) {
        Condition.Result res = get(val, main);
        return res != null && res.allows();
    }

}
