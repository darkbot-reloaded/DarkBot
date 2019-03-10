package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.components.MainButton;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;

import static com.github.manolo8.darkbot.Main.API;

public class MainGui extends JFrame {

    private final Main main;
    private final ConfigGui configGui;

    private MapDrawer mapDrawer;

    private boolean visible;

    private JButton toggleRunning;
    private JButton toggleVisibility;
    private JButton reload;
    private JButton copySid;
    private JButton openConfig;

    protected final Image icon = new ImageIcon(this.getClass().getResource("/icon.png")).getImage();

    public MainGui(Main main) throws HeadlessException {
        super("DarkBot");

        this.main = main;

        this.configGui = new ConfigGui(main);
        configGui.setAlwaysOnTop(main.config.MISCELLANEOUS.ALWAYS_ON_TOP);
        configGui.setIconImage(icon);

        this.visible = true;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);
        setAlwaysOnTop(main.config.MISCELLANEOUS.ALWAYS_ON_TOP);
        setIconImage(icon);
        setVisible(true);

        initComponents();
        setComponentPosition();
        setComponentEvent();
    }

    private void initComponents() {
        mapDrawer = new MapDrawer(main);
        toggleRunning = new MainButton("START");
        toggleVisibility = new MainButton("HIDE");
        reload = new MainButton("RELOAD");
        copySid = new MainButton("SID");
        openConfig = new MainButton("CONFIG");
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
        container.add(reload);
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

        reload.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                API.refresh();
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
                String sid = main.statsManager.sid;

                if (sid == null) return;

                StringSelection selection = new StringSelection(sid);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

                String sv = main.config.MISCELLANEOUS.SERVER_PREFIX;
                if (sv == null) return;
                try {
                    Desktop.getDesktop().browse(URI.create("https://" + sv + ".darkorbit.com?dosid=" + sid));
                } catch (IOException ex) {
                    ex.printStackTrace();
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
