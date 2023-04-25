package com.github.manolo8.darkbot.config.actions;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.PluginAPI;
import org.jetbrains.annotations.NotNull;

@Deprecated
public interface Condition extends Value<Condition.Result>, eu.darkbot.api.config.types.Condition {

    /**
     * @deprecated Use {@link #get(PluginAPI)} instead
     */
    @Deprecated
    default @NotNull Result get(Main main) {
        return Result.values()[get(main.pluginAPI).ordinal()];
    }

    @Deprecated
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
