package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import eu.darkbot.api.extensions.selectors.LaserSelector;
import eu.darkbot.api.extensions.selectors.PrioritizedSupplier;
import eu.darkbot.api.game.items.SelectableItem;

public class PrioritizedLaserHandler extends AbstractPrioritizedHandler<LaserSelector,
        SelectableItem.Laser, PrioritizedSupplier<SelectableItem.Laser>> {

    private static final Class<?>[] NATIVE = new Class[]{};

    private PrioritizedSupplier<SelectableItem.Laser> defaultSupplier = () -> SelectableItem.Laser.LCB_10;

    public PrioritizedLaserHandler(FeatureRegistry featureRegistry) {
        super(featureRegistry, LaserSelector::getLaserSupplier);
    }

    @Override
    protected PrioritizedSupplier<SelectableItem.Laser> getDefaultSupplier() {
        return defaultSupplier;
    }

    public void setDefaultSupplier(PrioritizedSupplier<SelectableItem.Laser> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
    }

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }
}
