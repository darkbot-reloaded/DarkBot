package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.*;
import com.github.manolo8.darkbot.core.manager.*;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.pathfinder.Area;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathFinder;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

public class MapDrawer extends JPanel {

    private final DecimalFormat formatter;

    private Color BACKGROUND = Color.decode("#263238");
    private Color TEXT = Color.decode("#F2F2F2");
    private Color TEXT_DARK = Color.decode("#707070");
    private Color GOING = Color.decode("#8F9BFF");
    private Color PORTALS = Color.decode("#AEAEAE");
    private Color OWNER = Color.decode("#FFF");
    private Color TRAIL = Color.decode("#E0E0E0");
    private Color BOXES = Color.decode("#C77800");
    private Color ALLIES = Color.decode("#29B6F6");
    private Color ENEMIES = Color.decode("#d50000");
    private Color NPCS = Color.decode("#9b0000");
    private Color PET = Color.decode("#004c8c");
    private Color PET_IN = Color.decode("#c56000");
    private Color HEALTH = Color.decode("#388e3c");
    private Color HEALTH_BACK = Color.decode("#255d27");
    private Color SHIELD = Color.decode("#0288d1");
    private Color SHIELD_BACK = Color.decode("#015c8e");

    private Font FONT_BIG = new Font("Consolas", Font.PLAIN, 32);
    private Font FONT_MID = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    private Font FONT_SMALL = new Font("Consolas", Font.PLAIN, 12);

    private double[][] lastPositions = new double[150][4];

    private HeroManager hero;
    private PathFinder pathFinder;
    private MapManager mapManager;
    private GuiManager guiManager;
    private StatsManager statsManager;
    private PingManager pingManager;
    private Config config;

    private java.util.List<Portal> portals;
    private java.util.List<Npc> npcs;
    private java.util.List<Box> boxes;
    private java.util.List<Ship> ships;
    private java.util.List<BattleStation> battleStations;

    private RenderingHints hints;

    private Location last;
    private int current;

