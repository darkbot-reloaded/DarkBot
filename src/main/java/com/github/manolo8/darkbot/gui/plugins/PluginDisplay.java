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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class PluginDisplay extends JPanel implements PluginListener {

    private Main main;
    private MainButton pluginTab;
    private PluginHandler pluginHandler;

    private JPanel pluginPanel;

    private List<PluginCard> cards = new ArrayList<>();

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

    void refreshUI() {
        pluginPanel.removeAll();
        cards.clear();

        pluginPanel.add(new PluginUpdateHeader(main, this));
        Stream.concat(
                pluginHandler.LOADING_EXCEPTIONS.stream().map(ExceptionCard::new),
                Stream.concat(
                        pluginHandler.FAILED_PLUGINS.stream(),
                        pluginHandler.LOADED_PLUGINS.stream()
                ).map(pl -> {
                    PluginCard card = new PluginCard(main, pl, main.featureRegistry);
                    if (pluginHandler.AVAILABLE_UPDATES.contains(pl))
                        cards.add(card);
                    return card;
                })
        ).forEach(pluginPanel::add);

        //todo maybe also add another icon here when update is available?
        pluginTab.setIcon(UIUtils.getIcon(pluginHandler.LOADING_EXCEPTIONS.isEmpty() && pluginHandler.FAILED_PLUGINS.isEmpty() ? "plugins" : "plugins_warn"));
        validate();
        repaint();
    }

    void updateAll(UpdateProgressBar progressBar) {
        double total = pluginHandler.AVAILABLE_UPDATES.size();
        AtomicInteger current = new AtomicInteger();

        cards.forEach(c -> {
            c.name.updateButton.setEnabled(false);
            c.update();
            c.updateTask.addPropertyChangeListener(l -> {
                if ("progress".equals(l.getPropertyName())) {
                    current.addAndGet((int) (25 / total));
                    progressBar.setValue(current.get());
                }
            });
        });
        cards.forEach(c -> c.updateTask.makeReloadable());
        while (!cards.stream().allMatch(c -> c.updateTask.canReload));
        pluginHandler.updatePlugins();
        cards.forEach(c -> c.updateTask.finishedReloading());
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
