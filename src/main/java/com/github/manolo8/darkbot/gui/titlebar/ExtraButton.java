package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.gui.utils.PopupMenuListenerAdapter;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.inspector.ObjectInspectorUI;
import com.github.manolo8.darkbot.utils.OSUtil;
import com.github.manolo8.darkbot.utils.SystemUtils;
import com.github.manolo8.darkbot.utils.debug.SWFUtils;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.ExtraMenus;
import eu.darkbot.api.managers.BackpageAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.I18nAPI;

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

public class ExtraButton extends TitleBarButton<JFrame> {

    private final Main main;
    private final JPopupMenu extraOptions = new JPopupMenu("Extra Options");

    private static final Set<ExtraMenus> EXTRA_DECORATIONS = new LinkedHashSet<>();
    private static final Lazy<Void> clean = new Lazy.NoCache<>();

    public static void setExtraDecorations(Stream<ExtraMenus> provider, FeatureRegistry featureRegistry) {
        EXTRA_DECORATIONS.clear();
        PluginExtraMenuProvider.PLUGINS.clear();

        AtomicReference<ExtraMenus> pluginProvider = new AtomicReference<>();
        provider.forEach(extra -> {
            Plugin pl = featureRegistry.getFeatureDefinition(extra).getPlugin();
            if (pl != null && extra.autoSubmenu())
                PluginExtraMenuProvider.PLUGINS.computeIfAbsent(
                        pl,
                        features -> new LinkedHashSet<>()).add(extra);
            else if (extra instanceof PluginExtraMenuProvider) pluginProvider.set(extra);
            else EXTRA_DECORATIONS.add(extra);
        });

        // Add PluginExtraMenuProvider outside forEach, so it is always placed at end
        if (pluginProvider.get() != null) {
            EXTRA_DECORATIONS.add(pluginProvider.get());
        }

        clean.send(null);
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
        for (ExtraMenus extraDecoration : EXTRA_DECORATIONS) {
            for (JComponent component : extraDecoration.getExtraMenuItems(main.pluginAPI)) {
                extraOptions.add(component);
            }
        }
        empty = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        rebuild(main);
        extraOptions.show(this, 0, getHeight() - 1);
    }

    @Feature(name = "Extra menu default provider", description = "Provides default extra buttons")
    public static class DefaultExtraMenuProvider implements ExtraMenus {

        @Override
        public Collection<JComponent> getExtraMenuItems(PluginAPI api) {
            List<JComponent> list = new ArrayList<>();

            I18nAPI i18n = api.requireAPI(I18nAPI.class);
            BackpageAPI backpage = api.requireAPI(BackpageAPI.class);
            ConfigAPI config = api.requireAPI(ConfigAPI.class);
            Main main = api.requireInstance(Main.class);

            String p = "gui.hamburger_button.";

            list.add(create(i18n.get(p + "home"), e -> {
                String sid = backpage.getSid(), instance = backpage.getInstanceURI().toString();
                if (sid == null || sid.isEmpty() || instance == null || instance.isEmpty()) return;
                String url = instance + "?dosid=" + sid;
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) SystemUtils.toClipboard(url);
                else SystemUtils.openUrl(url);
            }));
            list.add(create(i18n.get(p + "reload"), e -> main.addTask(() -> {
                System.out.println("Triggering refresh: user requested");
                try {
                    Main.API.handleRefresh();
                } catch (Exception ex) {
                    System.out.println("Exception handling user requested refresh:");
                    ex.printStackTrace();
                }
            })));
            list.add(create(i18n.get(p + "discord"), UIUtils.getIcon("discord"),
                    e -> SystemUtils.openUrl("https://discord.gg/KFd8vZT")));
            list.add(create(i18n.get(p + "copy_sid"), e -> SystemUtils.toClipboard(backpage.getSid())));
            list.add(create(i18n.get(p + "reset_colorscheme"), e -> {
                ConfigSetting<ColorScheme> cs = config.getConfig("bot_settings.map_display.cs");
                if (cs == null) return;
                cs.setValue(new ColorScheme());
                ConfigEntity.changed();
            }));
            list.add(create(i18n.get(p + "reset_stats"), e -> {
                main.statsManager.resetStats();
                main.repairManager.resetDeaths();
            }));

            if (OSUtil.isWindows()) {
                list.add(create("Open Hangar", e -> {
                    JComponent component = (JComponent) e.getSource();
                    component.setEnabled(false);
                    new FlashRunnerTask("Dock", main,
                            result -> SwingUtilities.invokeLater(() -> component.setEnabled(true)));
                }));
            }

            ConfigSetting<Config> root = config.getConfigRoot();
            if (root.getValue().BOT_SETTINGS.OTHER.DEV_STUFF) {
                list.add(createSeparator("Dev stuff"));
                list.add(create("Save SWF", e -> main.addTask(SWFUtils::dumpMainSWF)));
                list.add(create("Reset keybinds", e -> main.addTask(() -> main.guiManager.settingsGui.setKeyBinds(false))));
                list.add(create("Object inspector", e -> {
                    JFrame frame = new ObjectInspectorUI((JMenuItem) e.getSource());
                    frame.setSize(800, 600);
                    frame.setVisible(true);
                }));
            }

            return list;
        }

    }

    @Feature(name = "Plugin menu provider", description = "Provides plugin extra menus")
    public static class PluginExtraMenuProvider implements ExtraMenus {
        private static final Map<Plugin, Set<ExtraMenus>> PLUGINS = new LinkedHashMap<>();

        @Override
        public Collection<JComponent> getExtraMenuItems(PluginAPI api) {
            if (PLUGINS.isEmpty()) return Collections.emptyList();
            List<JComponent> list = new ArrayList<>();

            PLUGINS.forEach((plugin, features) -> {
                List<JComponent> menus = features.stream()
                        .flatMap(f -> f.getExtraMenuItems(api).stream()).collect(Collectors.toList());
                if (!menus.isEmpty())
                    list.add(createMenu(plugin.getName(), menus.stream()));
            });

            if (!list.isEmpty()) {
                I18nAPI i18n = api.requireAPI(I18nAPI.class);
                list.add(0, createSeparator(i18n.get("gui.hamburger_button.plugins")));
            }

            return list;
        }
    }
}
