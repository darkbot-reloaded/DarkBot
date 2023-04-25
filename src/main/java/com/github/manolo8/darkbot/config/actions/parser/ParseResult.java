package com.github.manolo8.darkbot.config.actions.parser;

import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import eu.darkbot.api.config.types.Condition;

public class ParseResult<T> {
    public final Value<T> value;
    public final Class<T> type;
    public final String leftover;

    public ParseResult(Value<T> value, Class<T> type, String leftover) {
        this.value = value;
        this.type = type;
        this.leftover = leftover;
    }

    public <C extends Value<?>> Condition asCondition(String original, Class<C> caller) throws SyntaxException {
        if (!(value instanceof Condition))
            throw new SyntaxException("Error: Expected boolean condition", original, Values.getMeta(caller));
        return (Condition) value;
    }
}