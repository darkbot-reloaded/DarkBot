package com.github.manolo8.darkbot.config.actions;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyntaxException extends Exception {

    private final String at;
    private final String expected;

    public SyntaxException(String message, String at) {
        this(message, at, (Stream<String>) null);
    }

    public <E extends Enum<E>> SyntaxException(String message, String at, Class<E> expected) {
        this(message, at, Arrays.stream(expected.getEnumConstants()));
    }

    public SyntaxException(String message, String at, String... expected) {
        this(message, at, Arrays.stream(expected));
    }

    public SyntaxException(String message, String at, Stream<?> expected) {
        super(message);
        this.at = at;
        this.expected = expected == null ? null :
                expected.map(e -> "'" + e + "'").collect(Collectors.joining(", "));
    }

    public String getAt() {
        return at;
    }

    public String getExpected() {
        return expected;
    }

    public int getStart(String original) {
        return original.lastIndexOf(at);
    }

}
