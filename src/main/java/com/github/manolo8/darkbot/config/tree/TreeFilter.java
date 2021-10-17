package com.github.manolo8.darkbot.config.tree;

import com.github.manolo8.darkbot.utils.StringQuery;

public class TreeFilter {
    private final StringQuery search = new StringQuery();
    private int selectedRoot = -1;

    public int getSelectedRoot() {
        return selectedRoot;
    }

    public void setSelectedRoot(int i) {
        this.selectedRoot = i;
    }

    public void setSearch(String query) {
        this.search.query = query;
    }

    public boolean matches(ConfigNode node) {
        return search.matches(node.convertToString());
    }

    public boolean isUnfiltered() {
        return search.query == null || search.query.isEmpty();
    }

}
