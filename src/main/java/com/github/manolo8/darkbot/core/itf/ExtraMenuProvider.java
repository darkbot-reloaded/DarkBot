package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.extensions.ExtraMenus;

import javax.swing.*;
import java.util.Collection;

public interface ExtraMenuProvider extends ExtraMenus {
    Collection<JComponent> getExtraMenuItems(Main main);

    @Override
    default Collection<JComponent> getExtraMenuItems(PluginAPI pluginAPI) {
        return getExtraMenuItems(pluginAPI.requireInstance(Main.class));
    }

}
