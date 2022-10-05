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
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MapDrawer extends JPanel {

    private static final RenderingHints RENDERING_HINTS =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

                } else main.hero.drive.move(mapGraphics.toGameLocation(e.getX(), e.getY()));
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP && SwingUtilities.isLeftMouseButton(e)) return;

                main.hero.drive.move(mapGraphics.toGameLocation(e.getX(), e.getY()));
            }
        });
    }

    public void setup(Main main) {
        this.main = main;
        this.mapGraphics = main.pluginAPI.requireInstance(MapGraphicsImpl.class);
    }

    protected void onPaint() {
        //background image is drawn first
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

        private final ConfigAPI config;
        private final Area.Rectangle mapBounds;

        private final ConfigSetting<ColorScheme> cs;
        private final ConfigSetting<ColorScheme.Fonts> fonts;
        private final ConfigSetting<Set<DisplayFlag>> displayFlags;

        private Graphics2D g2;
        private int width, widthMid, height, heightMid;

        public MapGraphicsImpl(StarSystemAPI star, ConfigAPI config) {
            this.config = config;
            this.mapBounds = star.getCurrentMapBounds();

            this.cs = config.requireConfig("bot_settings.map_display.cs");
            this.fonts = config.requireConfig("bot_settings.map_display.cs.fonts");
            this.displayFlags = config.requireConfig("bot_settings.map_display.toggle");
        }

        public void setup(Graphics graphics, int width, int height) {
            this.width = width;
            this.height = height;
            this.widthMid = width / 2;
            this.heightMid = height / 2;

            this.g2 = (Graphics2D) graphics; //graphics.create();

            this.setColor("background");
            this.g2.fillRect(0, 0, getWidth(), getHeight());
            this.g2.setRenderingHints(RENDERING_HINTS);
        }

        public void dispose() {
            //if (g2 != null) g2.dispose();
            g2 = null;
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
        public int toScreenPointX(double gameX) {
            return (int) Math.round((gameX / mapBounds.getWidth()) * getWidth());
        }

        @Override
        public int toScreenPointY(double gameY) {
            return (int) Math.round((gameY / mapBounds.getHeight()) * getHeight());
        }

        @Override
        public double toGameLocationX(int screenX) {
            return (screenX / (double) getWidth()) * mapBounds.getWidth();
        }

        @Override
        public double toGameLocationY(int screenY) {
            return (screenY / (double) getHeight()) * mapBounds.getHeight();
        }
    }
}
