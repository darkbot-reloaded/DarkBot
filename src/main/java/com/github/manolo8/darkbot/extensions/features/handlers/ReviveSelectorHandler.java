package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.core.manager.GuiManager;
import eu.darkbot.api.extensions.selectors.PrioritizedSupplier;
import eu.darkbot.api.extensions.selectors.ReviveSelector;
import eu.darkbot.api.game.enums.ReviveLocation;

public class ReviveSelectorHandler extends AbstractSelectorHandler<ReviveSelector, ReviveLocation, PrioritizedSupplier<ReviveLocation>> {

    private static final Class<?>[] NATIVE = new Class[]{GuiManager.DefaultReviveSupplier.class};

    public ReviveSelectorHandler() {
        super(ReviveSelector::getReviveLocationSupplier);
    }

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }
}
