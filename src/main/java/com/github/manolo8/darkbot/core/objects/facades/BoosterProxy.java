package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.managers.BoosterAPI;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.Main.UPDATE_LOCKER;

public class BoosterProxy extends Updatable implements BoosterAPI {
    public List<Booster> boosters = new ArrayList<>();

    private final ObjArray boostersArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        boostersArr.update(API.readMemoryLong(data + 0x48));
        synchronized (UPDATE_LOCKER) {
            boostersArr.sync(boosters, Booster::new, null);
        }
    }

    public static class Booster extends UpdatableAuto implements BoosterAPI.Booster {
        public double amount, cd;
        public String category, name;
        public BoosterCategory cat;

        private final ObjArray subBoostersArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            this.category = API.readMemoryString(address, 0x20);
            this.cat      = BoosterCategory.of(category);
            this.name     = API.readMemoryString(address, 0x40); //0x48 description;
            this.amount   = API.readMemoryDouble(address + 0x50);
            this.subBoostersArr.update(API.readMemoryLong(address + 0x30));

            double min = Double.POSITIVE_INFINITY, curr;
            for (int i = 0; i < subBoostersArr.getSize(); i++)
                if ((curr = API.readMemoryDouble(subBoostersArr.get(i), 0x30, 0x38)) > 0 && curr < min) min = curr;
            this.cd = min;
        }

        public String toSimpleString() {
            return String.format("%3s %2.0f%% %s", Time.secondsToShort(cd), amount, cat.getSmall(category));
        }

        public Color getColor() {
            return cat.getColor();
        }

        @Override
        public double getAmount() {
            return amount;
        }

        @Override
        public double getRemainingTime() {
            return cd;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private enum BoosterCategory {
        ABILITY_COOLDOWN_TIME   ("CD"    , new Color(0xFFC000)),
        DAMAGE                  ("DMG"   , new Color(0xFD0400)),
        EXPERIENCE_POINTS       ("EXP"   , new Color(0xF77800)),
        HITPOINTS               ("HP"    , new Color(0x049104)),
        HONOUR_POINTS           ("HON"   , new Color(0xFF8080)),
        REPAIR                  ("REP"   , new Color(0xA93DE4)),
        COLLECT_RESOURCES       ("RES"   , new Color(0xEAD215)),
        SHIELD                  ("SHD"   , new Color(0x69EBFF)),
        SHIELD_REGENERATION     ("SHDR"  , new Color(0x3B64BD)),
        AMOUNT                  ("AMT"   , new Color(0xFFCC00)),
        COLLECT_RESOURCES_NEWBIE("DBL"   , new Color(0xFFF3CF)),
        CHANCE                  ("CHN"   , new Color(0xFFD100)),
        EVENT_AMOUNT            ("EVT AM", new Color(0x05B6E3)),
        EVENT_CHANCE            ("EVT CH", new Color(0x00C6EE)),
        SPECIAL_AMOUNT          ("SP AM" , new Color(0xFFFFFF)),
        UNKNOWN                 ("?"     , new Color(0x808080)) {
            @Override
            public String getSmall(String category) {
                return Arrays.stream(category.split("_"))
                        .map(str -> str.length() <= 3 ? str : str.substring(0, 3))
                        .collect(Collectors.joining(" "));
            }
        };

        private final String small;
        private final Color color;

        BoosterCategory(String small, Color color) {
            this.small = small;
            this.color = color;
        }

        public String getSmall(String category) {
            return this.small;
        }
        public Color getColor() {
            return this.color;
        }

        public static BoosterCategory of(String category) {
            for (BoosterCategory cat : BoosterCategory.values()) {
                if (cat.name().equalsIgnoreCase(category)) return cat;
            }
            return UNKNOWN;
        }

    }

    @Override
    public List<Booster> getBoosters() {
        return boosters;
    }
}
