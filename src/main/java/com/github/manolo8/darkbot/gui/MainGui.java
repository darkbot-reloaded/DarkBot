package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import static com.github.manolo8.darkbot.Main.API;

public class MainGui extends JFrame {

    private final Main main;
    private final ConfigGui configGui;

    private MapDrawer mapDrawer;

    private boolean visible;

    private JButton toggleRunning;
    private JButton toggleVisibility;
    private JButton copySid;
    private JButton openConfig;

    public MainGui(Main main) throws HeadlessException {
        super("DarkBot");

        this.main = main;

        this.configGui = new ConfigGui(main);

        this.visible = true;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 480);
        setLocationRelativeTo(null);
        setVisible(true);

        initComponents();
        setComponentPosition();
        setComponentEvent();
    }

    private void initComponents() {
        mapDrawer = new MapDrawer(main);
        toggleRunning = new JButton("START");
        toggleVisibility = new JButton("HIDE");
        copySid = new JButton("SID");
        openConfig = new JButton("CONFIG");
    }

    private void setComponentPosition() {
        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;

        c.weightx = 1.0;
        c.weighty = 1.0;

        add(mapDrawer, c);

        c.weighty = 0;
        c.gridy = 1;

        Container container = new Container();

        container.setLayout(new GridLayout(1, 4));

        container.add(openConfig);

        container.add(copySid);

        container.add(toggleVisibility);

        container.add(toggleRunning);

        add(container, c);
    }

    private void setComponentEvent() {

        addWindowStateListener(e -> {
            if (e.getNewState() == WindowEvent.WINDOW_CLOSING) {
                main.saveConfig();
            }
        });

        toggleVisibility.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (visible) {
                    toggleVisibility.setText("SHOW");
                    API.setVisible(false);
                    visible = false;
                } else {
                    toggleVisibility.setText("HIDE");
                    API.setVisible(true);
                    visible = true;
                }
            }
        });

        toggleRunning.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                boolean running = main.isRunning();

                if (running) {
                    toggleRunning.setText("START");
                    main.setRunning(false);
                } else {
                    toggleRunning.setText("STOP");
                    main.setRunning(true);
                }
            }
        });

        copySid.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String value = main.statsManager.sid;

                if (value != null) {
                    StringSelection selection = new StringSelection(value);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                }

            }
        });

        openConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                configGui.setVisible(!configGui.isVisible());
            }
        });
    }

    public void tick() {
        validate();
        repaint();
    }
}
