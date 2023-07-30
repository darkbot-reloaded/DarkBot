package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.OreAPI;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.github.manolo8.darkbot.Main.API;

public class RefinementGui extends Gui implements API.Singleton {

    private final ObjArray basicOresArr      = ObjArray.ofArrObj();
    private final ObjArray upgradableOresArr = ObjArray.ofArrObj();
    private final ObjArray upgradedLabArr    = ObjArray.ofArrObj();

    private final List<Ore> basicOres      = new ArrayList<>();
    private final List<Ore> upgradableOres = new ArrayList<>();
    private final List<Ore> upgradedLab = new ArrayList<>();

    /**
     * Use {@link OreAPI#getAmount(OreAPI.Ore)}
     */
    @Deprecated(forRemoval = true)
    public Ore get(OreType type) {
        List<Ore> oresListRef = type.attribute == OreType.Attribute.BASIC ? basicOres : upgradableOres;

        for (Ore ore : oresListRef) {
            if (ore.name.endsWith(type.name().toLowerCase())) return ore;
        }

        return null;
    }

    public Ore get(OreAPI.Ore type) {
        List<Ore> oresListRef = type.isUpgradable() ? upgradableOres : basicOres;

        for (Ore ore : oresListRef) {
            if (ore.name.endsWith(type.name().toLowerCase())) return ore;
        }

        return null;
    }

    public int getAmount(OreAPI.Ore ore) {
        return Stream.concat(basicOres.stream(), upgradableOres.stream())
                .filter(o -> o.name.endsWith(ore.getName()))
                .map(o -> o.amount)
                .findFirst().orElse(-1);
    }

    @Override
    public void update() {
        super.update();
        if (address == 0) return;

        basicOresArr.update(API.readMemoryLong(getElementsList(37), 184));
        upgradableOresArr.update(API.readMemoryLong(getElementsList(31), 184));
        upgradedLabArr.update(API.readMemoryLong(getElementsList(32), 184));

        basicOresArr.sync(basicOres, Ore::new);
        upgradableOresArr.sync(upgradableOres, Ore::new);
        upgradedLabArr.sync(upgradedLab, Ore::new);
    }

    @Getter
    public static class Ore extends Auto {
        private String name, fuzzyName, upgradedOre;
        private int amount;

        @Override
        public void update() {
            amount = API.readMemoryInt(address, 0xf0);
        }

        @Override
        public void update(long address) {
            if (address != this.address || name == null || (!name.contains("ore") && !name.contains("lab"))) {
                name = API.readMemoryString(address, 184);

                if (name != null && !name.isEmpty()) {
                    String processedName;
                    if (name.contains("lab_")) {
                        processedName = API.readMemoryString(address,0x108).toLowerCase(Locale.ROOT);
                        // int upgradeOreId = API.readMemoryInt(address, 0x118, 0xB8);
                        upgradedOre = API.readMemoryString(address, 0x118, 0xC8).replace("lab_effect_", "");
                    }else{
                        processedName = name.replace("ore_", "");
                    }
                    fuzzyName = processedName.substring(0, 1).toUpperCase(Locale.ROOT) + processedName.substring(1);
                }
            }
            super.update(address);
        }
    }

    /**
     * @deprecated use {@link eu.darkbot.api.managers.OreAPI.Ore}
     */
    @Deprecated(forRemoval = true)
    public enum OreType {
        PROMETIUM(Attribute.BASIC),
        ENDURIUM(Attribute.BASIC),
        TERBIUM(Attribute.BASIC),
        XENOMIT(Attribute.BASIC),
        PALLADIUM(Attribute.BASIC),
        PROMETID(Attribute.UPGRADABLE),
        DURANIUM(Attribute.UPGRADABLE),
        PROMERIUM(Attribute.UPGRADABLE),
        SEPROM(Attribute.UPGRADABLE),
        OSMIUM(Attribute.UPGRADABLE);

        private enum Attribute {
            BASIC, UPGRADABLE
        }

        private final Attribute attribute;

        OreType(Attribute attribute) {
            this.attribute = attribute;
        }

        public boolean isUpgradable() {
            return attribute == Attribute.UPGRADABLE;
        }
    }
}
