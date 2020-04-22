package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.manolo8.darkbot.Main.API;

public class BoosterProxy extends Updatable {
    public List<Booster> boosters = new ArrayList<>();

    private ObjArray boostersArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.FIX;

        boostersArr.update(API.readMemoryLong(data + 0x48));
        boostersArr.sync(boosters, Booster::new, null);
    }

    public static class Booster extends UpdatableAuto {
        public double amount, cd;
        public String category, name;
        public BoosterCategory cat;

        private ObjArray subBoostersArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            this.category = API.readMemoryString(address, 0x20);
            this.cat      = BoosterCategory.of(category);
            this.name     = API.readMemoryString(address, 0x40); //0x48 description;
            this.amount   = API.readMemoryDouble(address + 0x50);
            this.subBoostersArr.update(API.readMemoryLong(address + 0x30));

            double current, min = 0;
            for (int i = 0; i < subBoostersArr.getSize(); i++) {
                current = API.readMemoryDouble(subBoostersArr.get(i), 0x30, 0x38);
                if (min == 0 || (current != 0 && current < min)) min = current;
            }
            this.cd = min;
        }

        public String toSimpleString() {
            long hours = Math.round(cd / 3600);
            String time = hours < 100 ? String.format("%2d", hours) : " ∞";
            return time + "h " + (int) amount + "% " + cat.getSmall(category);
        }

        public Color getColor() {
            return cat.getColor();
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

        private String small;
        private Color color;

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
}
