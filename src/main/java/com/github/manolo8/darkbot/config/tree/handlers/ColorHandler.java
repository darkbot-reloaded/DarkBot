package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Col;

import java.awt.*;
import java.lang.reflect.Field;

public class ColorHandler implements ValueHandler<Color> {

    private final boolean alpha;

    public ColorHandler(Field field) {
        Col col = field.getAnnotation(Col.class);
        this.alpha = col == null || col.alpha();
    }

    public ColorHandler(boolean alpha) {
        this.alpha = alpha;
    }

    @Override
    public Color validate(Color color) {
        if (!alpha && color.getAlpha() != 255)
            color = new Color(color.getRGB(), false);
        return color;
    }
}