    public MapDrawer(Main main) {
        this.hero = main.hero;
        this.pathFinder = main.hero.drive.pathFinder;
        this.mapManager = main.mapManager;
        this.guiManager = main.guiManager;
        this.statsManager = main.statsManager;
        this.pingManager = main.pingManager;
        this.config = main.config;

        this.portals = main.mapManager.entities.portals;
        this.npcs = main.mapManager.entities.npcs;
        this.boxes = main.mapManager.entities.boxes;
        this.ships = main.mapManager.entities.ships;
        this.battleStations = main.mapManager.entities.battleStations;

        this.formatter = new DecimalFormat("###,###,###");
        this.last = new Location(0, 0);

        this.hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hero.drive.move(undoTranslateX(e.getX()), undoTranslateY(e.getY()));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {

        int height = getHeight();
        int width = getWidth();

        int fontWidth;

        int mid = width / 2;

        g.setColor(BACKGROUND);
        g.fillRect(0, 0, width, height);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHints(hints);

        g2.setColor(TEXT_DARK);

        String ping = pingManager.ping + " ms";

        fontWidth = g2.getFontMetrics().stringWidth(ping);
        g2.drawString(ping, width - fontWidth - 10, 20);

        String running = "RUNNING " + statsManager.runningTimeStr();

        fontWidth = g2.getFontMetrics().stringWidth(running);
        g2.drawString(running, mid - (fontWidth / 2), (height / 2) + 35);
        g2.setFont(FONT_BIG);
        fontWidth = g2.getFontMetrics().stringWidth(hero.map.name);
        g2.drawString(hero.map.name, mid - (fontWidth / 2), (height / 2) - 5);

        g2.setColor(TEXT);
        g2.setFont(FONT_SMALL);

        int center = (height / 2) - 30;

        g2.drawString("cre/h " + formatter.format(statsManager.earnedCredits()), 0, center);
        g2.drawString("uri/h " + formatter.format(statsManager.earnedUridium()), 0, center + 15);
        g2.drawString("exp/h " + formatter.format(statsManager.earnedExperience()), 0, center + 30);
        g2.drawString("hon/h " + formatter.format(statsManager.earnedHonor()), 0, center + 45);
        g2.drawString("death " + guiManager.deaths + '/' + config.MAX_DEATHS, 0, center + 60);

        g.setColor(GOING);

        PathPoint begin = new PathPoint((int) hero.locationInfo.now.x, (int) hero.locationInfo.now.y);
        for (PathPoint path : pathFinder.path()) {

            g.drawLine(
                    translateX(path.x),
                    translateY(path.y),
                    translateX(begin.x),
                    translateY(begin.y)
            );


            begin = path;
        }


        synchronized (Main.UPDATE_LOCKER) {

            g2.setFont(FONT_SMALL);
            g2.setColor(PORTALS);
            for (Portal portal : portals) {

                Location loc = portal.locationInfo.now;

                g2.drawOval(
                        translateX(loc.x) - 5,
                        translateY(loc.y) - 5,
                        10,
                        10
                );
            }

            for (Barrier barrier : mapManager.entities.barriers) {

                //4=
                Area area = barrier.getArea();

                g.drawRect(
                        translateX(area.minX),
                        translateY(area.minY),
                        translateX(area.maxX - area.minX),
                        translateY(area.maxY - area.minY)
                );
            }

            g2.setColor(BOXES);
            for (Box box : boxes) {

                Location loc = box.locationInfo.now;

                int x = translateX(loc.x) - 1;
                int y = translateY(loc.y) - 1;

                if (box.boxInfo.collect) {
                    g2.fillRect(x, y, 4, 4);
                } else {
                    g2.drawRect(x, y, 3, 3);
                }

            }

            g2.setColor(NPCS);
            for (Npc npc : npcs) {

                Location loc = npc.locationInfo.now;

                int x = translateX(loc.x) - 1;
                int y = translateY(loc.y) - 1;

                if (npc.npcInfo.kill) {
                    g2.fillRect(x, y, 4, 4);
                } else {
                    g2.drawRect(x, y, 3, 3);
                }
            }

            for (BattleStation station : battleStations) {

                if (station.info.isEnemy()) {
                    g2.setColor(ENEMIES);
                } else {
                    g2.setColor(ALLIES);
                }

                Location loc = station.locationInfo.now;

                int x = translateX(loc.x);
                int y = translateY(loc.y);

                g2.fillRect(
                        x - 15,
                        y - 10,
                        30,
                        20
                );

                g2.setColor(Color.BLUE);
                g2.fillOval(x - 9, y - 5, 6, 6);
                g2.fillOval(x + 3, y - 5, 6, 6);

                g2.setColor(Color.RED);
                g2.fillRect(
                        x - 13,
                        y + 3,
                        26,
                        6
                );


                g2.setColor(Color.WHITE);

                for (int i = 0; i < 4; i++) {
                    g2.fillRect(
                            x - 12 + (i * 7),
                            y + 4,
                            3,
                            4
                    );
                }
            }

            for (Ship ship : ships) {

                Location loc = ship.locationInfo.now;

                if (ship.playerInfo.isEnemy()) {
                    g2.setColor(ENEMIES);
                } else {
                    g2.setColor(ALLIES);
                }

                g2.drawRect(
                        translateX(loc.x) - 1,
                        translateY(loc.y) - 1,
                        3,
                        3
                );

            }
        }

        Location heroLocation = hero.locationInfo.now;

        double distance = last.distance(heroLocation);

        if (distance > 500) {
            last = hero.locationInfo.now.copy();
        } else if (distance > 60) {

            int index = current % lastPositions.length;

            lastPositions[index][0] = heroLocation.x;
            lastPositions[index][1] = heroLocation.y;
            lastPositions[index][2] = last.x;
            lastPositions[index][3] = last.y;
            current++;

            last = heroLocation.copy();
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

        g2.setColor(OWNER);

        g2.fillOval(
                translateX(heroLocation.x) - 3,
                translateY(heroLocation.y) - 3,
                7,
                7
        );

        if (!hero.pet.removed) {

            Location loc = hero.pet.locationInfo.now;

            x = translateX(loc.x);
            y = translateY(loc.y);

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

        {
            g2.setColor(TEXT);
            g2.setFont(FONT_MID);
            fontWidth = g2.getFontMetrics().stringWidth(hero.playerInfo.username);
            g2.drawString(hero.playerInfo.username, 10 + (mid - 20) / 2 - fontWidth / 2, height - 40);

            String health = formatter.format(hero.health.hp);
            String shield = formatter.format(hero.health.shield);

            g2.setColor(HEALTH_BACK);
            g2.fillRect(10, height - 34, mid - 20, 12);
            g2.setColor(SHIELD_BACK);
            g2.fillRect(10, height - 22, mid - 20, 12);

            g2.setColor(HEALTH);
            g2.fillRect(10, height - 34, (int) (hero.health.hpPercent() * mid) - 20, 12);
            g2.setColor(SHIELD);
            g2.fillRect(10, height - 22, (int) (hero.health.shieldPercent() * mid) - 20, 12);

            g2.setFont(FONT_SMALL);
            g2.setColor(TEXT);
            fontWidth = g2.getFontMetrics().stringWidth(health);
            g2.drawString(health, 10 + (mid - 20) / 2 - fontWidth / 2, height - 24);
            fontWidth = g2.getFontMetrics().stringWidth(shield);
            g2.drawString(shield, 10 + (mid - 20) / 2 - fontWidth / 2, height - 12);

        }

        if (hero.target != null && !hero.target.removed) {
            Ship ship = hero.target;

            if (ship instanceof Npc) {
                g2.setColor(TEXT);
                g2.setFont(FONT_MID);
                String name = ((Npc) ship).playerInfo.username;

                fontWidth = g2.getFontMetrics().stringWidth(name);
                g2.drawString(name, 10 + mid + (mid - 20) / 2 - fontWidth / 2, height - 40);
            }

            String health = formatter.format(ship.health.hp);
            String shield = formatter.format(ship.health.shield);

            g2.setColor(HEALTH_BACK);
            g2.fillRect(mid + 10, height - 34, mid - 20, 12);
            g2.setColor(SHIELD_BACK);
            g2.fillRect(mid + 10, height - 22, mid - 20, 12);

            g2.setColor(HEALTH);
            g2.fillRect(mid + 10, height - 34, (int) (ship.health.hpPercent() * mid) - 20, 12);
            g2.setColor(SHIELD);
            g2.fillRect(mid + 10, height - 22, (int) (ship.health.shieldPercent() * mid) - 20, 12);

            g2.setFont(FONT_SMALL);
            g2.setColor(TEXT);
            fontWidth = g2.getFontMetrics().stringWidth(health);
            g2.drawString(health, 10 + mid + (mid - 20) / 2 - fontWidth / 2, height - 24);
            fontWidth = g2.getFontMetrics().stringWidth(shield);
            g2.drawString(shield, 10 + mid + (mid - 20) / 2 - fontWidth / 2, height - 12);
        }
    }

    private int translateX(double x) {
        return (int) ((x / (double) MapManager.internalWidth) * getWidth());
    }

    private double undoTranslateX(double x) {
        return ((x / getWidth()) * MapManager.internalWidth);
    }

    private double undoTranslateY(double y) {
        return ((y / getHeight()) * MapManager.internalHeight);
    }

    private int translateY(double y) {
        return (int) ((y / (double) MapManager.internalHeight) * getHeight());
    }
}
