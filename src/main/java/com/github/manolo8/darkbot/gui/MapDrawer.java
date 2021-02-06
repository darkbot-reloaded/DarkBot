package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.SafetyInfo;
import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.core.entities.Barrier;
import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.entities.BattleStation;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.FakeNpc;
import com.github.manolo8.darkbot.core.entities.Mine;
import com.github.manolo8.darkbot.core.entities.NoCloack;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.entities.bases.BaseHeadquarters;
import com.github.manolo8.darkbot.core.entities.bases.BaseStation;
import com.github.manolo8.darkbot.core.entities.bases.BaseTurret;
import com.github.manolo8.darkbot.core.manager.GuiManager;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.manager.PingManager;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.objects.facades.BoosterProxy;
import com.github.manolo8.darkbot.core.objects.itf.HealthHolder;
import com.github.manolo8.darkbot.core.objects.group.Group;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.pathfinder.RectangleImpl;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathPoint;
import com.github.manolo8.darkbot.gui.trail.Line;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.Time;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapDrawer extends JPanel {

    private final DecimalFormat STAT_FORMAT = new DecimalFormat("###,###,###");
    private final NumberFormat HEALTH_FORMAT;

    {
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setGroupingSeparator(' ');
        HEALTH_FORMAT = new DecimalFormat("###,###,###", sym);
    }

    protected ColorScheme cs = new ColorScheme();

    private final TreeMap<Long, Line> positions = new TreeMap<>();

    private Main main;
    protected HeroManager hero;
    private Drive drive;
    private MapManager mapManager;
    private GuiManager guiManager;
    private StatsManager statsManager;
    private PingManager pingManager;
    protected Config config;

    private List<Portal> portals;
    private List<Npc> npcs;
    private FakeNpc fakeNpc;
    private List<Box> boxes;
    private List<Mine> mines;
    private List<Ship> ships;
    private List<BattleStation> battleStations;
    private List<BasePoint> basePoints;

    private final RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    private Location last = new Location(0, 0);

    protected boolean hovering;
    protected int width, height, mid;

    public MapDrawer() {
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                hovering = true;
                repaint();
            }

            public void mouseExited(MouseEvent evt) {
                hovering = false;
                repaint();
            }
        });
    }

    public MapDrawer(Main main) {
        this();
        setBorder(UIUtils.getBorder());
        setup(main);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP && SwingUtilities.isLeftMouseButton(e)) {
                    main.setRunning(!main.isRunning());
                    repaint();
                    return;
                }
                hero.drive.move(undoTranslateX(e.getX()), undoTranslateY(e.getY()));
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP && SwingUtilities.isLeftMouseButton(e)) return;
                hero.drive.move(undoTranslateX(e.getX()), undoTranslateY(e.getY()));
            }
        });
    }

    public void setup(Main main) {
        this.main = main;
        this.hero = main.hero;
        this.drive = main.hero.drive;
        this.mapManager = main.mapManager;
        this.guiManager = main.guiManager;
        this.statsManager = main.statsManager;
        this.pingManager = main.pingManager;
        this.config = main.config;

        this.portals = main.mapManager.entities.portals;
        this.npcs = main.mapManager.entities.npcs;
        this.fakeNpc = main.mapManager.entities.fakeNpc;
        this.boxes = main.mapManager.entities.boxes;
        this.mines = main.mapManager.entities.mines;
        this.ships = main.mapManager.entities.ships;
        this.battleStations = main.mapManager.entities.battleStations;
        this.basePoints = main.mapManager.entities.basePoints;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (main == null) return;
        Graphics2D g2 = setupDraw(g);


        synchronized (Main.UPDATE_LOCKER) {
            drawZones(g2);
            if (hasFlag(DisplayFlag.ZONES)) drawCustomZones(g2);
            drawInfos(g2);
            drawHealth(g2);
            drawTrail(g2);
            drawStaticEntities(g2);
            drawDynamicEntities(g2);
            drawHero(g2);
        }

        if (config.BOT_SETTINGS.OTHER.DEV_STUFF) {
            g2.setFont(cs.FONTS.TINY);
            g2.setColor(cs.TEXT_DARK);
            synchronized (Main.UPDATE_LOCKER) {
                List<Entity> entities = mapManager.entities.allEntities.stream().flatMap(Collection::stream)
                        .filter(e -> e.id > 150_000_000 && e.id < 160_000_000 || e instanceof Mine)
                        .filter(e -> e.locationInfo.isLoaded())
                        .collect(Collectors.toList());

                g2.setColor(cs.TEXTS_BACKGROUND);
                for (Entity e : entities) {
                    Location loc = e.locationInfo.now;
                    int strWidth = g2.getFontMetrics().stringWidth(e.toString());
                    g2.fillRect(translateX(loc.x) - (strWidth >> 1), translateY(loc.y) - 7, strWidth, 8);
                }
                g2.setColor(cs.TEXT);
                g2.setFont(cs.FONTS.TINY);
                for (Entity e : entities) {
                    Location loc = e.locationInfo.now;
                    drawString(g2, e.toString(), translateX(loc.x), translateY(loc.y), Align.MID);
                }
            }
        }

        synchronized (Main.UPDATE_LOCKER) {
            if (!drawGroup(g2)) drawBoosters(g2);
        }

        if (hasFlag(DisplayFlag.STATS_AREA))
            drawBackgroundedText(g2, Align.LEFT,
                    "cre/h " + STAT_FORMAT.format(statsManager.earnedCredits()),
                    "uri/h " + STAT_FORMAT.format(statsManager.earnedUridium()),
                    "exp/h " + STAT_FORMAT.format(statsManager.earnedExperience()),
                    "hon/h " + STAT_FORMAT.format(statsManager.earnedHonor()),
                    "cargo " + statsManager.deposit + "/" + statsManager.depositTotal,
                    "death " + guiManager.deaths + '/' + config.GENERAL.SAFETY.MAX_DEATHS);

        if (hovering && main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP) drawActionButton(g2);
    }

    protected Graphics2D setupDraw(Graphics g) {
        cs = main.config.BOT_SETTINGS.MAP_DISPLAY.cs;

        height = getHeight();
        width = getWidth();
        mid = width / 2;

        g.setColor(cs.BACKGROUND);
        g.fillRect(0, 0, width, height);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHints(hints);
        return g2;
    }

    protected void drawZones(Graphics2D g2) {
        for (Barrier barrier : mapManager.entities.barriers) {
            if (!barrier.use()) continue;
            RectangleImpl area = barrier.getZone();
            g2.setColor(cs.BARRIER);
            g2.fillRect(
                    translateX(area.minX), translateY(area.minY),
                    translateX(area.maxX - area.minX), translateY(area.maxY - area.minY));
            g2.setColor(cs.BARRIER_BORDER);
            g2.drawRect(
                    translateX(area.minX), translateY(area.minY),
                    translateX(area.maxX - area.minX), translateY(area.maxY - area.minY));
        }

        g2.setColor(cs.NO_CLOACK);
        for (NoCloack noCloack : mapManager.entities.noCloack) {
            RectangleImpl area = noCloack.getZone();
            g2.fillRect(
                    translateX(area.minX), translateY(area.minY),
                    translateX(area.maxX - area.minX), translateY(area.maxY - area.minY));
        }
    }

    protected void drawCustomZones(Graphics2D g2) {
        g2.setColor(cs.PREFER);
        drawCustomZone(g2, config.PREFERRED.get(hero.map.id));
        if (config.GENERAL.ROAMING.SEQUENTIAL) drawCustomZonePath(g2, config.PREFERRED.get(hero.map.id));
        g2.setColor(cs.AVOID);
        drawCustomZone(g2, config.AVOIDED.get(hero.map.id));
        g2.setColor(cs.SAFETY);
        for (SafetyInfo safety : config.SAFETY.get(hero.map.id)) {
            if (safety.runMode == SafetyInfo.RunMode.NEVER
                    || safety.entity == null || safety.entity.removed) continue;
            drawSafeZone(g2, safety);
        }
    }

    private void drawInfos(Graphics2D g2) {
        g2.setColor(cs.TEXT_DARK);
        String status = I18n.get(
                (main.isRunning() ? "gui.map.running" : "gui.map.waiting"),
                Time.toString(statsManager.runningTime()));
        drawString(g2, status, mid, height / 2 + 35, Align.MID);

        g2.setFont(cs.FONTS.SMALL);
        String info = I18n.get("gui.map.info",
                Main.VERSION.toString(),
                (main.isRunning() || !config.MISCELLANEOUS.RESET_REFRESH ?
                        Time.toString(System.currentTimeMillis() - main.lastRefresh) : "00"),
                Time.toString(config.MISCELLANEOUS.REFRESH_TIME * 60 * 1000L));
        drawString(g2, info, 5, 12, Align.LEFT);
        if (main.module != null) {
            drawString(g2, main.tickingModule ? main.module.getStatus() : main.module.getStoppedStatus(), 5, 26, Align.LEFT);
        }

        drawString(g2, String.format("%.1ftick %dms ping", main.avgTick, pingManager.ping), width - 5, 12, Align.RIGHT);
        drawString(g2, "SID: " + main.backpage.sidStatus(), width - 5, 26, Align.RIGHT);

        drawMap(g2);
    }

    protected void drawMap(Graphics2D g2) {
        g2.setColor(cs.TEXT_DARK);
        g2.setFont(cs.FONTS.BIG);
        drawString(g2, hero.map.name, mid, (height / 2) - 5, Align.MID);
    }

    private void drawHealth(Graphics2D g2) {
        g2.setColor(cs.TEXT);
        g2.setFont(cs.FONTS.MID);
        if (hasFlag(DisplayFlag.HERO_NAME))
            drawString(g2, hero.playerInfo.username, 10 + (mid - 20) / 2, height - 40, Align.MID);
        drawHealth(g2, hero.health, 10, this.getHeight() - 34, mid - 20, 12);

        if (hero.target != null && !hero.target.removed) {
            if (hero.target instanceof Npc || hero.target.playerInfo.isEnemy()) g2.setColor(cs.ENEMIES);
            else g2.setColor(cs.ALLIES);
            g2.setFont(cs.FONTS.MID);
            String name = hero.target.playerInfo.username;
            drawString(g2, name, mid + 10 + (mid - 20) / 2, height - 40, Align.MID);

            drawHealth(g2, hero.target.health, mid + 10, height - 34, mid - 20, 12);
        }
    }

    private void drawTrail(Graphics2D g2) {
        Location heroLocation = hero.locationInfo.now;

        double distance = last.distance(heroLocation);

        if (distance > 500) {
            last = hero.locationInfo.now.copy();
        } else if (distance > 100) {
            positions.put(System.currentTimeMillis(), new Line(last, last = heroLocation.copy()));
        }
        positions.headMap(System.currentTimeMillis() - config.BOT_SETTINGS.MAP_DISPLAY.TRAIL_LENGTH * 1000).clear();

        if (positions.isEmpty()) return;

        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        List<List<Location>> paths = Line.getSmoothedPaths(positions.values());
        double max = paths.stream().mapToInt(Collection::size).sum() / 255d, curr = 0;
        for (List<Location> points : paths) {
            Location last = null;
            for (Location point : points) {
                g2.setColor(cs.getTrail()[(int) (curr++ / max)]);
                if (last != null) drawLine(g2, last, point);
                last = point;
            }
        }
        g2.setStroke(new BasicStroke());
    }

    protected void drawStaticEntities(Graphics2D g2) {
        g2.setColor(cs.PORTALS);
        for (Portal portal : portals) {
            Location loc = portal.locationInfo.now;
            g2.drawOval(translateX(loc.x) - 6, translateY(loc.y) - 6, 11, 11);
        }

        for (BattleStation station : this.battleStations) {
            if (station.hullId == 0) g2.setColor(cs.METEROID);
            else if (station.info.isEnemy()) g2.setColor(cs.ENEMIES);
            else g2.setColor(cs.ALLIES);

            Location loc = station.locationInfo.now;
            if (station.hullId >= 0 && station.hullId < 255)
                g2.fillOval(translateX(loc.x) - 5, translateY(loc.y) - 4, 11, 9);
            else drawEntity(g2, loc, false);
        }

        for (BasePoint base : this.basePoints) {
            Location loc = base.locationInfo.now;
            if (base instanceof BaseTurret) {
                g2.setColor(cs.BASES);
                g2.fillOval(this.translateX(loc.x) - 1, this.translateY(loc.y) - 1, 2, 2);
            } else {
                g2.setColor(cs.BASE_SPOTS);
                int radius = base instanceof BaseHeadquarters ? 3500 :
                        base instanceof BaseStation ? 3000 : 1000, half = radius / 2;
                g2.fillOval(translateX(loc.x - half), translateY(loc.y - half), translateX(radius), translateY(radius));
            }
        }
    }

    private void drawDynamicEntities(Graphics2D g2) {
        g2.setColor(cs.BOXES);
        for (Box box : boxes) {
            Location loc = box.locationInfo.now;
            drawEntity(g2, loc, box.boxInfo.collect);

            if (hasFlag(DisplayFlag.RESOURCE_NAMES))
                drawString(g2, box.type, translateX(loc.x), translateY(loc.y) - 5, Align.MID);
        }

        g2.setColor(cs.MINES);
        for (Mine mine : mines) drawEntity(g2, mine.locationInfo.now, true);

        if (config.BOT_SETTINGS.OTHER.DEV_STUFF) {
            g2.setColor(cs.GOING);
            for (Npc npc : npcs) drawLine(g2, npc.locationInfo, npc.shipInfo.destination);
            for (Ship ship : ships) drawLine(g2, ship.locationInfo, ship.shipInfo.destination);
        }

        g2.setColor(cs.NPCS);
        for (Npc npc : npcs) drawEntity(g2, npc.locationInfo.now, npc.npcInfo.kill);
        if (fakeNpc.isPingAlive()) {
            Location loc = fakeNpc.locationInfo.now;
            g2.setColor(cs.PING);
            g2.fillOval(translateX(loc.x) - 7, translateY(loc.y) - 7, 15, 15);
            g2.setColor(cs.PING_BORDER);
            g2.drawOval(translateX(loc.x) - 7, translateY(loc.y) - 7, 15, 15);
        }

        for (Ship ship : ships) {
            Location loc = ship.locationInfo.now;
            g2.setColor(ship.playerInfo.isEnemy() ? cs.ENEMIES : cs.ALLIES);
            drawEntity(g2, ship.locationInfo.now, false);
            if (hasFlag(DisplayFlag.USERNAMES))
                drawString(g2, ship.playerInfo.username, translateX(loc.x), translateY(loc.y) - 5, Align.MID);
        }

        if (hero.target != null && !hero.target.removed) {
            g2.setColor(cs.GOING);
            drawLine(g2, hero.target.locationInfo, hero.target.shipInfo.destination);
            g2.setColor(cs.TARGET);
            drawEntity(g2, hero.target.locationInfo.now, true);
        }

        if (!config.BOT_SETTINGS.OTHER.DEV_STUFF) return;

        g2.setColor(cs.UNKNOWN);
        for (Entity entity : mapManager.entities.unknown) {
            drawEntity(g2, entity.locationInfo.now, false);
        }

        for (PathPoint point : hero.drive.pathFinder.points) {
            g2.fillRect(translateX(point.x), translateY(point.y), 2, 2);
        }
    }

    private void drawHero(Graphics2D g2) {
        g2.setColor(cs.TEXT);
        g2.setFont(cs.FONTS.SMALL);
        drawString(g2, hero.config + "C", 12, height - 12, Align.LEFT);

        if (!hero.locationInfo.isLoaded()) return;

        g2.setColor(cs.GOING);
        PathPoint begin = new PathPoint((int) hero.locationInfo.now.x, (int) hero.locationInfo.now.y);
        for (PathPoint path : drive.paths) {
            g2.drawLine(translateX(begin.x), translateY(begin.y),
                    translateX(path.x), translateY((begin = path).y));
        }

        g2.setColor(cs.HERO);

        Location loc = hero.locationInfo.now;
        g2.fillOval(translateX(loc.x) - 3, translateY(loc.y) - 3, 7, 7);

        g2.setColor(cs.BARRIER_BORDER);
        g2.drawRect(translateX(mapManager.boundX), translateY(mapManager.boundY),
                translateX(mapManager.boundMaxX - mapManager.boundX),
                translateY(mapManager.boundMaxY - mapManager.boundY));

        if (hero.pet.removed || !hero.pet.locationInfo.isLoaded()) return;
        loc = hero.pet.locationInfo.now;

        int x = translateX(loc.x),
                y = translateY(loc.y);

        g2.setColor(cs.PET);
        g2.fillRect(x - 3, y - 3, 6, 6);

        g2.setColor(cs.PET_IN);
        g2.fillRect(x - 2, y - 2, 4, 4);
    }

    private boolean drawGroup(Graphics2D g2) {
        if (!hasFlag(DisplayFlag.GROUP_AREA)) return false;

        Group group = main.guiManager.group.group;
        if (group == null || !group.isValid()) return false;
        boolean hideNames = !hasFlag(DisplayFlag.GROUP_NAMES);
        drawBackgrounded(g2, 28, Align.RIGHT,
                (x, y, w, member) -> {
                    Font font = cs.FONTS.SMALL;
                    Color color = cs.TEXT;

                    Map<TextAttribute, Object> attrs = new HashMap<>();
                    attrs.put(TextAttribute.WEIGHT, member.isLeader ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
                    attrs.put(TextAttribute.STRIKETHROUGH, member.isDead ? TextAttribute.STRIKETHROUGH_ON : false);
                    attrs.put(TextAttribute.UNDERLINE, member.isLocked ? TextAttribute.UNDERLINE_ON : -1);
                    if (member.isCloacked) color = color.darker();

                    g2.setFont(font.deriveFont(attrs));
                    g2.setColor(color);
                    g2.drawString(member.getDisplayText(hideNames), x, y + 14);

                    drawHealth(g2, member.memberInfo, x, y + 18, w / 2 - 3, 4);
                    if (member.targetInfo.shipType != 0)
                        drawHealth(g2, member.targetInfo, x + (w / 2) + 3, y + 18, w / 2 - 3, 4);
                },
                member -> Math.min(g2.getFontMetrics().stringWidth(member.getDisplayText(hideNames)), 200),
                group.members);
        return true;
    }

    private void drawBoosters(Graphics2D g2) {
        if (!hasFlag(DisplayFlag.BOOSTER_AREA)) return;

        Stream<BoosterProxy.Booster> boosters = main.facadeManager.booster.boosters.stream().filter(b -> b.amount > 0);
        if (hasFlag(DisplayFlag.SORT_BOOSTERS))
            boosters = boosters.sorted(Comparator.comparingDouble(b -> -b.cd));

        drawBackgrounded(g2, 15, Align.RIGHT,
                (x, y, w, booster) -> {
                    g2.setColor(booster.getColor());
                    g2.drawString(booster.toSimpleString(), x, y + 14);
                },
                b -> g2.getFontMetrics().stringWidth(b.toSimpleString()),
                boosters.collect(Collectors.toList()));
    }

    private void drawActionButton(Graphics2D g2) {
        g2.setColor(cs.DARKEN_BACK);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.setColor(cs.ACTION_BUTTON);
        int height2 = this.getHeight() / 2, height3 = this.getHeight() / 3,
                width3 = this.getWidth() / 3, width9 = this.getWidth() / 9;
        if (this.main.isRunning()) {
            g2.fillRect(width3, height3, width9, height3); // Two vertical parallel lines
            g2.fillRect((width3 * 2) - width9, height3, width9, height3);
        } else { // A "play" triangle
            g2.fillPolygon(new int[]{width3, width3 * 2, width3},
                    new int[]{height3, height2, height3 * 2}, 3);
        }
    }

    private void drawBackgroundedText(Graphics2D g2, Align align, String... texts) {
        this.drawBackgrounded(g2, 15, align,
                (x, y, h, str) -> g2.drawString(str, x, y + 14),
                g2.getFontMetrics()::stringWidth,
                Arrays.asList(texts));
    }

    private <T> void drawBackgrounded(Graphics2D g2, int lineHeight, Align align,
                                      Renderer<T> renderer,
                                      ToIntFunction<T> widthGetter,
                                      Collection<T> toRender) {
        if (toRender.size() == 0) return;
        g2.setFont(cs.FONTS.SMALL);

        int width = toRender.stream().mapToInt(widthGetter).max().orElse(0) + 8;
        int height = toRender.size() * lineHeight + 4;
        int top = getHeight() / 2 - height / 2;
        int left = align == Align.RIGHT ? getWidth() - width : 0;

        g2.setColor(cs.TEXTS_BACKGROUND);
        g2.fillRect(left, top, width, height);
        g2.setColor(cs.TEXT);
        for (T render : toRender) {
            renderer.render(left + 4, top, width - 8, render);
            top += lineHeight;
        }
    }

    @FunctionalInterface
    private interface Renderer<T> {
        void render(int x, int y, int w, T object);
    }

    protected void drawCustomZone(Graphics2D g2, ZoneInfo zoneInfo) {
        if (zoneInfo == null) return;
        for (int x = 0; x < zoneInfo.resolution; x++) {
            for (int y = 0; y < zoneInfo.resolution; y++) {
                if (!zoneInfo.get(x, y)) continue;
                int startX = gridToMapX(x), startY = gridToMapY(y);
                g2.fillRect(startX, startY, gridToMapX(x + 1) - startX, gridToMapY(y + 1) - startY);
            }
        }
    }

    protected void drawCustomZonePath(Graphics2D g2, ZoneInfo zoneInfo) {
        if (zoneInfo == null) return;
        List<ZoneInfo.Zone> zones = zoneInfo.getSortedZones();
        for (int i = 0; i < zones.size(); i++) {
            Location loc1 = zones.get(i).innerPoint(0.5, 0.5, MapManager.internalWidth, MapManager.internalHeight);
            Location loc2 = zones.get((i + 1) % zones.size()).innerPoint(0.5, 0.5, MapManager.internalWidth, MapManager.internalHeight);
            drawLine(g2, loc1, loc2);
        }
    }

    protected void drawSafeZone(Graphics2D g2, SafetyInfo safetyInfo) {
        if (safetyInfo == null) return;
        int radius = safetyInfo.radius();
        g2.fillOval(translateX(safetyInfo.x - radius), translateY(safetyInfo.y - radius),
                translateX(safetyInfo.diameter()), translateY(safetyInfo.diameter()));
    }

    private void drawHealth(Graphics2D g2, HealthHolder health, int x, int y, int width, int height) {
        g2.setFont(cs.FONTS.SMALL);

        boolean displayAmount = height >= 8 && hasFlag(DisplayFlag.HP_SHIELD_NUM);
        int margin = height < 8 ? 2 : 0;

        int totalMaxHealth = health.getMaxHp() + health.getHull();
        int hullWidth = totalMaxHealth == 0 ? 0 : (health.getHull() * width / totalMaxHealth);

        g2.setColor(cs.HEALTH.darker());
        g2.fillRect(x, y, width, height);
        g2.setColor(cs.HEALTH);
        g2.fillRect(x, y, hullWidth + (int) (health.hpPercent() * (width - hullWidth)), height);
        g2.setColor(cs.NANO_HULL);
        g2.fillRect(x, y, hullWidth, height);

        g2.setColor(cs.TEXT);
        if (displayAmount)
            drawString(g2, HEALTH_FORMAT.format(health.getHull() + health.getHp()) + "/" +
                    HEALTH_FORMAT.format(totalMaxHealth), x + width / 2, y + height - 2, Align.MID);

        if (health.getMaxShield() != 0) {
            g2.setColor(cs.SHIELD.darker());
            g2.fillRect(x, y + height + margin, width, height);
            g2.setColor(cs.SHIELD);
            g2.fillRect(x, y + height + margin, (int) (health.shieldPercent() * width), height);
            g2.setColor(cs.TEXT);
            if (displayAmount)
                drawString(g2, HEALTH_FORMAT.format(health.getShield()) + "/" +
                        HEALTH_FORMAT.format(health.getMaxShield()), x + width / 2, y + height + height - 2, Align.MID);
        }
    }

    protected enum Align {
        LEFT, MID, RIGHT
    }

    protected void drawString(Graphics2D g2, String str, int x, int y, Align align) {
        if (str == null || str.isEmpty()) return;
        if (align != Align.LEFT) {
            int strWidth = g2.getFontMetrics().stringWidth(str);
            x -= strWidth >> (align == Align.MID ? 1 : 0);
        }
        g2.drawString(str, x, y);
    }

    private void drawLine(Graphics2D g2, LocationInfo a, LocationInfo b) {
        if (!a.isLoaded() || !b.isLoaded()) return;
        drawLine(g2, a.now, b.now);
    }

    private void drawLine(Graphics2D g2, Location a, Location b) {
        g2.drawLine(translateX(a.x), translateY(a.y), translateX(b.x), translateY(b.y));
    }

    private void drawEntity(Graphics2D g2, Location loc, boolean fill) {
        int x = this.translateX(loc.x) - 1;
        int y = this.translateY(loc.y) - 1;
        if (fill) g2.fillRect(x, y, 4, 4);
        else g2.drawRect(x, y, 3, 3);
    }
    
    private boolean hasFlag(DisplayFlag df) {
        return config.BOT_SETTINGS.MAP_DISPLAY.TOGGLE.contains(df);
    }

    protected int translateX(double x) {
        return (int) ((x / (double) MapManager.internalWidth) * getWidth());
    }

    protected int translateY(double y) {
        return (int) ((y / (double) MapManager.internalHeight) * getHeight());
    }

    protected double undoTranslateX(double x) {
        return ((x / (double) getWidth()) * MapManager.internalWidth);
    }

    protected double undoTranslateY(double y) {
        return ((y / (double) getHeight()) * MapManager.internalHeight);
    }

    protected int gridToMapX(int x) {
        return x * width / config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION;
    }

    protected int gridToMapY(int y) {
        return y * height / config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION;
    }

    protected int mapToGridX(int x) {
        return (x + 1) * config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION / width;
    }

    protected int mapToGridY(int y) {
        return (y + 1) * config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION / height;
    }
}
