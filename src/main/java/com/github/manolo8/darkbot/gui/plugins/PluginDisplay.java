package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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
        if (pluginPanel == null) add(setupUI());
        refreshUI();
        pluginHandler.addListener(this);
    }

    private JComponent setupUI() {
        pluginPanel = new JPanel(new MigLayout("wrap 1, fillx", "[fill]", ""));
        JScrollPane scrollPane = new JScrollPane(pluginPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
        return scrollPane;
    }

    private void refreshUI() {
        pluginPanel.removeAll();

        List<PluginName> names = new ArrayList<>();
        pluginPanel.add(new PluginUpdateHeader(main, names));
        Stream.concat(
                pluginHandler.LOADING_EXCEPTIONS.stream().map(ExceptionCard::new),
                Stream.concat(
                        pluginHandler.FAILED_PLUGINS.stream(),
                        pluginHandler.LOADED_PLUGINS.stream()
                ).map(pl -> {
                    PluginCard card = new PluginCard(main, pl, main.featureRegistry);
                    if (pluginHandler.AVAILABLE_UPDATES.containsKey(pl))
                        names.add(card.name);
                    return card;
                })
        ).forEach(pluginPanel::add);
        //todo maybe also add another icon here when update is available?
        pluginTab.setIcon(UIUtils.getIcon(pluginHandler.LOADING_EXCEPTIONS.isEmpty() && pluginHandler.FAILED_PLUGINS.isEmpty() ? "plugins" : "plugins_warn"));
        validate();
        repaint();
    }

    @Override
    public void beforeLoad() {
        pluginPanel.removeAll();
    }

    @Override
    public void afterLoadComplete() {
        SwingUtilities.invokeLater(this::refreshUI);
    }

}
