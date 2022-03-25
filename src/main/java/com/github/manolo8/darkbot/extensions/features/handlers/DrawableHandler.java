package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.drawables.ConstantEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.DevStuffDrawer;
import com.github.manolo8.darkbot.gui.drawables.DynamicEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.HeroDrawer;
import com.github.manolo8.darkbot.gui.drawables.InfosDrawer;
import com.github.manolo8.darkbot.gui.drawables.StatsDrawer;
import com.github.manolo8.darkbot.gui.drawables.TrailDrawer;
import com.github.manolo8.darkbot.gui.drawables.ZonesDrawer;
import eu.darkbot.api.extensions.Draw;
import eu.darkbot.api.extensions.Drawable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DrawableHandler extends FeatureHandler<Drawable> {
    private static final Class<?>[] NATIVE =
            new Class[]{ZonesDrawer.class, InfosDrawer.class, TrailDrawer.class, ConstantEntitiesDrawer.class,
                    DynamicEntitiesDrawer.class, HeroDrawer.class, DevStuffDrawer.class, StatsDrawer.class};

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
        List<Drawable> all = features.map(featureRegistry::getFeature)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        List<Drawable> drawables = new ArrayList<>();
        Set<Drawable> toRemove = new HashSet<>();

        all.removeIf(drawable -> {
            if (isNative(drawable)) {
                drawables.add(drawable);
                return true;
            }
            return false;
        });

        for (Drawable drawable : all) {
            Draw draw = drawable.getClass().getAnnotation(Draw.class);

            if (draw == null) drawables.add(drawable);
            else {
                Drawable n = drawables.stream()
                        .filter(d -> d.getClass() == NATIVE[draw.value().ordinal()])
                        .findAny().orElse(null);
                int index = drawables.indexOf(n);

                Draw.Stage stage = draw.stage();
                drawables.add(index + (stage == Draw.Stage.BEFORE ? 0 : 1), drawable);

                if (stage == Draw.Stage.REPLACE)
                    toRemove.add(n);
            }
        }

        toRemove.forEach(drawables::remove);
        this.drawables = drawables;
    }

    private boolean isNative(Object o) {
        for (Class<?> drawableClass : NATIVE)
            if (drawableClass == o.getClass())
                return true;

        return false;
    }
}
