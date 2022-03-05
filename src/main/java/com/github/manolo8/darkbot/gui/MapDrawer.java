package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.extensions.features.handlers.DrawableHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Function;

public class MapDrawer extends JPanel {

    private static final RenderingHints RENDERING_HINTS =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) {{
                put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
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

                } else main.hero.drive.move(mapGraphics.undoTranslate(e.getX(), e.getY()));
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP || !SwingUtilities.isLeftMouseButton(e)) {
                    main.hero.drive.move(mapGraphics.undoTranslate(e.getX(), e.getY()));
                }
            }
        });
    }

    public void setup(Main main) {
        this.main = main;
        this.mapGraphics = main.pluginAPI.requireInstance(MapGraphicsImpl.class);
    }

    protected void onPaint() {
        drawableHandler.draw(mapGraphics);

        // just ensure that is drawn always last
        if (hovering && main.config.BOT_SETTINGS.MAP_DISPLAY.MAP_START_STOP) mapGraphics.drawActionButton();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (main == null) return;

        mapGraphics.setup(g, getWidth(), getHeight());
        onPaint();
        mapGraphics.dispose();
    }

    public static class MapGraphicsImpl implements MapGraphics {

        private final BotAPI bot;
        private final ConfigAPI config;
        private final Area.Rectangle mapBounds;

        private final ConfigSetting.Parent<ColorScheme> cs;
        private final ConfigSetting.Parent<ColorScheme.Fonts> fonts;

        private Graphics2D g2;
        private int width, mid, height;

        public MapGraphicsImpl(BotAPI bot, StarSystemAPI star, ConfigAPI config) {
            this.bot = bot;
            this.config = config;
            this.mapBounds = star.getCurrentMapBounds();

            ConfigSetting<ColorScheme> cs = config.requireConfig("bot_settings.map_display.cs");
            this.cs = (ConfigSetting.Parent<ColorScheme>) cs;

            ConfigSetting<ColorScheme.Fonts> fonts = config.requireConfig("bot_settings.map_display.cs.fonts");
            this.fonts = (ConfigSetting.Parent<ColorScheme.Fonts>) fonts;
        }

        public void setup(Graphics graphics, int width, int height) {
            this.width = width;
            this.height = height;
            this.mid = width / 2;

            this.g2 = (Graphics2D) graphics.create();

            this.setColor("background");
            this.g2.fillRect(0, 0, getWidth(), getHeight());
            this.g2.setRenderingHints(RENDERING_HINTS);
        }

        public void dispose() {
            if (g2 != null) g2.dispose();
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
        public int getMiddle() {
            return mid;
        }

        @Override
        public Graphics2D getGraphics2D() {
            return g2;
        }

        @Override
        public Point translate(double x, double y) {
            return Point.of(translateX(x), translateY(y));
        }

        @Override
        public Locatable undoTranslate(double x, double y) {
            return Locatable.of(undoTranslateX(x), undoTranslateY(y));
        }

        @Override
        public void setColor(String color, Function<Color, Color> modifiers) {
            Color val = config.getConfigValue(cs, color);
            setColor(modifiers != null ? modifiers.apply(val) : val);
        }

        @Override
        public void setFont(String font, Function<Font, Font> modifiers) {
            Font val = config.getConfigValue(fonts, font);
            setFont(modifiers != null ? modifiers.apply(val) : val);
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

        private int translateX(double x) {
            return (int) Math.round(((x / mapBounds.getWidth()) * getWidth()));
        }

        private int translateY(double y) {
            return (int) Math.round(((y / mapBounds.getHeight()) * getHeight()));
        }

        private double undoTranslateX(double x) {
            return ((x / (double) getWidth()) * mapBounds.getWidth());
        }

        private double undoTranslateY(double y) {
            return ((y / (double) getHeight()) * mapBounds.getHeight());
        }
    }
}
