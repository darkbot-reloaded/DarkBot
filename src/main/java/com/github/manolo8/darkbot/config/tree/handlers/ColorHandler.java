package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Col;
import eu.darkbot.impl.config.DefaultHandler;

import java.awt.*;
import java.lang.reflect.Field;

public class ColorHandler extends DefaultHandler<Color> {

    private final boolean alpha;

    public static ColorHandler of(Field field) {
        Col col = field.getAnnotation(Col.class);
        return new ColorHandler(field, col == null || col.alpha());
    }

    public ColorHandler(boolean alpha) {
        this(null, alpha);
    }

    public ColorHandler(Field field, boolean alpha) {
        super(field);
        metadata.put("alpha", this.alpha = alpha);
    }

    @Override
    public Color validate(Color color) {
        if (!alpha && color.getAlpha() != 255)
            color = new Color(color.getRGB(), false);
        return color;
    }
}
