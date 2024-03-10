package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.components.DiagnosticsPanel;
import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.plaf.LayerUI;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.function.Function;

public class DiagnosticBar extends JButton {
    private final MainGui frame;

    static JComponent create(Main main, MainGui frame) {
        return new JLayer<JButton>(new DiagnosticBar(main, frame), new LayerUI<>() {
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

    private DiagnosticBar(Main main, MainGui frame) {
        this.frame = frame;
        StatsManager stats = main.statsManager;

        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);

        JLabel tick = createLabel("tick_time", "Tick time", false, true,
                color -> UIUtils.getTrafficLight(main.getTickTime(), 30), stats.getTickStats());
        JLabel ping = createLabel("ping", "In-game ping", false, false,
                color -> UIUtils.getTrafficLight(main.statsManager.getPing(), 300), stats.getPingStats());

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(tick);
        left.add(ping);
        add(left);

        if (Main.API.hasCapability(Capability.HANDLER_CPU_USAGE, Capability.HANDLER_RAM_USAGE)) {
            JLabel cpu = createLabel("cpu", "Cpu usage", true, true,
                    color -> UIUtils.getTrafficLight(Main.API.getCpuUsage(), 100), stats.getCpuStats());
            JLabel ram = createLabel("ram", "Ram usage", true, false,
                    color -> UIUtils.getTrafficLight(Main.API.getMemoryUsage(), 2500), stats.getMemoryStats());

            add(Box.createHorizontalStrut(5));

            JPanel right = new JPanel(new GridLayout(2, 1));
            right.setOpaque(false);
            right.add(cpu);
            right.add(ram);
            add(right);
        }

        addActionListener(l -> new DiagnosticsPanel(main, this));
    }

    private JLabel createLabel(String iconName, String tooltip, boolean alignRight, boolean format,
                               Function<Color, Color> colorFilter, StatsManager.AverageStats stat) {
        FlatSVGIcon icon = UIUtils.getSVGIcon(iconName, 15, 15);
        if (colorFilter != null) icon.setColorFilter(new FlatSVGIcon.ColorFilter(colorFilter));

        JLabel label = new JLabel(icon) {
            @Override
            public String getToolTipText() {
                return tooltip + "\n\n" + stat;
            }
        };
        ToolTipManager.sharedInstance().registerComponent(label);
        label.setFont(label.getFont().deriveFont(11f));
        label.setText("-");

        if (alignRight) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
        } else {
            label.setHorizontalAlignment(SwingConstants.LEFT);
        }

        stat.setListener(value -> {
            if (frame.isHidden()) return;
            String text = format ? Strings.ONE_PLACE_FORMAT.format(value) : String.valueOf((int) value);
            label.setText(text);
        });
        return label;
    }
}
