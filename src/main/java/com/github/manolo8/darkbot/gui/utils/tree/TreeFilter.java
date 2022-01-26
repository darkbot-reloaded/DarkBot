package com.github.manolo8.darkbot.gui.utils.tree;

import com.github.manolo8.darkbot.utils.StringQuery;
import eu.darkbot.api.config.ConfigSetting;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class TreeFilter implements Predicate<ConfigSetting<?>> {
    private final StringQuery search = new StringQuery();

    private final Map<ConfigSetting<?>, Visibility> visibilityCache = new IdentityHashMap<>();

    private enum Visibility {
        UNKNOWN(null, null),
        NO_MATCH_UNKNOWN(false, null),
        MATCH_UNKNOWN(true, null),

        UNKNOWN_NO_VISIBLE(null, false),
        NO_MATCH_NO_VISIBLE(false, false),
        MATCH_NO_VISIBLE(true, false), // Should be impossible to get this state

        UNKNOWN_VISIBLE(null, true),
        NO_MATCH_VISIBLE(false, true),
        MATCH_VISIBLE(true, true);

        private final Boolean matches;
        private final Boolean visible;

        Visibility(Boolean matches, Boolean visible) {
            this.matches = matches;
            this.visible = visible;
        }

        public Visibility setMatches(boolean result) {
            return Visibility.values()[this.ordinal() + (result ? 2 : 1)];
        }

        public Visibility setVisible(boolean result) {
            return Visibility.values()[this.ordinal() + (result ? 6 : 3)];
        }
    }

    public void setSearch(String query) {
        this.search.query = query;
        this.visibilityCache.clear();
    }

    public void invalidate() {
        visibilityCache.clear();
    }

    public boolean isUnfiltered() {
        return search.query == null || search.query.isEmpty();
    }

    @Override
    public boolean test(ConfigSetting<?> setting) {
        if (setting instanceof ToggleableNode && !((ToggleableNode) setting).isShown()) return false;
        if (isUnfiltered()) return true;

        return isVisible(setting);
    }

    protected boolean isVisible(ConfigSetting<?> setting) {
        return visibilityCache.compute(setting, (s, v) -> {
            if (v == null) v = Visibility.UNKNOWN;
            else if (v.visible != null) return v;

            ConfigSetting<?> parents = s;
            do if (matches(parents)) return v.setVisible(true);
            while ((parents = parents.getParent()) != null);

            if (s instanceof ConfigSetting.Parent) {
                return v.setVisible(((ConfigSetting.Parent<?>) s).getChildren()
                        .values()
                        .stream()
                        .anyMatch(this));
            }

            return v.setVisible(false);
        }).visible;
    }

    protected boolean matches(ConfigSetting<?> setting) {
        return visibilityCache.compute(setting, (s, v) -> {
            if (v == null) v = Visibility.UNKNOWN;
            else if (v.matches != null) return v;
            return v.setMatches(search.matches(setting.getName()) ||
                    search.matches(setting.getDescription()) ||
                    search.matches(setting.getKey().replace(".", " ")));
        }).matches;
    }

}
