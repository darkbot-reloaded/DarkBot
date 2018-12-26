package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.*;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.Location;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

public class Mapping extends JPanel {

    private final Main main;
    private final DecimalFormat formatter;

    private Color BACKGROUND = Color.decode("#263238");
    private Color TEXT = Color.decode("#F2F2F2");
    private Color TEXT_DARK = Color.decode("#707070");
    private Color GOING = Color.decode("#8F9BFF");
    private Color PORTALS = Color.decode("#AEAEAE");
    private Color OWNER = Color.decode("#FFF");
    private Color TRAIL = Color.decode("#E0E0E0");
    private Color CARGO_BOX = Color.decode("#C77800");
    private Color BONUS_BOX = Color.decode("#0077C2");
    private Color ALLIES = Color.decode("#29B6F6");
    private Color ENEMIES = Color.decode("#d50000");
    private Color NPCS = Color.decode("#9b0000");
    private Color PET = Color.decode("#004c8c");
    private Color PET_IN = Color.decode("#c56000");
    private Color HEALTH = Color.decode("#388e3c");
    private Color SHIELD = Color.decode("#0288d1");


    private double[][] lastPositions = new double[150][4];
    private Location last;
    private int current;

    public Mapping(Main main) {
        this.main = main;
        this.formatter = new DecimalFormat("###,###,###");
        this.last = new Location(0, 0);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {

        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setColor(BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2 = (Graphics2D) g.create();


        g2.setRenderingHints(hints);

        g2.setColor(TEXT_DARK);

        String running = "RUNNING " + main.statsManager.runningTimeStr();
        int width = g2.getFontMetrics().stringWidth(running);
        g2.drawString(running, (getWidth() / 2) - (width / 2), (getHeight() / 2) + 35);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        width = g2.getFontMetrics().stringWidth(main.hero.map.name);
        g2.drawString(main.hero.map.name, (getWidth() / 2) - (width / 2), (getHeight() / 2) - 5);

        g2.setColor(TEXT);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g2.drawString("X " + (int) main.hero.location.x + " Y " + (int) main.hero.location.y + " CONF " + main.hero.config + " DEATHS " + main.guiManager.revives, 0, 10);

        int center = (getHeight() / 2) - 30;

        g2.drawString("+" + formatter.format(main.statsManager.earnedCredits()) + " cre/h", 0, center);
        g2.drawString("+" + formatter.format(main.statsManager.earnedUridium()) + " uri/h", 0, center + 15);
        g2.drawString("+" + formatter.format(main.statsManager.earnedExperience()) + " exp/h", 0, center + 30);
        g2.drawString("+" + formatter.format(main.statsManager.earnedHonor()) + " hon/h", 0, center + 45);

        if (main.hero.target != null && !main.hero.target.removed) {

            Ship ship = main.hero.target;

            g2.setColor(TEXT);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
            if (ship instanceof Npc) {
                g2.drawString(((Npc) ship).playerInfo.username, 0, getHeight() - 40);
            }

            g2.setColor(HEALTH);
            g2.fillRect(0, getHeight() - 30, (int) (ship.health.hpPercent() * (getWidth())), 15);

            g2.setColor(SHIELD);
            g2.fillRect(0, getHeight() - 15, (int) (ship.health.shieldPercent() * (getWidth())), 15);

        }

        synchronized (Main.UPDATE_LOCKER) {

            g2.setColor(PORTALS);
            for (Portal portal : main.mapManager.portals) {
                g2.drawOval(
                        translateX(portal.location.x) - 5,
                        translateY(portal.location.y) - 5,
                        10,
                        10
                );
            }

            g2.setColor(BONUS_BOX);
            for (Box box : main.mapManager.boxes) {
                g2.drawRect(
                        translateX(box.location.x) - 1,
                        translateY(box.location.y) - 1,
                        3,
                        3
                );
            }

            g2.setColor(NPCS);
            for (Npc npc : main.mapManager.npcs) {
                g2.drawRect(
                        translateX(npc.location.x) - 1,
                        translateY(npc.location.y) - 1,
                        3,
                        3
                );
            }

            for (BattleStation station : main.mapManager.battleStations) {

                if (station.info.isEnemy()) {
                    g2.setColor(ENEMIES);
                } else {
                    g2.setColor(ALLIES);
                }

                g2.drawRect(
                        translateX(station.location.x) - 2,
                        translateY(station.location.y) - 2,
                        4,
                        4
                );
            }

            for (Ship ship : main.mapManager.ships) {

                if (ship.playerInfo.isEnemy()) {
                    g2.setColor(ENEMIES);
                } else {
                    g2.setColor(ALLIES);
                }

                g2.drawRect(
                        translateX(ship.location.x) - 1,
                        translateY(ship.location.y) - 1,
                        3,
                        3
                );
            }
        }

        g2.setColor(OWNER);
        HeroManager hero = main.hero;

        g2.fillOval(
                translateX(main.hero.location.x) - 2,
                translateY(main.hero.location.y) - 2,
                5,
                5
        );

        if (last.distance(main.hero) > 500) {
            last = main.hero.location.add(0, 0);
        } else {

            lastPositions[current % lastPositions.length][0] = hero.location.x;
            lastPositions[current % lastPositions.length][1] = hero.location.y;
            lastPositions[current % lastPositions.length][2] = last.x;
            lastPositions[current % lastPositions.length][3] = last.y;
            current++;

            last = hero.location.add(0, 0);
        }

        g2.setColor(TRAIL);

        int x, y;

        for (double[] lastPosition : lastPositions) {
            g2.drawLine(
                    translateX(lastPosition[0]),
                    translateY(lastPosition[1]),
                    translateX(lastPosition[2]),
                    translateY(lastPosition[3])
            );
        }

        if (main.hero.going != null) {

            g2.setColor(GOING);

            g2.drawLine(
                    translateX(main.hero.location.x),
                    translateY(main.hero.location.y),
                    translateX(main.hero.going.x),
                    translateY(main.hero.going.y)
            );

            g2.fillOval(translateX(main.hero.going.x) - 2, translateY(main.hero.going.y) - 2, 5, 5);

        }

        if (!main.hero.pet.removed) {
            x = translateX(main.hero.pet.location.x);
            y = translateY(main.hero.pet.location.y);

            g2.setColor(PET);
            g2.fillRect(
                    x - 3,
                    y - 3,
                    6,
                    6
            );

            g2.setColor(PET_IN);
            g2.fillRect(
                    x - 2,
                    y - 2,
                    4,
                    4
            );
        }
    }

    private int translateX(double x) {
        return (int) ((x / (double) main.mapManager.internalWidth) * getWidth());
    }


    private int translateY(double y) {
        return (int) ((y / (double) main.mapManager.internalHeight) * getHeight());
    }
}
