package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.components.ExtraButton;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.SystemUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import static com.github.manolo8.darkbot.Main.API;

public class MainGui extends JFrame {

    private final Main main;
    private final ConfigGui configGui;

    private MapDrawer mapDrawer;
    private boolean visible = true;

    private ExtraButton extraButton;
    private JButton startButton;
    private JButton toggleVisibility;
    private JButton openHome;
    private JButton openConfig;

    protected final Image icon = new ImageIcon(this.getClass().getResource("/icon.png")).getImage();

    public MainGui(Main main) throws HeadlessException {
        super("DarkBot");

        this.main = main;

        this.configGui = new ConfigGui(main);
        configGui.setAlwaysOnTop(main.config.MISCELLANEOUS.DISPLAY.ALWAYS_ON_TOP);
        configGui.setIconImage(icon);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);
        setAlwaysOnTop(main.config.MISCELLANEOUS.DISPLAY.ALWAYS_ON_TOP);
        setIconImage(icon);
        setVisible(true);

        initComponents();
        setComponentPosition();
        setComponentEvent();
    }

    private void initComponents() {
        mapDrawer = new MapDrawer(main);
        extraButton = new ExtraButton(main);
        startButton = new MainButton("START");
        toggleVisibility = new MainButton("HIDE");
        openHome = new MainButton("HOME");
        openConfig = new MainButton("CONFIG");
    }

    private void setComponentPosition() {
        getContentPane().setLayout(new MigLayout("ins 0, gap 0, wrap 5, fill", "[25%][25%][25%][25%][1%]", "[grow][]"));

        add(mapDrawer, "grow, span");
        add(openConfig, "grow");
        add(openHome, "grow");
        add(toggleVisibility, "grow");
        add(startButton, "grow");
        add(extraButton);
    }

    private void setComponentEvent() {
        addWindowStateListener(e -> {
            if (e.getNewState() == WindowEvent.WINDOW_CLOSING) {
                main.saveConfig();
            }
        });

        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                main.setRunning(!main.isRunning());
                startButton.setText(main.isRunning() ? "STOP" : "START");
            }
        });

        toggleVisibility.addActionListener(e -> {
            API.setVisible(visible = !visible);
            toggleVisibility.setText(visible ? "HIDE" : "SHOW");
        });

        openHome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String sid = main.statsManager.sid, instance = main.statsManager.instance;
                if (sid == null || sid.isEmpty() || instance == null || instance.isEmpty()) return;
                SystemUtils.openUrl(instance + "?dosid=" + sid);
            }
        });

        openConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                configGui.setVisible(true);
                configGui.toFront();
            }
        });
    }

    public void tick() {
        validate();
        repaint();
    }
}
