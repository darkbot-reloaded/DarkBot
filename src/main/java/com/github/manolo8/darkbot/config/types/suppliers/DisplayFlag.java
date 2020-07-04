package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.utils.I18n;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum DisplayFlag {
    USERNAMES, HERO_NAME, HP_SHIELD_NUM, ZONES, STATS_AREA, GROUP_AREA, GROUP_NAMES, BOOSTER_AREA, SORT_BOOSTERS;

    public String getName() {
        return I18n.getOrDefault("config.bot_settings.display.toggle." + name().toLowerCase(Locale.ROOT), name());
    }

    public String getShortName() {
        return I18n.getOrDefault("config.bot_settings.display.toggle." + name().toLowerCase(Locale.ROOT) + ".short", name());
    }

    public String getDescription() {
        return I18n.getOrDefault("config.bot_settings.display.toggle." + name().toLowerCase(Locale.ROOT) + ".desc", null);
    }

    public static class Supplier extends OptionList<DisplayFlag> {
        private static final List<String> OPTIONS =
                Arrays.stream(DisplayFlag.values()).map(DisplayFlag::getName).collect(Collectors.toList());

        @Override
        public DisplayFlag getValue(String text) {
            for (DisplayFlag df : DisplayFlag.values()) {
                if (df.getName().equals(text)) return df;
            }
            return null;
        }

        @Override
        public String getText(DisplayFlag value) {
            return value.getName();
        }

        @Override
        public String getShortText(DisplayFlag value) {
            return value.getShortName();
        }

        @Override
        public String getTooltipFromVal(DisplayFlag value) {
            return value.getDescription();
        }

        @Override
        public List<String> getOptions() {
            return OPTIONS;
        }
    }
}
