package eu.darkbot.api.managers;

import eu.darkbot.api.API;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API for boosters (seeing which boosters are currently active, how much time they have, etc.)
 */
public interface BoosterAPI extends API.Singleton {
    /**
     * @return {@code List} of all Boosters currently active on ship
     */
    List<? extends Booster> getBoosters();

    /**
     * Booster object for {@link BoosterAPI}
     */
    interface Booster {
        /**
         * @return booster percentage
         */
        double getAmount();

        /**
         * @return booster time in seconds
         */
        double getRemainingTime();

        String getCategory();
        String getName();

        default Type getType() {
            return Type.of(getCategory());
        }

    }

    /**
     * The types of all boosters available
     */
    enum Type {
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

        Type(String small, Color color) {
            this.small = small;
            this.color = color;
        }

        public String getSmall(String category) {
            return this.small;
        }
        public Color getColor() {
            return this.color;
        }

        /**
         * Gets the specific {@code Type} that matches with the {@code category}
         *
         * @param category  the category of the booster from {@code getCategory()}
         * @return the {@code Type} that matches with the {@code category}
         * @see Booster#getCategory()
         */
        public static Type of(String category) {
            for (Type cat : Type.values()) {
                if (cat.name().equalsIgnoreCase(category)) return cat;
            }
            return UNKNOWN;
        }
    }
}
