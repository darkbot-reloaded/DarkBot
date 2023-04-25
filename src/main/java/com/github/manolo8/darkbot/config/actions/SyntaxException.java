package com.github.manolo8.darkbot.config.actions;

import com.github.manolo8.darkbot.config.actions.parser.Values;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SyntaxException extends RuntimeException {

    private static final String[] EMPTY = new String[]{};

    private final String at;
    private final int atIdx;
    private final String[] chars;
    private final List<Values.Meta<?>> metadatas;
    private boolean singleMeta = false;

    public SyntaxException(String message, String at) {
        this(message, at, (List<Values.Meta<?>>) null, (String[]) null);
    }

    public SyntaxException(String message, int atIdx, String... chars) {
        super(message);
        this.at = "";
        this.atIdx = atIdx;
        this.chars = chars == null ? EMPTY : chars;
        this.metadatas = Collections.emptyList();
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
        this.atIdx = -1;
        this.chars = chars == null ? EMPTY : chars;
        this.metadatas = metas == null ? Collections.emptyList() : metas;
    }

    public String getAt() {
        return at;
    }

    public int getIdx(String originalString) {
        if (atIdx != -1) return atIdx;

        return Math.max(0, originalString.lastIndexOf(at));
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
