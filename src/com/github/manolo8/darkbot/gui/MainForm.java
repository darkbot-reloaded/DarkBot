package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import static com.github.manolo8.darkbot.Main.API;

public class MainForm {

    private final Main main;
    private JFrame frame;
    public JPanel content;
    private JButton btnConfig;
    private JButton startButton;
    private JPanel mapping;
    private JButton btnShow;
    private JButton btnSid;
    private boolean visible;

    public MainForm(Main main) {
        this.main = main;

        this.visible = true;

        frame = new JFrame("DarkBOT");

        frame.setContentPane(content);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 480);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        this.main.status.add(value -> {
            startButton.setText(value ? "Stop" : "Start");
        });

        try {
            frame.setIconImage(ImageIO.read(getClass().getResource("/resources/icon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }


        btnConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                ConfigForm form = new ConfigForm(main);

                JFrame frame = new JFrame("Config");

                frame.setContentPane(form.content);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(640, 480);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        main.config.changed = true;
                    }
                });

                try {
                    frame.setIconImage(ImageIO.read(getClass().getResource("/resources/icon.png")));
                } catch (IOException ignored) {
                }
            }
        });
        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                main.setRunning(!main.isRunning());
            }
        });
        btnShow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (visible) {
                    btnShow.setText("Show");
                    API.setVisible(false);
                    visible = false;
                } else {
                    btnShow.setText("Hide");
                    API.setVisible(true);
                    visible = true;
                }

            }
        });
        btnSid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                String value = main.statsManager.sid;

                if (value != null) {
                    StringSelection selection = new StringSelection(value);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                }

            }
        });
    }

    public void tick() {
        mapping.repaint();
    }

    private void createUIComponents() {
        mapping = new Mapping(main);
    }
}
