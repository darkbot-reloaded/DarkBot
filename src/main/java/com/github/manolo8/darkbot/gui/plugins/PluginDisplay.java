package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.extensions.plugins.PluginUpdater;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class PluginDisplay extends JPanel implements PluginListener {

    private Main main;
    private MainButton pluginTab;
    private PluginHandler pluginHandler;
    private PluginUpdater pluginUpdater;

    private JPanel pluginPanel;

    public PluginDisplay() {
        super(new MigLayout("wrap 1, fillx", "[fill]", ""));
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void setup(Main main, MainButton pluginTab) {
        this.main = main;
        this.pluginTab = pluginTab;
        this.pluginHandler = main.pluginHandler;
        this.pluginUpdater = main.pluginUpdater;
        pluginUpdater.setPluginDisplay(this);
        setupUI();
        refreshUI();
        pluginHandler.addListener(this);
    }

    private void setupUI() {
        if (pluginPanel != null) return;
        pluginPanel = new JPanel(new MigLayout("wrap 1, fillx", "[fill]", ""));

        add(new PluginUpdateHeader(pluginUpdater));
        add(new ScrollPane(pluginPanel));
    }

    public void refreshUI() {
        removeAll();
        pluginPanel.removeAll();

        Stream.concat(
                pluginHandler.LOADING_EXCEPTIONS.stream().map(ExceptionCard::new),
                Stream.concat(
                        pluginHandler.FAILED_PLUGINS.stream(),
                        pluginHandler.LOADED_PLUGINS.stream()
                ).map(pl -> new PluginCard(main, pl, main.featureRegistry))
        ).forEach(pluginPanel::add);

        add(new PluginUpdateHeader(pluginUpdater));
        add(new ScrollPane(pluginPanel));

        if (!pluginHandler.LOADING_EXCEPTIONS.isEmpty() || !pluginHandler.FAILED_PLUGINS.isEmpty())
            pluginTab.setIcon(UIUtils.getIcon("plugin_warn"));
        else if (pluginUpdater.hasUpdates()) pluginTab.setIcon(UIUtils.getIcon("plugins"));//todoo add "plugins_update" icon here
        else pluginTab.setIcon(UIUtils.getIcon("plugins"));

        validate();
        repaint();
    }

    public PluginCard getPluginCard(Plugin plugin) {
        return Arrays.stream(pluginPanel.getComponents())
                .map(comp -> (PluginCard) comp)
                .filter(pl -> pl.getPlugin().equals(plugin))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void beforeLoad() {
        pluginPanel.removeAll();
    }

    @Override
    public void afterLoadComplete() {
        SwingUtilities.invokeLater(this::refreshUI);
    }

    private static class ScrollPane extends JScrollPane {
        private ScrollPane(JPanel panel) {
            super(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            setBorder(BorderFactory.createEmptyBorder());
            getVerticalScrollBar().setUnitIncrement(25);
        }
    }

}
