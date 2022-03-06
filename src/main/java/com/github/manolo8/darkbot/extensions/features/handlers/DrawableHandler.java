package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.drawables.DevStuffDrawer;
import com.github.manolo8.darkbot.gui.drawables.DynamicEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.HeroDrawer;
import com.github.manolo8.darkbot.gui.drawables.InfosDrawer;
import com.github.manolo8.darkbot.gui.drawables.ConstantEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.StatsDrawer;
import com.github.manolo8.darkbot.gui.drawables.TrailDrawer;
import eu.darkbot.api.extensions.Drawable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DrawableHandler extends FeatureHandler<Drawable> {
    private static final Class<?>[] NATIVE =
            new Class[]{InfosDrawer.class, TrailDrawer.class, ConstantEntitiesDrawer.class,
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
        this.drawables = features.map(featureRegistry::getFeature)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
