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

                } else main.hero.drive.move(mapGraphics.locFromClick(e));
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP && SwingUtilities.isLeftMouseButton(e)) return;

                main.hero.drive.move(mapGraphics.locFromClick(e));
            }
        });
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
        protected final ConfigSetting<Double> radiationScale;

        protected final Rectangle2D rect = new Rectangle2D.Double();
        protected final Ellipse2D ellipse = new Ellipse2D.Double();
        protected final Path2D path = new Path2D.Double();
        protected final Line2D line = new Line2D.Double();

        protected Graphics2D g2;
        protected int width, widthMid, height, heightMid;
        protected double scaleX, scaleY, gameX, gameY;

        protected int x, y;

        public MapGraphicsImpl(StarSystemAPI star, ConfigAPI config) {
            this.config = config;
            this.mapBounds = star.getCurrentMapBounds();

            this.cs = config.requireConfig("bot_settings.map_display.cs");
            this.fonts = config.requireConfig("bot_settings.map_display.cs.fonts");
            this.displayFlags = config.requireConfig("bot_settings.map_display.toggle");
            this.radiationScale = config.requireConfig("bot_settings.map_display.radiation_scale");
        }

        public void setup(Graphics g2, int width, int height) {
            this.width = width;
            this.height = height;
            this.widthMid = width / 2;
            this.heightMid = height / 2;

            this.scaleX = mapBounds.getWidth() / width;
            this.scaleY = mapBounds.getHeight() / height;

            this.g2 = (Graphics2D) g2;
            this.g2.addRenderingHints(RENDERING_HINTS);

            double scale = getRadScale();
            if (scale > 0) {
                x = (int) ((mapBounds.getWidth() / 2 * scale) / (mapBounds.getWidth() / width));
                y = (int) ((mapBounds.getHeight() / 2 * scale) / (mapBounds.getHeight() / height));

                int w = width - x * 2;
                int h = height - y * 2;

                scaleX = mapBounds.getWidth() / w;
                scaleY = mapBounds.getHeight() / h;

                gameX = x * getScaleX();
                gameY = y * getScaleY();

                setColor("radiation");
                getGraphics2D().fillRect(0, 0, width, height);
                setColor("background");
                getGraphics2D().fillRect(x, y, w, h);
                setColor("unknown");
                getGraphics2D().drawRect(x, y, w, h);
            } else {
                x = y = 0;
                gameX = gameY = 0;
                scaleX = mapBounds.getWidth() / width;
                scaleY = mapBounds.getHeight() / height;
                setColor("background");
                getGraphics2D().fillRect(0, 0, width, height);
            }
        }

        public void dispose() {
            g2 = null;
        }

        protected double getRadScale() {
            return radiationScale.getValue();
        }

        protected Locatable locFromClick(MouseEvent e) {
            return Locatable.of((e.getX() - x) * getScaleX(), (e.getY() - y) * getScaleY());
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
            return x + gameX / scaleX;
        }

        @Override
        public double toScreenPointY(double gameY) {
            return y + gameY / scaleY;
        }

        @Override
        public double toGameLocationX(double screenX) {
            return gameX + screenX * scaleX;
        }

        @Override
        public double toGameLocationY(double screenY) {
            return gameY + screenY * scaleY;
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

            switch (type) {
                case DRAW_POLYGON:
                case DRAW_POLYLINE:
                    g2.draw(path);
                    break;
                case FILL_POLYGON:
                    g2.fill(path);
            }
        }

        @Override
        public void drawLine(double x1, double y1, double x2, double y2) {
            line.setLine(x1, y1, x2, y2);
            g2.draw(line);
        }
    }
}
