package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.core.manager.PetManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PetGearSupplier extends OptionList<Integer> {

    public static List<PetManager.Gear> GEARS = Collections.emptyList();
    private static final List<String> DEFAULT_OPTIONS =
            IntStream.of(1, 2, 4, 6, 10, 12).mapToObj(Gears::getName).collect(Collectors.toList());

    @Override
    public Integer getValue(String text) {
        if (text == null) return null;
        for (PetManager.Gear gear : GEARS) if (text.equalsIgnoreCase(gear.name)) return gear.id;
        //noinspection StringEquality
        return Arrays.stream(Gears.values())
                .filter(e -> e.name == text)
                .map(Gears::getId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getText(Integer value) {
        for (PetManager.Gear gear : GEARS) if (value == gear.id) return gear.name;
        return Gears.getName(value);
    }

    @Override
    public List<String> getOptions() {
        return GEARS.isEmpty() ? DEFAULT_OPTIONS : GEARS.stream().map(g -> g.name).collect(Collectors.toList());
    }

    public enum Gears {
        PASSIVE("Passive mode"),
        GUARD("Guard mode"),
        DESTROYER("Unknown id 3, Destroyer"),
        LOOTER("Auto-looter"),
        RESOURCE("Auto-resource collector"),
        ENEMY_LOCATOR("Enemy locator"),
        RESOURCE_LOCATOR("Resource locator"),
        TRADER("Cargo trader"),
        REPAIR("P.E.T. repairer"),
        KAMIKAZE("Kamikaze Detonator"),
        COMBO_REPAIR("Combo Ship Repair Gear"),
        COMBO_GUARD("Combo Guard Mode Gear"),
        DESTROYER_2("Unknown id 13, Destroyer"),
        SACRIFICIAL("Sacrificial Flame"),
        PET_TARGET("Retargeting P.E.T. Gear"),
        HP_LINK("HP Link P.E.T. gear");

        String name;
        Gears(String name) {
            this.name = name;
        }

        public int getId() {
            return ordinal() + 1;
        }

        public static String getName(Integer id) {
            if (id == null) return null;
            if (values().length > id) return values()[id - 1].name;
            return "Unknown gear " + id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
