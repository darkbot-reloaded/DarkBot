package com.github.manolo8.darkbot.config.actions;

import com.github.manolo8.darkbot.config.actions.parser.Values;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SyntaxException extends Exception {

    private static final String[] EMPTY = new String[]{};

    private final String at;
    private final String[] chars;
    private final List<Values.Meta<?>> metadatas;
    private boolean singleMeta = false;

    public SyntaxException(String message, String at) {
        this(message, at, (List<Values.Meta<?>>) null, (String[]) null);
    }

    public <E extends Enum<E>> SyntaxException(String message, String at, Class<E> metadatas) {
        this(message, at, (List<Values.Meta<?>>) null, Arrays.stream(metadatas.getEnumConstants())
                .map(Objects::toString).toArray(String[]::new));
    }

    public SyntaxException(String message, String at, Values.Meta<?> meta, String... chars) {
        this(message, at, meta == null ? null : Collections.singletonList(meta), chars);
        singleMeta = true;
    }

    public SyntaxException(String message, String at, List<Values.Meta<?>> metas, String... chars) {
        super(message);
        this.at = at == null ? "" : at;
        this.chars = chars == null ? EMPTY : chars;
        this.metadatas = metas == null ? Collections.emptyList() : metas;
    }

    public String getAt() {
        return at;
    }

    public @NotNull String[] getExpected() {
        return chars;
    }

    public @NotNull List<Values.Meta<?>> getMetadata() {
        return metadatas;
    }

    public boolean isSingleMeta() {
        return singleMeta;
    }

}
