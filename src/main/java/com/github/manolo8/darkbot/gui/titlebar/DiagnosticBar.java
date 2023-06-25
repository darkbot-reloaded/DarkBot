package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.gui.components.DiagnosticsPanel;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.Function;

public class DiagnosticBar extends JButton {

    DiagnosticBar(Main main) {
        StatsManager statsManager = main.statsManager;

        setBorder(BorderFactory.createEmptyBorder());

        setLayout(new MigLayout("ins 0, gap 0", "3px:5px[][right]3px:5px", "[15px!][15px!]"));
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);

        JLabel tick = createLabel("tick_time", "Tick time", false,
                color -> UIUtils.getTrafficLight(main.getTickTime(), 30), statsManager.getTickStats());
        JLabel ping = createLabel("ping", "In-game ping", false,
                color -> UIUtils.getTrafficLight(main.statsManager.getPing(), 300), statsManager.getPingStats());
        add(tick, "cell 0 0");
        add(ping, "cell 0 1");
        if (Main.API.hasCapability(Capability.HANDLER_CPU_USAGE, Capability.HANDLER_RAM_USAGE)) {
            JLabel cpu = createLabel("cpu", "Cpu usage", true, null, statsManager.getCpuStats());
            JLabel ram = createLabel("ram", "Ram usage", true, null, statsManager.getMemoryStats());
            add(cpu, "cell 1 0, gapleft 5px");
            add(ram, "cell 1 1, gapleft 5px");
        }

        redirectMouseEvents(this);
        addActionListener(l -> new DiagnosticsPanel(main, this));
    }

    private JLabel createLabel(String iconName, String tooltip, boolean alignRight,
                               Function<Color, Color> colorFilter, StatsManager.AverageStats stat) {
        FlatSVGIcon icon = UIUtils.getSVGIcon(iconName, 15, 15);
        if (colorFilter != null) icon.setColorFilter(new FlatSVGIcon.ColorFilter(colorFilter));

        JLabel label = new JLabel(icon);
        label.setFont(label.getFont().deriveFont(11f));
        label.setToolTipText(tooltip);
        label.setText("-");

        if (alignRight) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
        } else {
            label.setHorizontalAlignment(SwingConstants.LEFT);
        }

        stat.setListener(label::setText);
        return label;
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
