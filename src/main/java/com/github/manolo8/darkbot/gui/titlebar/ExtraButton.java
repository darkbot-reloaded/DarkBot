package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.gui.utils.PopupMenuListenerAdapter;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.SystemUtils;
import com.github.manolo8.darkbot.utils.debug.SWFUtils;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtraButton extends TitleBarToggleButton<JFrame> {

    private final Main main;
    private final JPopupMenu extraOptions = new JPopupMenu("Extra Options");

    private static final Set<ExtraMenuProvider> EXTRA_DECORATIONS = new LinkedHashSet<>();
    private static final Lazy<Void> clean = new Lazy.NoCache<>();

    public static void setExtraDecorations(Stream<ExtraMenuProvider> provider, FeatureRegistry featureRegistry) {
        EXTRA_DECORATIONS.clear();
        PluginExtraMenuProvider.PLUGINS.clear();

        AtomicReference<ExtraMenuProvider> pluginProvider = new AtomicReference<>();
        provider.forEach(extra -> {
            Plugin pl = featureRegistry.getFeatureDefinition(extra).getPlugin();
            if (pl != null && extra.autoSubmenu())
                PluginExtraMenuProvider.PLUGINS.computeIfAbsent(
                        pl,
                        features -> new LinkedHashSet<>()).add(extra);
            else if (extra instanceof PluginExtraMenuProvider) pluginProvider.set(extra);
            else EXTRA_DECORATIONS.add(extra);
        });

        // Add PluginExtraMenuProvider outside of forEach so it is always placed at end
        if (pluginProvider.get() != null) {
            EXTRA_DECORATIONS.add(pluginProvider.get());
        }

        clean.send(null);
    }

    @Feature(name = "Plugin menu provider", description = "Provides plugin extra menus")
    public static class PluginExtraMenuProvider implements ExtraMenuProvider {
        private static final Map<Plugin, Set<ExtraMenuProvider>> PLUGINS = new LinkedHashMap<>();

        @Override
        public Collection<JComponent> getExtraMenuItems(Main main) {
            if (PLUGINS.isEmpty()) return Collections.emptyList();
            List<JComponent> list = new ArrayList<>();

            PLUGINS.forEach((plugin, features) -> {
                List<JComponent> menus = features.stream()
                        .flatMap(f -> f.getExtraMenuItems(main).stream()).collect(Collectors.toList());
                if (!menus.isEmpty())
                    list.add(createMenu(plugin.getName(), menus.stream()));
            });

            if (!list.isEmpty()) list.add(0, createSeparator("plugins"));

            return list;
        }
    }

    protected boolean empty = true;

    ExtraButton(Main main, JFrame frame) {
        super(UIUtils.getIcon("hamburger"), frame);

        this.main = main;

        extraOptions.setBorder(UIUtils.getBorder());
        extraOptions.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                setSelected(false);
            }
        });

        clean.add(v -> {
            extraOptions.removeAll();
            empty = true;
        });

    }

    private void rebuild(Main main) {
        if (!empty) return;
        for (ExtraMenuProvider extraDecoration : EXTRA_DECORATIONS) {
            for (JComponent component : extraDecoration.getExtraMenuItems(main)) {
                extraOptions.add(component);
            }
        }
        empty = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        rebuild(main);
        if (isSelected()) extraOptions.show(this, 0, getHeight() - 1);
    }

    @Feature(name = "Extra menu default provider", description = "Provides default extra buttons")
    public static class DefaultExtraMenuProvider implements ExtraMenuProvider {

        @Override
        public Collection<JComponent> getExtraMenuItems(Main main) {
            List<JComponent> list = new ArrayList<>();

            list.add(create("home", e -> {
                String sid = main.statsManager.sid, instance = main.statsManager.instance;
                if (sid == null || sid.isEmpty() || instance == null || instance.isEmpty()) return;
                String url = instance + "?dosid=" + sid;
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) SystemUtils.toClipboard(url);
                else SystemUtils.openUrl(url);
            }));
            list.add(create("reload", e -> {
                System.out.println("Triggering refresh: user requested");
                Main.API.handleRefresh();
            }));
            list.add(create("discord", UIUtils.getIcon("discord"), e -> SystemUtils.openUrl("https://discord.gg/KFd8vZT")));
            list.add(create("copy_sid", e -> SystemUtils.toClipboard(main.statsManager.sid)));
            list.add(create("reset_colorscheme", e -> {
                main.config.BOT_SETTINGS.MAP_DISPLAY.cs = new ColorScheme();
                ConfigEntity.changed();
                main.getGui().updateConfiguration();
            }));


            if (main.config.BOT_SETTINGS.OTHER.DEV_STUFF) {
                list.add(createSeparator("Dev stuff"));
                list.add(create("Save SWF", e -> SWFUtils.dumpMainSWF()));
            }

            return list;
        }

    }

}
