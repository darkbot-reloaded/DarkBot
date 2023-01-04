package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.extensions.plugins.PluginUpdater;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class PluginDisplay extends JPanel implements PluginListener {

    private Main main;
    private PluginHandler pluginHandler;
    private PluginUpdater pluginUpdater;

    private AbstractButton pluginTab;
    private JPanel pluginPanel;
    private PluginUpdateHeader header;

    public PluginDisplay() {
        super(new MigLayout("ins 0, gap 0, wrap 1, fillx", "[fill]", ""));
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void setup(Main main, AbstractButton pluginTab) {
        this.main = main;
        this.pluginTab = pluginTab;
        this.pluginHandler = main.pluginHandler;
        this.pluginUpdater = main.pluginUpdater;
        setupUI();
        refreshUI();
        pluginHandler.addListener(this);
        pluginUpdater.setup(this);
    }

    private void setupUI() {
        if (pluginPanel != null) return;
        pluginPanel = new JPanel(new MigLayout("wrap 1, fillx", "[fill]", ""));
        header = new PluginUpdateHeader(main);

        JScrollPane scrollPane = new JScrollPane(pluginPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);

        add(header);
        add(scrollPane);
    }

    public void refreshUI() {
        header.refreshUI();
        pluginPanel.removeAll();

        Stream.concat(pluginHandler.LOADING_EXCEPTIONS.stream(), pluginUpdater.UPDATING_EXCEPTIONS.stream())
                .map(ExceptionCard::new)
                .forEach(pluginPanel::add);

        pluginPanel.add(new NativeCard(main, main.featureRegistry));

        Stream.concat(pluginHandler.FAILED_PLUGINS.stream(), pluginHandler.LOADED_PLUGINS.stream())
                .map(pl -> new PluginCard(main, pl, main.featureRegistry))
                .forEach(pluginPanel::add);

        if (!pluginHandler.LOADING_EXCEPTIONS.isEmpty() || !pluginHandler.FAILED_PLUGINS.isEmpty())
            pluginTab.setIcon(UIUtils.getIcon("plugins_warn"));
        else if (pluginUpdater.hasAnyUpdates()) pluginTab.setIcon(UIUtils.getIcon("plugins_update"));
        else pluginTab.setIcon(UIUtils.getIcon("plugins"));

        validate();
        repaint();
    }

    public PluginCard getPluginCard(Plugin plugin) {
        return Arrays.stream(pluginPanel.getComponents())
                .filter(comp -> comp instanceof PluginCard)
                .map(comp -> (PluginCard) comp)
                .filter(pl -> pl.getPlugin() == plugin)
                .findFirst()
                .orElse(null);
    }

    public JProgressBar getMainProgressBar() {
        return header.getProgressBar();
    }

    @Override
    public void beforeLoad() {
        pluginPanel.removeAll();
    }

    @Override
    public void afterLoadCompleteUI() {
        refreshUI();
    }

}
