package com.github.manolo8.darkbot.config.actions;

import com.github.manolo8.darkbot.Main;
import org.jetbrains.annotations.NotNull;

public interface Condition extends Value<Condition.Result> {

    @NotNull Result getValue(Main main);

    enum Result {
        ALLOW, DENY, ABSTAIN;

        public static Result fromBoolean(Boolean state) {
            return state == null ? ABSTAIN : state ? ALLOW : DENY;
        }

        public boolean toBoolean() {
            return !this.equals(DENY);
        }

        public boolean hasResult() {
            return !this.equals(ABSTAIN);
        }
    }

}
