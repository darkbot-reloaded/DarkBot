package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ExtraButton extends TitleBarToggleButton<JFrame> {

    private final Main main;
    private final JPopupMenu extraOptions = new JPopupMenu("Extra Options");

    private static final Set<ExtraMenuProvider> EXTRA_DECORATIONS = new LinkedHashSet<>();
    private static final Lazy<Void> clean = new Lazy.NoCache<>();

    public static void setExtraDecorations(Stream<ExtraMenuProvider> provider, FeatureRegistry featureRegistry) {
        EXTRA_DECORATIONS.clear();
        PluginExtraMenuProvider.plugins.clear();

        provider.forEach(extra -> {
            if (!(extra instanceof PluginExtraMenuProvider) && !(extra instanceof DefaultExtraMenuProvider)) {
                Plugin plugin = featureRegistry.getFeatureDefinition(extra).getPlugin();
                PluginExtraMenuProvider.plugins.computeIfAbsent(
                        plugin,
                        features -> new LinkedHashSet<>()).add(extra);
            } else {
                EXTRA_DECORATIONS.add(extra);
            }
        });

        clean.send(null);
    }

    @Feature(name = "Plugin menu provider", description = "Provides plugin extra menus")
    public static class PluginExtraMenuProvider implements ExtraMenuProvider {
        private static final Map<Plugin, Set<ExtraMenuProvider>> plugins = new HashMap<>();

        @Override
        public Collection<JComponent> getExtraMenuItems(Main main) {
            List<JComponent> list = new ArrayList<>();
            if (plugins.isEmpty()) return list;
            list.add(createSeparator("plugins"));

            plugins.forEach((plugin, features) -> {
                List<JComponent> components = new ArrayList<>();
                features.forEach(feature -> feature.getExtraMenuItems(main).forEach(component -> components.add(component)));
                list.add(createMenu(plugin.getName(), components));
            });

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
            list.add(create("discord", e -> SystemUtils.openUrl("https://discord.gg/KFd8vZT")));
            list.add(create("copy_sid", e -> SystemUtils.toClipboard(main.statsManager.sid)));


            if (main.config.BOT_SETTINGS.DEV_STUFF) {
                list.add(createSeparator("Dev stuff"));
                list.add(create("Save SWF", e -> SWFUtils.dumpMainSWF()));
            }

            return list;
        }
    }
}