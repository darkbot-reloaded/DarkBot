package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.TagDefault;
import eu.darkbot.api.config.annotations.Tag;
import eu.darkbot.api.config.types.PlayerTag;
import eu.darkbot.impl.config.DefaultHandler;

import java.lang.reflect.Field;

public class PlayerTagHandler extends FieldDefaultHandler<PlayerTag> {

    private final TagDefault fallback;

    public PlayerTagHandler(Field field) {
        super(field);
        Tag tag = field.getAnnotation(Tag.class);
        com.github.manolo8.darkbot.config.types.Tag legacyTag =
                field.getAnnotation(com.github.manolo8.darkbot.config.types.Tag.class);

        if (tag != null) {
            fallback = TagDefault.valueOf(tag.value().name());
        } else if (legacyTag != null) {
            fallback = legacyTag.value();
        } else {
            fallback = TagDefault.UNSET;
        }
    }

    public PlayerTagHandler(TagDefault fallback) {
        this.fallback = fallback;
    }

    @Override
    public PlayerTag validate(PlayerTag playerTag) {
        return playerTag;
    }

    public TagDefault getFallback() {
        return fallback;
    }
}
