package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.core.manager.PetManager;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.game.enums.PetGear;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PetGears implements Dropdown.Options<PetGear> {

    private static final List<PetGear> DEFAULT_OPTIONS = Arrays.asList(PetGear.PASSIVE, PetGear.GUARD,
            PetGear.LOOTER, PetGear.ENEMY_LOCATOR, PetGear.KAMIKAZE);

    private final List<PetGear> gears;

    public PetGears(PetManager pet) {
        this.gears = pet.getGears();
    }

    @Override
    public Collection<PetGear> options() {
        return gears.isEmpty() ? DEFAULT_OPTIONS : gears;
    }

    @Override
    public @NotNull String getText(@Nullable PetGear value) {
        if (value == null) return "";
        return value.getName();
    }

}
