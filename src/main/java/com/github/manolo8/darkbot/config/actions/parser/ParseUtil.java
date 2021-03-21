package com.github.manolo8.darkbot.config.actions.parser;

import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;

import java.util.Arrays;

public class ParseUtil {

    public static String separate(String[] parts, Class<? extends Value<?>> val, String... expected) throws SyntaxException {
        return separate(parts, Values.getMeta(val), expected);
    }

    public static String separate(String[] parts, Values.Meta<?> meta, String... expected) throws SyntaxException {
        if (parts.length != 2)
            throw new SyntaxException("Missing separator in '" + meta.getName() + "'", "", meta, expected);
        return parts[1].trim();
    }

    public static String separate(String str, Class<? extends Value<?>> val, String... expected) throws SyntaxException{
        return separate(str, Values.getMeta(val), expected);
    }

    public static String separate(String str, Values.Meta<?> meta, String... expected) throws SyntaxException{
        String match = Arrays.stream(expected).filter(str::startsWith).findFirst().orElse(null);
        if (str.isEmpty() || match == null)
            throw new SyntaxException("Missing separator in '" + meta.getName() + "'", str, meta, expected);
        return str.substring(match.length()).trim();
    }

}
