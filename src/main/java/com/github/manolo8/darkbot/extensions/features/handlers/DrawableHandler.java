package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.drawables.ConstantEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.DevStuffDrawer;
import com.github.manolo8.darkbot.gui.drawables.DynamicEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.HeroDrawer;
import com.github.manolo8.darkbot.gui.drawables.InfosDrawer;
import com.github.manolo8.darkbot.gui.drawables.OverlayDrawer;
import com.github.manolo8.darkbot.gui.drawables.TrailDrawer;
import com.github.manolo8.darkbot.gui.drawables.ZonesDrawer;
import eu.darkbot.api.extensions.Draw;
import eu.darkbot.api.extensions.Drawable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DrawableHandler extends FeatureHandler<Drawable> {
    private static final Class<?>[] NATIVE =
            new Class[]{ZonesDrawer.class, InfosDrawer.class, TrailDrawer.class, ConstantEntitiesDrawer.class,
                    DynamicEntitiesDrawer.class, HeroDrawer.class, DevStuffDrawer.class, OverlayDrawer.class};

    private List<Drawable> drawables;

    public List<Drawable> getDrawables() {
        return drawables;
    }

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }

    @Override
    public void update(Stream<FeatureDefinition<Drawable>> features) {
        List<Drawable> drawables = features.map(featureRegistry::getFeature)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.nullsLast(Comparator.comparing(this::getStep))
                        .thenComparing(this::getAttach))
                .collect(Collectors.toList());

        if (drawables.isEmpty()) return;

        Draw previous = drawables.get(0).getClass().getAnnotation(Draw.class);
        for (int i = 1; i < drawables.size(); i++) {
            Draw draw = drawables.get(i).getClass().getAnnotation(Draw.class);

            if (previous != null && draw != null &&
                    previous.value() == draw.value() &&
                    previous.attach() == Draw.Attach.REPLACE &&
                    draw.attach() == Draw.Attach.REPLACE) {
                drawables.remove(i - 1);
                i--;
            }
        }

        this.drawables = drawables;
    }

    private @Nullable Draw.Stage getStep(Drawable drawable) {
        Draw draw = drawable.getClass().getAnnotation(Draw.class);
        return draw == null ? null : draw.value();
    }

    private @NotNull Draw.Attach getAttach(Drawable drawable) {
        Draw draw = drawable.getClass().getAnnotation(Draw.class);
        return draw == null || draw.attach() == null ? Draw.Attach.AFTER : draw.attach();
    }

}
