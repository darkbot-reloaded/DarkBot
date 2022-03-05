package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.drawables.DevStuffDrawer;
import com.github.manolo8.darkbot.gui.drawables.DynamicEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.HeroDrawer;
import com.github.manolo8.darkbot.gui.drawables.InfosDrawer;
import com.github.manolo8.darkbot.gui.drawables.StaticEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.StatsDrawer;
import com.github.manolo8.darkbot.gui.drawables.TrailDrawer;
import com.github.manolo8.darkbot.gui.drawables.ZonesDrawer;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DrawableHandler extends FeatureHandler<Drawable> {
    private static final Class<?>[] NATIVE =
            new Class[]{ZonesDrawer.class, InfosDrawer.class, TrailDrawer.class, StaticEntitiesDrawer.class,
                    DynamicEntitiesDrawer.class, HeroDrawer.class, DevStuffDrawer.class, StatsDrawer.class};

    private List<Drawable> drawables;

    public void draw(MapGraphics mg) {
        synchronized (Main.UPDATE_LOCKER) {
            for (Drawable drawable : drawables) {
                drawable.onDraw(mg);
            }
        }
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
