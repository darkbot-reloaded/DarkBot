package com.github.manolo8.darkbot.gui.tree.utils;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.extensions.PluginInfo;
import eu.darkbot.api.managers.I18nAPI;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EnumDropdownOptions<E extends Enum<E>> implements Dropdown.Options<E> {

    private final I18nAPI i18n;
    private final PluginInfo context;
    private final List<E> options;
    private final String baseKey;

    public EnumDropdownOptions(PluginAPI api, PluginInfo context, Class<E> e) {
        this.i18n = api.requireAPI(I18nAPI.class);
        this.context = context;
        this.options = Arrays.asList(e.getEnumConstants());
        this.baseKey = e.getAnnotation(Configuration.class).value();
    }

    @Override
    public List<E> options() {
        return options;
    }

    @Override
    public String getText(E option) {
        if (option == null) return "";
        String name = option.name().toLowerCase(Locale.ROOT);
        return i18n.getOrDefault(context, baseKey + "." + name, name);
    }

    @Override
    public String getTooltip(E option) {
        if (option == null) return null;
        String name = option.name().toLowerCase(Locale.ROOT);
        return i18n.getOrDefault(context, baseKey + "." + name + ".desc", null);
    }
}
