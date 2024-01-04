package com.github.manolo8.darkbot.config.actions.parser;

import com.github.manolo8.darkbot.config.actions.Value;

public class ParseResult<T> {
    public final Value<T> value;
    public final Class<T> type;

    public ParseResult(Value<T> value, Class<T> type) {
        this.value = value;
        this.type = type;
    }

}