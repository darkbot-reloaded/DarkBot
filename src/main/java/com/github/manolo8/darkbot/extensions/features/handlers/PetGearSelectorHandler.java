package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.core.manager.PetManager;
import eu.darkbot.api.extensions.selectors.GearSelector;
import eu.darkbot.api.extensions.selectors.PetGearSupplier;
import eu.darkbot.api.game.enums.PetGear;

public class PetGearSelectorHandler extends AbstractSelectorHandler<GearSelector,
        PetGear, PetGearSupplier> {

    private static final Class<?>[] NATIVE = new Class[]{PetManager.DefaultGearSupplier.class};

    public PetGearSelectorHandler() {
        super(GearSelector::getGearSupplier);
    }

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }
}
