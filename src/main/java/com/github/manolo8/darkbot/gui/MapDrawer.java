package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.*;
import com.github.manolo8.darkbot.core.manager.*;
import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.pathfinder.Area;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathFinder;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathPoint;
import com.github.manolo8.darkbot.utils.Time;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class MapDrawer extends JPanel {

    private final DecimalFormat formatter;

    private Color BACKGROUND = Color.decode("#263238");
    private Color TEXT = Color.decode("#F2F2F2");
    private Color TEXT_DARK = Color.decode("#CCCCCC");
    private Color GOING = Color.decode("#8F9BFF");
    private Color PORTALS = Color.decode("#AEAEAE");
    private Color OWNER = Color.decode("#22CC22");
    private Color[] TRAIL = IntStream.rangeClosed(1, 255).mapToObj(i -> new Color(224, 224, 224, i)).toArray(Color[]::new);
    private Color BOXES = Color.decode("#BBB830");
    private Color ALLIES = Color.decode("#29B6F6");
    private Color ENEMIES = Color.decode("#d50000");
    private Color NPCS = Color.decode("#AA4040");
    private Color PET = Color.decode("#004c8c");
    private Color PET_IN = Color.decode("#c56000");
    private Color HEALTH = Color.decode("#388e3c");
    private Color SHIELD = Color.decode("#0288d1");
    private Color METEROID = Color.decode("#AAAAAA");
    private Color BARRIER = new Color(255, 255, 255, 32);
    private Color BARRIER_BORDER = new Color(255, 255, 255, 128);
    private Color NO_CLOACK = new Color(23, 128, 255, 32);
    private Color BASES = Color.decode("#00D14E");
    private Color UNKNOWN = Color.decode("#7C05D1");
    private Color STATS_BACKGROUND = new Color(38, 50, 56, 128);

    private Font FONT_BIG = new Font("Consolas", Font.PLAIN, 32);
    private Font FONT_MID = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    private Font FONT_SMALL = new Font("Consolas", Font.PLAIN, 12);

    private TreeMap<Long, Line> positions = new TreeMap<>();

    private Main main;
    private HeroManager hero;
    private PathFinder pathFinder;
    private MapManager mapManager;
    private GuiManager guiManager;
    private StatsManager statsManager;
    private PingManager pingManager;
    private Config config;

    private List<Portal> portals;
    private List<Npc> npcs;
    private List<Box> boxes;
    private List<Ship> ships;
    private List<BattleStation> battleStations;
    private List<BasePoint> basePoints;

    private RenderingHints hints;

    private Location last;
    private class Line {
        double x1, x2, y1, y2;
        Line(Location loc1, Location loc2) {
            this.x1 = loc1.x;
            this.x2 = loc2.x;
            this.y1 = loc1.y;
            this.y2 = loc2.y;
        }
        void draw(Graphics2D g2) {
            drawLine(g2, x1, y1, x2, y2);
        }
    }

    public MapDrawer(Main main) {
        this.main = main;
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
        this.basePoints = main.mapManager.entities.basePoints;

        this.formatter = new DecimalFormat("###,###,###");
        this.last = new Location(0, 0);

        this.hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        hints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hero.drive.move(undoTranslateX(e.getX()), undoTranslateY(e.getY()));
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
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

        synchronized (Main.UPDATE_LOCKER) {
            for (Barrier barrier : mapManager.entities.barriers) {
                Area area = barrier.getZone();
                g2.setColor(this.BARRIER);
                g2.fillRect(
                        translateX(area.minX), translateY(area.minY),
                        translateX(area.maxX - area.minX), translateY(area.maxY - area.minY));
                g2.setColor(BARRIER_BORDER);
                g2.drawRect(
                        translateX(area.minX), translateY(area.minY),
                        translateX(area.maxX - area.minX), translateY(area.maxY - area.minY));
            }

            for (NoCloack noCloack : mapManager.entities.noCloack) {
                Area area = noCloack.getZone();
                g2.setColor(this.NO_CLOACK);
                g2.fillRect(
                        translateX(area.minX), translateY(area.minY),
                        translateX(area.maxX - area.minX), translateY(area.maxY - area.minY));
            }
        }


        g2.setColor(TEXT_DARK);

        String ping = pingManager.ping + " ms";

        fontWidth = g2.getFontMetrics().stringWidth(ping);
        g2.drawString(ping, width - fontWidth - 10, 20);

        String running = (main.isRunning() ? "RUNNING " : "WAITING ") + Time.toString(statsManager.runningTime());
        drawString(g2, running,mid, height / 2 + 35);

        g2.setFont(FONT_BIG);
        drawString(g2, hero.map.name, mid, (height / 2) - 5);

        g2.setColor(TEXT);
        g2.setFont(FONT_SMALL);
        g2.drawString(String.format(" v1.13 beta4 - Tick avg: %.1fms", main.avgTick), 0, 12);
        g2.drawString(" CONF " + this.main.hero.config + " - " +
                        "Refresh: " + (main.isRunning() ? Time.toString(System.currentTimeMillis() - main.lastRefresh) : "-") +
                        "/" + Time.toString(config.MISCELLANEOUS.REFRESH_TIME * 60 * 1000)
                , 0, 12 + 15);


        g2.setColor(TEXT);
        g2.setFont(FONT_MID);
        if (!config.MISCELLANEOUS.HIDE_NAME)
            drawString(g2, hero.playerInfo.username, 10 + (mid - 20) / 2, height - 40);
        drawHealth(g2, hero.health, 10, this.getHeight() - 34, mid - 20, 12);

        if (hero.target != null && !hero.target.removed) {
            Ship ship = hero.target;

            if (ship instanceof Npc) {
                g2.setColor(ENEMIES);
                g2.setFont(FONT_MID);
                String name = ((Npc) ship).playerInfo.username;
                drawString(g2, name, mid + 10 + (mid - 20) / 2, height - 40);
            }

            drawHealth(g2, hero.target.health, mid + 10, height - 34, mid - 20, 12);
        }

        Location heroLocation = hero.locationInfo.now;

        double distance = last.distance(heroLocation);

        if (distance > 500) {
            last = hero.locationInfo.now.copy();
        } else if (distance > 60) {
            positions.put(System.currentTimeMillis(), new Line(last, last = heroLocation.copy()));
        }
        positions.headMap(System.currentTimeMillis() - config.MISCELLANEOUS.TRAIL_LENGTH * 1000).clear();

        double max = positions.size() / 255d, curr = 0;
        for (Line line : positions.values()) {
            g2.setColor(TRAIL[(int) (curr++ / max)]);
            line.draw(g2);
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

            for (BattleStation station : this.battleStations) {
                if (station.hullId == 0) g2.setColor(this.METEROID);
                else if (station.info.isEnemy()) g2.setColor(this.ENEMIES);
                else g2.setColor(this.ALLIES);

                Location loc = station.locationInfo.now;
                int x = translateX(loc.x);
                int y = translateY(loc.y);
                if (station.hullId >= 0 && station.hullId < 255)
                    g2.fillOval(x - 5, y - 4, 11, 9);
                else drawEntity(g2, loc, false);
            }

            g2.setColor(this.BASES);
            for (BasePoint base : this.basePoints) {
                Location loc = base.locationInfo.now;
                g2.fillOval(this.translateX(loc.x) - 2, this.translateY(loc.y) - 2, 4, 4);
            }

            g2.setColor(BOXES);
            for (Box box : boxes) {
                drawEntity(g2, box.locationInfo.now, box.boxInfo.collect);
            }

            g2.setColor(NPCS);
            for (Npc npc : npcs) {
                drawEntity(g2, npc.locationInfo.now, npc.npcInfo.kill);
            }

            for (Ship ship : ships) {
                if (ship.playerInfo.isEnemy()) g2.setColor(ENEMIES);
                else g2.setColor(ALLIES);
                drawEntity(g2, ship.locationInfo.now, false);
            }

            /*g2.setColor(UNKNOWN);
            for (Entity entity : mapManager.entities.unknown) {
                Location loc = entity.locationInfo.now;
                drawString(g2, entity.id + "", translateX(loc.x), translateY(loc.y));
                drawEntity(g2, entity.locationInfo.now, false);
            }*/
        }

        g2.setColor(GOING);
        PathPoint begin = new PathPoint((int) hero.locationInfo.now.x, (int) hero.locationInfo.now.y);
        for (PathPoint path : pathFinder.path()) {
            g2.drawLine(translateX(begin.x), translateY(begin.y),
                    translateX(path.x), translateY((begin = path).y));
        }

        g2.setColor(OWNER);

        g2.fillOval(
                translateX(heroLocation.x) - 3,
                translateY(heroLocation.y) - 3,
                7,
                7
        );

        if (!hero.pet.removed && this.guiManager.pet.active()) {
            Location loc = hero.pet.locationInfo.now;

            int x = translateX(loc.x);
            int y = translateY(loc.y);

            g2.setColor(PET);
            g2.fillRect(x - 3, y - 3, 6, 6);

            g2.setColor(PET_IN);
            g2.fillRect(x - 2, y - 2, 4, 4);
        }

        drawStats(g2,
                "cre/h " + formatter.format(statsManager.earnedCredits()),
                "uri/h " + formatter.format(statsManager.earnedUridium()),
                "exp/h " + formatter.format(statsManager.earnedExperience()),
                "hon/h " + formatter.format(statsManager.earnedHonor()),
                "cargo " + statsManager.deposit + "/" + statsManager.depositTotal,
                "death " + guiManager.deaths + '/' + config.MAX_DEATHS);

    }

    private void drawStats(Graphics2D g2, String... stats) {
        int top = this.getHeight() / 2 - (stats.length * 15) / 2,
                width = Arrays.stream(stats).mapToInt(g2.getFontMetrics()::stringWidth).max().orElse(0);
        g2.setColor(STATS_BACKGROUND);
        g2.fillRect(0, top, width + 3, stats.length * 15 + 3);
        g2.setColor(TEXT);
        g2.setFont(FONT_SMALL);
        for (String str : stats) g2.drawString(str, 0, top += 15);
    }

    private void drawHealth(Graphics2D g2, Health health, int x, int y, int width, int height) {
        g2.setColor(HEALTH.darker());
        g2.fillRect(x, y, width, height);
        g2.setColor(SHIELD.darker());
        g2.fillRect(x, y + height, width, height);
        g2.setColor(HEALTH);
        g2.fillRect(x, y, (int) (health.hpPercent() * width), height);
        g2.setColor(SHIELD);
        g2.fillRect(x, y + height, (int) (health.shieldPercent() * width), height);
        g2.setColor(TEXT);
        g2.setFont(FONT_SMALL);
        drawString(g2, health.hp + "/" + health.maxHp, x + width / 2, y + height - 2);
        drawString(g2, health.shield + "/" + health.maxShield, x + width / 2, y + height + height - 2);
    }

    private void drawString(Graphics2D g2, String str, int midX, int y) {
        int strWidth = g2.getFontMetrics().stringWidth(str);
        g2.drawString(str, midX - (strWidth / 2), y);
    }

    private void drawLine(Graphics2D g2, double x1, double y1, double x2, double y2) {
        g2.draw(new Line2D.Double(translateXd(x1), translateYd(y1), translateXd(x2), translateYd(y2)));
    }

    private void drawEntity(Graphics2D g2, Location loc, boolean fill) {
        int x = this.translateX(loc.x) - 1;
        int y = this.translateY(loc.y) - 1;
        if (fill) g2.fillRect(x, y, 3, 3);
        else g2.drawRect(x, y, 3, 3);
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

    private double translateXd(double x) {
        return x / (double) MapManager.internalWidth * (double) this.getWidth();
    }

    private double translateYd(double y) {
        return y / (double) MapManager.internalHeight * (double) this.getHeight();
    }
}
