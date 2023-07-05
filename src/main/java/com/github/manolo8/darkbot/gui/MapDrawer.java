package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.FlashResManager;
import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.extensions.features.handlers.DrawableHandler;
import com.github.manolo8.darkbot.gui.titlebar.RefreshButton;
import com.github.manolo8.darkbot.modules.TemporalPortalJumper;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.StarSystemAPI;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class MapDrawer extends JPanel {

    private static final RenderingHints RENDERING_HINTS =
            new RenderingHints(Map.of(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON));

    static {
        // tests needed - may increase CPU usage for some users
        /*Toolkit toolkit = Toolkit.getDefaultToolkit();
        RenderingHints hints = (RenderingHints) toolkit.getDesktopProperty("awt.font.desktophints");

        if (hints != null) {
            RENDERING_HINTS.add(hints);
            toolkit.addPropertyChangeListener("awt.font.desktophints",
                    evt -> RENDERING_HINTS.add((RenderingHints) evt.getNewValue()));
        }*/
    }

    public MapGraphicsImpl mapGraphics;
    public boolean hovering;

    protected Main main;
    protected DrawableHandler drawableHandler;

    private FlashResManager flashResManager;

    private CompletableFuture<Image> minimapFuture;
    private GameMap lastMap;
    private Image backgroundImage;

    @Getter private long lastMapClick;

    public MapDrawer() {
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                hovering = true;
                if (main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP)
                    repaint();
            }

            public void mouseExited(MouseEvent evt) {
                hovering = false;
                if (main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP)
                    repaint();
            }
        });
    }

    public MapDrawer(Main main) {
        this();
        setup(main);

        this.drawableHandler = main.pluginAPI.requireInstance(DrawableHandler.class);
        this.flashResManager = main.pluginAPI.requireInstance(FlashResManager.class);

        JPopupMenu portalMenu = new JPopupMenu();
        portalMenu.setBorder(BorderFactory.createEmptyBorder());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP && SwingUtilities.isLeftMouseButton(e)) {
                    main.setRunning(!main.isRunning());
                    repaint();

                } else {
                    Locatable loc = mapGraphics.toGameLocation(e);

                    // move ship only on left button click
                    if (SwingUtilities.isRightMouseButton(e)) {
                        List<? extends Portal> portals;
                        synchronized (Main.UPDATE_LOCKER) {
                             portals = main.mapManager.entities.getPortals().stream()
                                    .filter(p -> p.distanceTo(loc) < 1000)
                                    .collect(Collectors.toList());
                        }

                        if (!portals.isEmpty()) {
                            portalMenu.removeAll();

                            // https://github.com/JFormDesigner/FlatLaf/issues/328
                            // compensate `MenuItem.textNoAcceleratorGap` which is 6 by default
                            Object iconSize = UIManager.put("MenuItem.minimumIconSize", new Dimension(6, 0));
                            Object itemWidth = UIManager.put("MenuItem.minimumWidth", 0);
                            for (Portal portal : portals) {
                                String portalText = portal.getTargetMap()
                                        .map(GameMap::getShortName)
                                        .orElse("(" + portal.getLocationInfo().getLast().toString() + ")");

                                JMenuItem item = new JMenuItem(portalText);
                                item.addActionListener(l -> main.setModule(new TemporalPortalJumper(main, portal)));
                                portalMenu.add(item);
                            }
                            UIManager.put("MenuItem.minimumIconSize", iconSize);
                            UIManager.put("MenuItem.minimumWidth", itemWidth);
                            portalMenu.show(MapDrawer.this, e.getX(), e.getY());
                        }
                    } else {
                        main.hero.drive.move(loc);
                        lastMapClick = System.currentTimeMillis();
                    }
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if ((main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP && SwingUtilities.isLeftMouseButton(e))
                        || SwingUtilities.isRightMouseButton(e)) return;

                main.hero.drive.move(mapGraphics.toGameLocation(e));
                lastMapClick = System.currentTimeMillis();
            }
        });

        setLayout(new MigLayout("insets 0px"));
        add(new RefreshButton(), "gapx 7px");
    }

    public void setup(Main main) {
        this.main = main;
        this.mapGraphics = main.pluginAPI.requireInstance(MapGraphicsImpl.class);
    }

    protected void onPaint() {
        drawBackgroundImage();

        for (Drawable drawable : drawableHandler.getDrawables()) {
            drawable.onDraw(mapGraphics);
        }

        // just ensure that is drawn always last
        if (hovering && main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP)
            drawActionButton();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (main == null) return;

        mapGraphics.setup(g, getWidth(), getHeight());

        synchronized (Main.UPDATE_LOCKER) {
            onPaint();
        }

        mapGraphics.dispose();
    }

    private void drawBackgroundImage() {
        if (!main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.ENABLED) return;

        Image bgImg;
        if (main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.USE_GAME_BACKGROUND) {
            GameMap currentMap = main.hero.getMap();

            if (currentMap != lastMap) {
                Future<?> f = minimapFuture; //prevent race condition
                if (f != null) f.cancel(true);

                lastMap = currentMap;
                backgroundImage = null;

                minimapFuture = currentMap.getId() <= 0 ? null : flashResManager.getBackgroundImage(currentMap);
                if (minimapFuture != null)
                    minimapFuture
                            .thenApply(r -> backgroundImage = r)
                            .whenComplete((r, t) -> minimapFuture = null);
            }
            bgImg = backgroundImage;

        } else {
            if (backgroundImage != null || lastMap != null) {
                backgroundImage = null;
                lastMap = null;
            }

            bgImg = main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.IMAGE.getImage();
        }

        if (bgImg != null) {
            mapGraphics.g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.OPACITY));
            mapGraphics.g2.drawImage(bgImg, 0, 0, mapGraphics.width, mapGraphics.height, this);
            mapGraphics.g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    private void drawActionButton() {
        mapGraphics.setColor("darken_back");
        mapGraphics.getGraphics2D().fillRect(0, 0, this.getWidth(), this.getHeight());

        mapGraphics.setColor("action_button");
        int height2 = this.getHeight() / 2, height3 = this.getHeight() / 3,
                width3 = this.getWidth() / 3, width9 = this.getWidth() / 9;

        if (main.isRunning()) {
            mapGraphics.g2.fillRect(width3, height3, width9, height3); // Two vertical parallel lines
            mapGraphics.g2.fillRect((width3 * 2) - width9, height3, width9, height3);
        } else { // A "play" triangle
            mapGraphics.g2.fillPolygon(new int[]{width3, width3 * 2, width3},
                    new int[]{height3, height2, height3 * 2}, 3);
        }
    }

    public static class MapGraphicsImpl implements MapGraphics {

        protected final ConfigAPI config;
        protected final Area.Rectangle mapBounds;

        protected final ConfigSetting<ColorScheme> cs;
        protected final ConfigSetting<ColorScheme.Fonts> fonts;
        protected final ConfigSetting<Set<DisplayFlag>> displayFlags;
        protected final ConfigSetting<Double> mapZoom;

        protected final Rectangle2D rect = new Rectangle2D.Double();
        protected final Ellipse2D ellipse = new Ellipse2D.Double();
        protected final Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        protected final Line2D line = new Line2D.Double();

        protected Graphics2D g2;
        protected int width, widthMid, height, heightMid, offsetX, offsetY;
        protected double scaleX, scaleY, invertedScaleX, invertedScaleY;

        protected boolean accuracyEnabled;

        public MapGraphicsImpl(StarSystemAPI star, ConfigAPI config) {
            this.config = config;
            this.mapBounds = star.getCurrentMapBounds();

            this.cs = config.requireConfig("bot_settings.map_display.cs");
            this.fonts = config.requireConfig("bot_settings.map_display.cs.fonts");
            this.displayFlags = config.requireConfig("bot_settings.map_display.toggle");
            this.mapZoom = config.requireConfig("bot_settings.map_display.map_zoom");
        }

        public void setup(Graphics graphics, int width, int height) {
            this.width = width;
            this.height = height;
            this.widthMid = width / 2;
            this.heightMid = height / 2;

            this.accuracyEnabled = false;

            this.g2 = (Graphics2D) graphics;
            this.g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));

            double mapZoom = getMapZoom();
            if (mapZoom < 1) {
                int w = (int) (width * mapZoom);
                int h = (int) (height * mapZoom);

                offsetX = (width - w) / 2;
                offsetY = (height - h) / 2;

                scale(w, h);

                setColor("radiation");
                getGraphics2D().fillRect(0, 0, width, height);
                setColor("background");
                getGraphics2D().fillRect(offsetX, offsetY, w, h);
                setColor("unknown");
                getGraphics2D().drawRect(offsetX, offsetY, w, h);
            } else {
                offsetX = offsetY = 0;
                scale(width, height);

                setColor("background");
                getGraphics2D().fillRect(0, 0, width, height);
            }

            setSubPixelAccuracy(true);
            getGraphics2D().addRenderingHints(RENDERING_HINTS);
            getGraphics2D().setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        }

        public void dispose() {
            g2 = null;
        }

        // 0-1 -> 0-100%
        protected double getMapZoom() {
            return mapZoom.getValue();
        }

        protected Locatable toGameLocation(MouseEvent e) {
            return toGameLocation(e.getX(), e.getY());
        }

        private void scale(int width, int height) {
            scaleX = mapBounds.getWidth() / width;
            scaleY = mapBounds.getHeight() / height;
            invertedScaleX = 1.0 / scaleX;
            invertedScaleY = 1.0 / scaleY;
        }

        private void drawShape(Shape shape, boolean fill) {
            if (fill) g2.fill(shape);
            else g2.draw(shape);
        }

        private void setSubPixelAccuracy(boolean enable) {
            if (accuracyEnabled == enable) return;
            accuracyEnabled = enable;

            // in jdk17, java internally checks rendering hint instance but not sure about other versions
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    enable ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_DEFAULT);
        }

        @Override
        public Graphics2D getGraphics2D() {
            return g2;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getWidthMiddle() {
            return widthMid;
        }

        @Override
        public int getHeightMiddle() {
            return heightMid;
        }

        @Override
        public boolean hasDisplayFlag(eu.darkbot.api.config.types.DisplayFlag displayFlag) {
            DisplayFlag legacyFlag = DisplayFlag.values()[displayFlag.ordinal()];
            return displayFlags.getValue().contains(legacyFlag);
        }

        @Override
        public Color getColor(String color) {
            return config.getConfigValue(cs, color);
        }

        @Override
        public Font getFont(String font) {
            return config.getConfigValue(fonts, font);
        }

        @Override
        public double getScaleX() {
            return scaleX;
        }

        @Override
        public double getScaleY() {
            return scaleY;
        }

        @Override
        public double toScreenPointX(double gameX) {
            return offsetX + gameX * invertedScaleX;
        }

        @Override
        public double toScreenPointY(double gameY) {
            return offsetY + gameY * invertedScaleY;
        }

        @Override
        public double toScreenSizeW(double gameW) {
            return gameW * invertedScaleX;
        }

        @Override
        public double toScreenSizeH(double gameH) {
            return gameH * invertedScaleY;
        }

        @Override
        public double toGameLocationX(double screenX) {
            return (screenX - offsetX) * scaleX;
        }

        @Override
        public double toGameLocationY(double screenY) {
            return (screenY - offsetY) * scaleY;
        }

        @Override
        public void drawRect(double x, double y, double width, double height, boolean fill) {
            setSubPixelAccuracy(fill || (width > 10 && height > 10));
            rect.setRect(x, y, width, height);
            drawShape(rect, fill);
        }

        @Override
        public void drawOval(double x, double y, double width, double height, boolean fill) {
            setSubPixelAccuracy(true);
            ellipse.setFrame(x, y, width, height);
            drawShape(ellipse, fill);
        }

        @Override
        public void drawPoly(PolyType type, @NotNull Point... points) {
            setSubPixelAccuracy(true);
            if (points.length < 2) return;

            path.reset();
            boolean first = true;
            for (Point point : points) {
                if (first) {
                    path.moveTo(point.getX(), point.getY());
                    first = false;
                } else path.lineTo(point.getX(), point.getY());
            }
            if (type != PolyType.DRAW_POLYLINE)
                path.closePath();

            drawShape(path, type == PolyType.FILL_POLYGON);
        }

        @Override
        public void drawLine(double x1, double y1, double x2, double y2) {
            setSubPixelAccuracy(true);
            line.setLine(x1, y1, x2, y2);
            g2.draw(line);
        }
    }
}
