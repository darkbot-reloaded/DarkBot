package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.modules.utils.AttackAPIImpl;
import eu.darkbot.api.extensions.selectors.LaserSelector;
import eu.darkbot.api.extensions.selectors.PrioritizedSupplier;
import eu.darkbot.api.game.items.SelectableItem;

public class LaserSelectorHandler extends AbstractSelectorHandler<LaserSelector,
        SelectableItem.Laser, PrioritizedSupplier<SelectableItem.Laser>> {

    private static final Class<?>[] NATIVE = new Class[]{AttackAPIImpl.DefaultLaserSupplier.class};

    public LaserSelectorHandler() {
        super(LaserSelector::getLaserSupplier);
    }

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }
}
