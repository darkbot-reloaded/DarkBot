package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import static com.github.manolo8.darkbot.Main.API;

public class MainForm {

    private final Main main;
    public JPanel content;
    private JButton btnConfig;
    private JButton startButton;
    private JProgressBar healthBar;
    private JProgressBar shieldbar;
    private JPanel mapping;
    private JButton btnShow;

    public MainForm(Main main) {
        this.main = main;
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

                if (API.toggleBrowser()) {
                    btnShow.setText("Show");
                } else {
                    btnShow.setText("Hide");
                }

            }
        });
    }

    public void tick() {

        healthBar.setValue(main.hero.health.hp);
        healthBar.setMaximum(main.hero.health.maxHp);
        shieldbar.setValue(main.hero.health.shield);
        shieldbar.setMaximum(main.hero.health.maxShield);

        mapping.repaint();
    }

    private void createUIComponents() {
        mapping = new Mapping(main);
    }
}
