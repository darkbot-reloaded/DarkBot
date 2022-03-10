package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.extensions.features.handlers.DrawableHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MapDrawer extends JPanel {

    private static final RenderingHints RENDERING_HINTS =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) {{
                put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            }};

    public MapGraphicsImpl mapGraphics;

    public boolean hovering;

    protected Main main;
    protected DrawableHandler drawableHandler;

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

        this.drawableHandler = main.pluginAPI.requireInstance(DrawableHandler.class);

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
        synchronized (Main.UPDATE_LOCKER) {
            for (Drawable drawable : drawableHandler.getDrawables()) {
                drawable.onDraw(mapGraphics);
            }
        }

        // just ensure that is drawn always last
        if (hovering && main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP) mapGraphics.drawActionButton();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (main == null) return;

        mapGraphics.setup(g, getWidth(), getHeight());
        onPaint();
        mapGraphics.dispose();
    }

    public static class MapGraphicsImpl implements MapGraphics {

        private final BotAPI bot;
        private final ConfigAPI config;
        private final Area.Rectangle mapBounds;

        private final ConfigSetting<ColorScheme> cs;
        private final ConfigSetting<ColorScheme.Fonts> fonts;

        private Graphics2D g2;
        private int width, widthMid, height, heightMid;

        public MapGraphicsImpl(BotAPI bot, StarSystemAPI star, ConfigAPI config) {
            this.bot = bot;
            this.config = config;
            this.mapBounds = star.getCurrentMapBounds();

            this.cs = config.requireConfig("bot_settings.map_display.cs");
            this.fonts = config.requireConfig("bot_settings.map_display.cs.fonts");
        }

        public void setup(Graphics graphics, int width, int height) {
            this.width = width;
            this.height = height;
            this.widthMid = width / 2;
            this.heightMid = height / 2;

            this.g2 = (Graphics2D) graphics.create();

            this.setColor("background");
            this.g2.fillRect(0, 0, getWidth(), getHeight());
            this.g2.setRenderingHints(RENDERING_HINTS);
        }

        public void dispose() {
            if (g2 != null) g2.dispose();
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
            return (int) Math.round(((gameY / mapBounds.getHeight()) * getHeight()));
        }

        @Override
        public double toGameLocationX(int screenX) {
            return ((screenX / (double) getWidth()) * mapBounds.getWidth());
        }

        @Override
        public double toGameLocationY(int screenY) {
            return ((screenY / (double) getHeight()) * mapBounds.getHeight());
        }

        private void drawActionButton() {
            setColor("darken_back");
            g2.fillRect(0, 0, this.getWidth(), this.getHeight());

            setColor("action_button");
            int height2 = this.getHeight() / 2, height3 = this.getHeight() / 3,
                    width3 = this.getWidth() / 3, width9 = this.getWidth() / 9;

            if (bot.isRunning()) {
                g2.fillRect(width3, height3, width9, height3); // Two vertical parallel lines
                g2.fillRect((width3 * 2) - width9, height3, width9, height3);
            } else { // A "play" triangle
                g2.fillPolygon(new int[]{width3, width3 * 2, width3},
                        new int[]{height3, height2, height3 * 2}, 3);
            }
        }
    }
}
