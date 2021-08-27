package com.github.manolo8.darkbot.config.tree;

import com.github.manolo8.darkbot.utils.StringQuery;
import eu.darkbot.api.config.ConfigSetting;

import java.util.function.Predicate;

public class TreeFilter implements Predicate<ConfigSetting<?>> {
    private final StringQuery search = new StringQuery();

    public void setSearch(String query) {
        this.search.query = query;
    }

    public boolean isUnfiltered() {
        return search.query == null || search.query.isEmpty();
    }

    @Override
    public boolean test(ConfigSetting<?> setting) {
        if (isUnfiltered()) return true;

        ConfigSetting<?> parents = setting;
        do if (matches(parents)) return true;
        while ((parents = parents.getParent()) != null);

        if (setting instanceof ConfigSetting.Parent) {
            return ((ConfigSetting.Parent<?>) setting).getChildren()
                    .values()
                    .stream()
                    .anyMatch(this);
        }

        return false;
    }

    private boolean matches(ConfigSetting<?> setting) {
        return search.matches(setting.getName()) ||
                search.matches(setting.getDescription()) ||
                search.matches(setting.getKey());
    }

}
