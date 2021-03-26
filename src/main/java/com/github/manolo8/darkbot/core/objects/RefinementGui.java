package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.OreAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.manolo8.darkbot.Main.API;

public class RefinementGui extends Gui implements API.Singleton {

    private final ObjArray basicOresArr      = ObjArray.ofArrObj();
    private final ObjArray upgradableOresArr = ObjArray.ofArrObj();

    private final List<Ore> basicOres      = new ArrayList<>();
    private final List<Ore> upgradableOres = new ArrayList<>();

    public Ore get(OreType type) {
        List<Ore> oresListRef = type.attribute == OreType.Attribute.BASIC ? basicOres : upgradableOres;

        for (Ore ore : oresListRef) {
            if (ore.name.endsWith(type.name().toLowerCase())) return ore;
        }

        return null;
    }

    public int getAmount(OreAPI.Ore ore) {
        return Stream.concat(basicOres.stream(), upgradableOres.stream())
                .filter(o -> o.name.endsWith(ore.name().toLowerCase()))
                .map(o -> o.amount)
                .findFirst().orElse(-1);
    }

    @Override
    public void update() {
        super.update();
        if (address == 0) return;

        basicOresArr.update(API.readMemoryLong(getElementsList(37), 184));
        upgradableOresArr.update(API.readMemoryLong(getElementsList(31), 184));

        basicOresArr.sync(basicOres, Ore::new, null);
        upgradableOresArr.sync(upgradableOres, Ore::new, null);
    }

    public static class Ore extends UpdatableAuto {
        private String name, fuzzyName;
        private int amount;

        public String getName() {
            return name;
        }

        public String getFuzzyName() {
            return fuzzyName;
        }

        public int getAmount() {
            return amount;
        }

        @Override
        public void update() {
            amount = API.readMemoryInt(address, 0xf0);
        }

        @Override
        public void update(long address) {
            if (address != this.address || name == null || !name.contains("ore")) {
                name = API.readMemoryString(address, 184);

                if (name != null && !name.isEmpty()) {
                    String processedName = name.replace("ore_", "");
                    fuzzyName = processedName.substring(0, 1).toUpperCase(Locale.ROOT) + processedName.substring(1);
                }
            }
            super.update(address);
        }
    }

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
