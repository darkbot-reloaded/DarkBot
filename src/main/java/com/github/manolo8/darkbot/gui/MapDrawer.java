package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.FlashResManager;
import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.extensions.features.handlers.DrawableHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.StarSystemAPI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MapDrawer extends JPanel {

    private static final RenderingHints RENDERING_HINTS =
            new RenderingHints(new HashMap<RenderingHints.Key, Object>() {{
                put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            }});

    public MapGraphicsImpl mapGraphics;
    public boolean hovering;

    protected Main main;
    protected DrawableHandler drawableHandler;

    private FlashResManager flashResManager;

    private CompletableFuture<Image> minimapFuture;
    private GameMap lastMap;
    private Image backgroundImage;

    private RadiationScalingMapGraphics radMapGraphics;

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
        setBorder(UIUtils.getUnfocusableBorder());
        setup(main);

        this.drawableHandler = main.pluginAPI.requireInstance(DrawableHandler.class);
        this.flashResManager = main.pluginAPI.requireInstance(FlashResManager.class);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP && SwingUtilities.isLeftMouseButton(e)) {
                    main.setRunning(!main.isRunning());
                    repaint();

                } else main.hero.drive.move(radMapGraphics.locFromClick(e));
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP && SwingUtilities.isLeftMouseButton(e)) return;

                main.hero.drive.move(radMapGraphics.locFromClick(e));
            }
        });
    }

    public void setup(Main main) {
        this.main = main;
        this.mapGraphics = main.pluginAPI.requireInstance(MapGraphicsImpl.class);
        this.radMapGraphics = main.pluginAPI.requireInstance(RadiationScalingMapGraphics.class);
    }

    protected void onPaint() {
        radMapGraphics.onPaint();
        drawBackgroundImage();

        for (Drawable drawable : drawableHandler.getDrawables()) {
            drawable.onDraw(mapGraphics);
            drawable.onDrawRadiation(mapGraphics, radMapGraphics);
        }

        // just ensure that is drawn always last
        if (hovering && main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP)
            drawActionButton();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (main == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.addRenderingHints(RENDERING_HINTS);

        mapGraphics.setup(g2, getWidth(), getHeight());
        radMapGraphics.setup(g2, getWidth(), getHeight());

        synchronized (Main.UPDATE_LOCKER) {
            onPaint();
        }

        mapGraphics.dispose();
        radMapGraphics.dispose();
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

        protected final Rectangle2D rect = new Rectangle2D.Double();
        protected final Ellipse2D ellipse = new Ellipse2D.Double();
        protected final Path2D path = new Path2D.Double();
        protected final Line2D line = new Line2D.Double();

        protected Graphics2D g2;
        protected int width, widthMid, height, heightMid, startX, startY;
        protected double scaleX, scaleY;

        public MapGraphicsImpl(StarSystemAPI star, ConfigAPI config) {
            this.config = config;
            this.mapBounds = star.getCurrentMapBounds();

            this.cs = config.requireConfig("bot_settings.map_display.cs");
            this.fonts = config.requireConfig("bot_settings.map_display.cs.fonts");
            this.displayFlags = config.requireConfig("bot_settings.map_display.toggle");
        }

        public void setup(Graphics2D g2, int width, int height) {
            this.width = width;
            this.height = height;
            this.widthMid = width / 2;
            this.heightMid = height / 2;

            this.scaleX = mapBounds.getWidth() / width;
            this.scaleY = mapBounds.getHeight() / height;

            this.g2 = g2; //graphics.create();
        }

        public void dispose() {
            g2 = null;
        }

        public void onPaint() {
            setColor("background");
            getGraphics2D().fillRect(0, 0, getWidth(), getHeight());
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
            return (gameX / scaleX);
        }

        @Override
        public double toScreenPointY(double gameY) {
            return (gameY / scaleY);
        }

        @Override
        public double toGameLocationX(double screenX) {
            return screenX * scaleX;
        }

        @Override
        public double toGameLocationY(double screenY) {
            return screenY * scaleY;
        }

        @Override
        public void drawRect(double x, double y, double width, double height, boolean fill) {
            rect.setRect(x, y, width, height);
            if (fill) g2.fill(rect);
            else g2.draw(rect);
        }

        @Override
        public void drawOval(double x, double y, double width, double height, boolean fill) {
            ellipse.setFrame(x, y, width, height);
            if (fill) g2.fill(ellipse);
            else g2.draw(ellipse);
        }

        @Override
        public void drawPoly(PolyType type, @NotNull Point... points) {
            if (points.length == 0) return;

            path.reset();
            boolean first = true;
            for (Point point : points) {
                if (first) {
                    path.moveTo(point.getX(), point.getY());
                    first = false;
                } else path.lineTo(point.getX(), point.getY());
            }
            path.closePath();

            if (type == PolyType.DRAW_POLYGON || type == PolyType.DRAW_POLYLINE) g2.draw(path);
            else if (type == PolyType.FILL_POLYGON) g2.fill(path);
        }

        @Override
        public void drawLine(double x1, double y1, double x2, double y2) {
            line.setLine(x1, y1, x2, y2);
            g2.draw(line);
        }
    }

    public static class RadiationScalingMapGraphics extends MapGraphicsImpl {
        private final ConfigSetting<Double> radiationScale;
        private double gameStartX, gameStartY;

        private int componentWidth, componentHeight;

        public RadiationScalingMapGraphics(StarSystemAPI star, ConfigAPI config) {
            super(star, config);
            this.radiationScale = config.requireConfig("bot_settings.map_display.radiation_scale");
        }

        @Override
        public void setup(Graphics2D g2, int width, int height) {
            componentWidth = width;
            componentHeight = height;

            startX = (int) ((mapBounds.getWidth() / 2 * radiationScale.getValue()) / (mapBounds.getWidth() / width));
            startY = (int) ((mapBounds.getHeight() / 2 * radiationScale.getValue()) / (mapBounds.getHeight() / height));

            super.setup(g2, width - startX * 2, height - startY * 2);

            gameStartX = startX * getScaleX();
            gameStartY = startY * getScaleY();
        }

        @Override
        public void onPaint() {
            setColor("radiation");
            getGraphics2D().fillRect(0, 0, componentWidth, componentHeight);
            setColor("background");
            getGraphics2D().fillRect(startX, startY, getWidth(), getHeight());
            setColor("unknown");
            getGraphics2D().drawRect(startX, startY, getWidth(), getHeight());
        }

        @Override
        public double toScreenPointX(double gameX) {
            return startX + super.toScreenPointX(gameX);
        }

        @Override
        public double toScreenPointY(double gameY) {
            return startY + super.toScreenPointY(gameY);
        }

        @Override
        public double toGameLocationX(double screenX) {
            return gameStartX + super.toGameLocationX(screenX);
        }

        @Override
        public double toGameLocationY(double screenY) {
            return gameStartY + super.toGameLocationY(screenY);
        }

        protected Locatable locFromClick(MouseEvent e) {
            return Locatable.of((e.getX() - startX) * getScaleX(),(e.getY() - startY) * getScaleY());
        }
    }
}
