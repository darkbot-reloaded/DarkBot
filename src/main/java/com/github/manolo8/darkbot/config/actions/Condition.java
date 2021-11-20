package com.github.manolo8.darkbot.config.actions;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.PluginAPI;
import org.jetbrains.annotations.NotNull;

public interface Condition extends Value<Condition.Result>, eu.darkbot.api.config.types.Condition {

    @Override
    default @NotNull eu.darkbot.api.config.types.Condition.Result get(PluginAPI pluginAPI) {
        Result result = get(pluginAPI.requireInstance(Main.class));

        return eu.darkbot.api.config.types.Condition.Result.values()[result.ordinal()];
    }

    /**
     * @deprecated Use {@link #get(PluginAPI)} instead
     */
    @Deprecated
    @NotNull Result get(Main main);

    enum Result {
        ALLOW, DENY, ABSTAIN;

        public static Result fromBoolean(Boolean state) {
            return state == null ? ABSTAIN : state ? ALLOW : DENY;
        }

        public boolean toBoolean() {
            return !this.equals(DENY);
        }

        public boolean allows() {
            return this == ALLOW;
        }

        public boolean denies() {
            return this == DENY;
        }

        public boolean abstains() {
            return this == ABSTAIN;
        }

        public boolean hasResult() {
            return this != ABSTAIN;
        }
    }

}
