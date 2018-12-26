package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import static com.github.manolo8.darkbot.Main.API;

public class MainForm {

    private final Main main;
    private JFrame frame;
    public JPanel content;
    private JButton btnConfig;
    private JButton startButton;
    private JProgressBar healthBar;
    private JProgressBar shieldbar;
    private JPanel mapping;
    private JButton btnShow;
    private JButton btnSid;

    private String nick = "";

    public MainForm(Main main) {
        this.main = main;

        frame = new JFrame("DarkBOT");

        frame.setContentPane(content);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 480);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

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
                frame.setSize(360, 480);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                try {
                    frame.setIconImage(ImageIO.read(getClass().getResource("/resources/icon.png")));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }


            }
        });
        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                main.setRunning(!main.isRunning());

                startButton.setText(main.isRunning() ? "Stop" : "Start");

            }
        });
        btnShow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

//                if (API.toggleBrowser()) {
//                    btnShow.setText("Show");
//                } else {
//                    btnShow.setText("Hide");
//                }

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

        healthBar.setValue(main.hero.health.hp);
        healthBar.setMaximum(main.hero.health.maxHp);
        shieldbar.setValue(main.hero.health.shield);
        shieldbar.setMaximum(main.hero.health.maxShield);

        if (main.hero.playerInfo.username != null && !main.hero.playerInfo.username.equals(nick)) {
            nick = main.hero.playerInfo.username;
            frame.setTitle("DarkBOT - " + nick);
        }

        mapping.repaint();
    }

    private void createUIComponents() {
        mapping = new Mapping(main);
    }
}
