package com.github.manolo8.darkbot.gui.plugins;

import com.bulenkov.iconloader.util.Gray;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Stream;

public class PluginDisplay extends JPanel implements PluginListener {

    private Main main;
    private MainButton pluginTab;
    private PluginHandler pluginHandler;

    private JPanel pluginPanel;

    public PluginDisplay() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void setup(Main main, MainButton pluginTab) {
        this.main = main;
        this.pluginTab = pluginTab;
        this.pluginHandler = main.pluginHandler;
        add(setupUI());
        afterLoadComplete();
        pluginHandler.addListener(this);
    }

    private JComponent setupUI() {
        pluginPanel = new JPanel(new MigLayout("wrap 1, fillx", "[fill]", ""));
        JScrollPane scrollPane = new JScrollPane(pluginPanel);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    @Override
    public void beforeLoad() {
        pluginPanel.removeAll();
    }

    @Override
    public void afterLoadComplete() {
        Stream.concat(
                pluginHandler.LOADING_EXCEPTIONS.stream().map(ExceptionCard::new),
                Stream.concat(
                        pluginHandler.FAILED_PLUGINS.stream(),
                        pluginHandler.LOADED_PLUGINS.stream()
                ).map(pl -> new PluginCard(pl, main.moduleHandler, main.behaviourHandler))
        ).forEach(pluginPanel::add);
        pluginTab.setIcon(UIUtils.getIcon(pluginHandler.LOADING_EXCEPTIONS.isEmpty() && pluginHandler.FAILED_PLUGINS.isEmpty() ? "plugins" : "plugins_warn"));
        validate();
        repaint();
    }

}
