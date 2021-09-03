package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.TagDefault;
import eu.darkbot.api.config.annotations.Tag;
import eu.darkbot.api.config.types.PlayerTag;
import eu.darkbot.impl.config.DefaultHandler;

import java.lang.reflect.Field;

public class PlayerTagHandler extends DefaultHandler<PlayerTag> {

    public static PlayerTagHandler of(Field field) {
        Tag tag = field.getAnnotation(Tag.class);
        return new PlayerTagHandler(field, TagDefault.valueOf(tag.value().name()));
    }

    public static PlayerTagHandler ofLegacy(Field field) {
        com.github.manolo8.darkbot.config.types.Tag legacyTag =
                field.getAnnotation(com.github.manolo8.darkbot.config.types.Tag.class);

        return new PlayerTagHandler(field, legacyTag.value());
    }

    public static PlayerTagHandler fallback(Field field) {
        return new PlayerTagHandler(field, TagDefault.UNSET);
    }

    public PlayerTagHandler(TagDefault fallback) {
        this(null, fallback);
    }

    public PlayerTagHandler(Field field, TagDefault fallback) {
        super(field);
        metadata.put("tagDefault", fallback);
    }

}
