package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.gui.components.DiagnosticsPanel;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.function.Function;

public class DiagnosticBar extends JButton {

    static JComponent create(Main main) {
        return new JLayer<JButton>(new DiagnosticBar(main), new LayerUI<>() {
            public void installUI(JComponent c) {
                super.installUI(c);
                ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
            }

            public void uninstallUI(JComponent c) {
                super.uninstallUI(c);
                ((JLayer<?>) c).setLayerEventMask(0);
            }

            @Override
            protected void processMouseEvent(MouseEvent e, JLayer<? extends JButton> l) {
                Component source = (Component) e.getSource();
                Component parent = l.getView();

                if (source == parent) return;
                parent.dispatchEvent(SwingUtilities.convertMouseEvent(source, e, parent));
            }
        });
    }

    private DiagnosticBar(Main main) {
        StatsManager stats = main.statsManager;

        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new MigLayout("ins 0, gap 0", "3px:5px[][right]3px:5px", "[15px!][15px!]"));
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);

        JLabel tick = createLabel("tick_time", "Tick time", false,
                color -> UIUtils.getTrafficLight(main.getTickTime(), 30), stats.getTickStats());
        JLabel ping = createLabel("ping", "In-game ping", false,
                color -> UIUtils.getTrafficLight(main.statsManager.getPing(), 300), stats.getPingStats());
        add(tick, "cell 0 0");
        add(ping, "cell 0 1");

        if (Main.API.hasCapability(Capability.HANDLER_CPU_USAGE, Capability.HANDLER_RAM_USAGE)) {
            JLabel cpu = createLabel("cpu", "Cpu usage", true,
                    color -> UIUtils.getTrafficLight(Main.API.getCpuUsage(), 100), stats.getCpuStats());
            JLabel ram = createLabel("ram", "Ram usage", true,
                    color -> UIUtils.getTrafficLight(Main.API.getMemoryUsage(), 2500), stats.getMemoryStats());
            add(cpu, "cell 1 0, gapleft 5px");
            add(ram, "cell 1 1, gapleft 5px");
        }

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
}
