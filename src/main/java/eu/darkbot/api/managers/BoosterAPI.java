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
     * @return The {@code List} of all Boosters currently active on ship
     */
    List<? extends Booster> getBoosters();

    /**
     * Booster representation of a type of boost and the amount of boost given.
     *
     * Keep in mind one booster may have many sub-boosters, some may last longer
     * and some may last shorter.
     *
     * The sub-boosters are not exposed in the API, instead the Booster is responsible
     * for representing the whole booster, amount will be the sum of all boosters and
     * remaining time will be the shortest of the sub-boosters.
     */
    interface Booster {
        /**
         * @return the amount of boost in percentage, +10% = 0.1
         */
        double getAmount();

        /**
         * @return time in which the current booster will expire in seconds.
         *         Keep in mind that the time running out doesn't necessarily mean
         *         the amount drops down to 0.
         *         Example: amount could be 15% (combination of 10% + 5%) and remaining time be 10s.
         *           After the 10 seconds, it could be that amount is 10% & remaining time is hours.
         */
        double getRemainingTime();

        /**
         * @return The in-game name of this specific booster
         */
        String getName();

        /**
         * @return A small name (typically about 3 chars) for this type of booster.
         */
        default String getSmall() {
            return getType().getSmall(getCategory());
        }

        /**
         * @return A color usually associated by users to this type of booster
         */
        default Color getColor() {
            return getType().getColor();
        }

        /**
         * @return The type of booster this is
         * @see Type
         */
        default Type getType() {
            return Type.of(getCategory());
        }

        /**
         * The string version of the category, prefer using {@link #getType()} instead
         *
         * @return The string version of category type of this booster
         */
        String getCategory();
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
            String getSmall(String category) {
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

        String getSmall(String category) {
            return this.small;
        }

        Color getColor() {
            return this.color;
        }

        private static Type of(String category) {
            for (Type cat : Type.values()) {
                if (cat.name().equalsIgnoreCase(category)) return cat;
            }
            return UNKNOWN;
        }
    }
}
