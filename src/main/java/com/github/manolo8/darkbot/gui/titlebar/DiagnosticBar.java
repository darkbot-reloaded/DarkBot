package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.gui.components.DiagnosticsPanel;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.Function;

public class DiagnosticBar extends JButton {

    private final Main main;

    DiagnosticBar(Main main) {
        this.main = main;
        StatsManager statsManager = main.statsManager;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(1, 5, 0, 5));
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);

        add(createLeftPanel(statsManager), BorderLayout.WEST);
        if (Main.API.hasCapability(Capability.HANDLER_CPU_USAGE, Capability.HANDLER_RAM_USAGE)) {
            add(createRightPanel(statsManager), BorderLayout.EAST);
        }

        redirectMouseEvents(this);
        addActionListener(l -> new DiagnosticsPanel(main, this));
    }

    private JComponent createLeftPanel(StatsManager statsManager) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setOpaque(false);

        JLabel tick = createLabel("tick_time", "Tick time", false,
                color -> UIUtils.getTrafficLight(main.getTickTime(), 30), statsManager.getTickStats());
        JLabel ping = createLabel("ping", "In-game ping", false,
                color -> UIUtils.getTrafficLight(main.statsManager.getPing(), 300), statsManager.getPingStats());

        panel.add(tick);
        panel.add(ping);

        return panel;
    }

    @Override
    public Dimension getSize() {
        return super.getSize();
    }

    private JComponent createRightPanel(StatsManager statsManager) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setOpaque(false);

        JLabel cpu = createLabel("cpu", "Cpu usage", true, null, statsManager.getCpuStats());
        JLabel ram = createLabel("ram", "Ram usage", true, null, statsManager.getMemoryStats());

        panel.add(cpu);
        panel.add(ram);

        return panel;
    }

    private JLabel createLabel(String iconName, String tooltip, boolean alignRight,
                               Function<Color, Color> colorFilter, StatsManager.AverageStats stat) {
        FlatSVGIcon icon = UIUtils.getSVGIcon(iconName, 12, 12);
        if (colorFilter != null) icon.setColorFilter(new FlatSVGIcon.ColorFilter(colorFilter));

        JLabel label = new JLabel(icon);
        label.setFont(label.getFont().deriveFont(11f));
        label.setToolTipText(tooltip);
        label.setText("...");

        if (alignRight) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
        } else {
            label.setHorizontalAlignment(SwingConstants.LEFT);
        }

        Dimension preferredSize = label.getPreferredSize();
        label.setPreferredSize(new Dimension(42, preferredSize.height));

        stat.setListener(label::setText);
        return label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //draw separators
        g.setColor(UIUtils.darker(g.getColor(), 0.6));
        g.drawLine(0, 5, 0, getHeight() - 6);
        g.drawLine(getWidth() - 1, 5, getWidth() - 1, getHeight() - 6);
        if (getComponentCount() > 1)
            g.drawLine(getWidth() / 2, 8, getWidth() / 2, getHeight() - 9);
    }

    private void redirectMouseEvents(JComponent c) {
        for (Component component : c.getComponents()) {
            redirectMouseEvents((JComponent) component);

            component.addMouseListener(new RedirectMouseAdapter());
            component.addMouseMotionListener(new RedirectMouseAdapter());
        }
    }

    private static class RedirectMouseAdapter implements MouseListener, MouseMotionListener {
        public void mouseClicked(MouseEvent e) {
            redirect(e);
        }

        public void mousePressed(MouseEvent e) {
            redirect(e);
        }

        public void mouseReleased(MouseEvent e) {
            redirect(e);
        }

        public void mouseEntered(MouseEvent e) {
            redirect(e);
        }

        public void mouseExited(MouseEvent e) {
            redirect(e);
        }

        public void mouseDragged(MouseEvent e) {
            redirect(e);
        }

        public void mouseMoved(MouseEvent e) {
            redirect(e);
        }

        private void redirect(MouseEvent e) {
            Component source = (Component) e.getSource();
            source.getParent().dispatchEvent(SwingUtilities.convertMouseEvent(source, e, source.getParent()));
        }
    }
}
