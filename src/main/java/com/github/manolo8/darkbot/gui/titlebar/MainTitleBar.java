package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Function;
import java.util.function.Supplier;

public class MainTitleBar extends JMenuBar implements SimpleMouseListener {

    private final Info info;
    private final Main main;

    public MainTitleBar(Main main, MainGui frame) {
        this.main = main;

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder());

        add(new ExtraButton(main, frame));
        add(new ConfigButton(frame));
        add(new StatsButton(frame));
        add(new StartButton(main, frame));
        add(new BackpageButton(main, frame));

        add(this.info = new Info());

        add(new HookButton(frame));
        add(new DiagnosticsButton(main, frame));
        add(new VisibilityButton(main, frame));
        add(new PinButton(frame));
        add(new TrayButton(main, frame));
    }

    public void setInfo(String info) {
        this.info.title.setText(info);
    }

    private class Info extends Box.Filler {

        private final JLabel title = new JLabel();

        Info() {
            super(new Dimension(20, 0), new Dimension(120, 0), new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            StatsManager statsManager = main.statsManager;

            add(title, BorderLayout.CENTER);
            add(createLeftPanel(statsManager), BorderLayout.WEST);
            if (Main.API.hasCapability(Capability.HANDLER_CPU_USAGE) && Main.API.hasCapability(Capability.HANDLER_RAM_USAGE)) {
                add(createRightPanel(statsManager), BorderLayout.EAST);
            }
            title.setHorizontalAlignment(SwingConstants.CENTER);
        }

        private JComponent createLeftPanel(StatsManager statsManager) {
            JPanel panel = new JPanel(new GridLayout(2, 1));

            JLabel tick = createLabel("tick_time", false, null,
                    () -> "Tick time\n\n" + statsManager.getTickStats());
            JLabel ping = createLabel("ping", false,
                    color -> {
                        float hue = 1 - Math.min(500, Math.max(1, main.statsManager.getPing())) / 500f;
                        return Color.getHSBColor(hue * 0.35f, 1f, 0.75f);
                    }, () -> "Game ping\n\n" + statsManager.getPingStats());

            statsManager.getPingStats().setListener(ping::setText);
            statsManager.getTickStats().setListener(tick::setText);

            panel.add(tick);
            panel.add(ping);

            return panel;
        }

        private JComponent createRightPanel(StatsManager statsManager) {
            JPanel panel = new JPanel(new GridLayout(2, 1));

            JLabel cpu = createLabel("cpu", true, null, () -> "CPU usage\n\n" + statsManager.getCpuStats());
            JLabel ram = createLabel("ram", true, null,
                    () -> "Process RAM usage\n\n" + statsManager.getMemoryStats()
                            + "\n\nHeap=" + (Runtime.getRuntime().totalMemory() >> 20) + "MB"
                            + "\nIn-game=" + main.facadeManager.stats.getMemory()  + "MB");

            statsManager.getCpuStats().setListener(cpu::setText);
            statsManager.getMemoryStats().setListener(ram::setText);

            panel.add(cpu);
            panel.add(ram);

            return panel;
        }

        private JLabel createLabel(String iconName, boolean alignRight,
                                   Function<Color, Color> colorFilter, Supplier<String> tooltipSupplier) {
            FlatSVGIcon icon = UIUtils.getSVGIcon(iconName, 10, 10);
            if (colorFilter != null) icon.setColorFilter(new FlatSVGIcon.ColorFilter(colorFilter));

            JLabel label = new JLabel(icon) {
                @Override
                public String getToolTipText(MouseEvent e) {
                    return tooltipSupplier.get();
                }
            };
            ToolTipManager.sharedInstance().registerComponent(label);
            label.setFont(label.getFont().deriveFont(10f));

            if (alignRight) {
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setHorizontalTextPosition(SwingConstants.LEFT);
            } else label.setHorizontalAlignment(SwingConstants.LEFT);

            Dimension preferredSize = label.getPreferredSize();
            label.setPreferredSize(new Dimension(40, preferredSize.height));

            label.setText("0");
            return label;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // draw separators
            g.setColor(UIUtils.darker(g.getColor(), 0.6));
            g.drawLine(0, 5, 0, getHeight() - 6);
            g.drawLine(getWidth() - 1, 5, getWidth() - 1, getHeight() - 6);
        }
    }
}
