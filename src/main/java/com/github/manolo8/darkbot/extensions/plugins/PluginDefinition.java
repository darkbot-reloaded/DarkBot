package com.github.manolo8.darkbot.extensions.plugins;

public class PluginDefinition {
    private transient final String[] NULL_ARRAY = new String[0];
    public String name;
    public String author;
    public String[] modules = NULL_ARRAY;
    public String[] behaviours = NULL_ARRAY;
}
