package com.github.manolo8.darkbot.gui.utils.tree;

import com.github.manolo8.darkbot.utils.StringQuery;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.annotations.Visibility;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class TreeFilter implements Predicate<ConfigSetting<?>> {
    private final StringQuery search = new StringQuery();
    private Visibility.Level visibility = Visibility.Level.BASIC;

    private final Map<ConfigSetting<?>, SearchCache> searchCache = new IdentityHashMap<>();
    private final Map<ConfigSetting<?>, Boolean> visibilityCache = new IdentityHashMap<>();

    private enum SearchCache {
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

        SearchCache(Boolean matches, Boolean visible) {
            this.matches = matches;
            this.visible = visible;
        }

        public SearchCache setMatches(boolean result) {
            return SearchCache.values()[this.ordinal() + (result ? 2 : 1)];
        }

        public SearchCache setVisible(boolean result) {
            return SearchCache.values()[this.ordinal() + (result ? 6 : 3)];
        }
    }

    public void setSearch(String query) {
        this.search.query = query;
        this.searchCache.clear();
    }

    public void setVisibility(Visibility.Level visibility) {
        this.visibility = visibility;
        this.visibilityCache.clear();
    }

    public void invalidate() {
        searchCache.clear();
        visibilityCache.clear();
    }

    public boolean isSearching() {
        return search.query != null && !search.query.isEmpty();
    }

    public boolean isUnfiltered() {
        return !isSearching() && visibility == Visibility.Level.DEVELOPER;
    }

    @Override
    public boolean test(ConfigSetting<?> setting) {
        if (setting instanceof ToggleableNode && !((ToggleableNode) setting).isShown()) return false;
        if (isUnfiltered()) return true;

        return isVisibilityShown(setting) && isSearchShown(setting);
    }

    protected boolean isVisibilityShown(ConfigSetting<?> setting) {
        return visibilityCache.computeIfAbsent(setting, s -> getVisibility(s).ordinal() <= visibility.ordinal());
    }

    private Visibility.Level getVisibility(ConfigSetting<?> setting) {
        if (setting == null) return Visibility.Level.BASIC;
        return setting.getOrCreateMetadata("visibility", () -> getVisibility(setting.getParent()));
    }

    protected boolean isSearchShown(ConfigSetting<?> setting) {
        return searchCache.compute(setting, (s, v) -> {
            if (v == null) v = SearchCache.UNKNOWN;
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
        return searchCache.compute(setting, (s, v) -> {
            if (v == null) v = SearchCache.UNKNOWN;
            else if (v.matches != null) return v;

            return v.setMatches(search.matches(setting.getName()) ||
                    search.matches(setting.getDescription()) ||
                    search.matches(setting.getKey().replace(".", " ")));
        }).matches;
    }

}
