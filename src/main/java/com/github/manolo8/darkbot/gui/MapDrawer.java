package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.extensions.features.handlers.DrawableHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.http.Http;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MapDrawer extends JPanel implements Listener {

    private static final RenderingHints RENDERING_HINTS =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    public MapGraphicsImpl mapGraphics;

    public boolean hovering;

    protected Main main;
    protected DrawableHandler drawableHandler;

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
        setBorder(UIUtils.getBorder());
        setup(main);

        this.drawableHandler = main.pluginAPI.requireInstance(DrawableHandler.class);
        main.pluginAPI.requireAPI(EventBrokerAPI.class).registerListener(this);

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
        if (main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.ENABLED) {
            Image img = null;

            if (main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.USE_GAME_BACKGROUND)
                img = backgroundImage;

            if (img == null)
                img = main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.IMAGE.getImage();

            if (img != null) {
                mapGraphics.g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.OPACITY));
                mapGraphics.g2.drawImage(img, 0, 0, mapGraphics.width, mapGraphics.height, this);
                mapGraphics.g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        }

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

    @EventHandler
    public void onMapChange(StarSystemAPI.MapChangeEvent e) {
        if (e.getNext().getId() <= 0) {
            backgroundImage = null;
            return;
        }

        if (main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.ENABLED
                && main.config.BOT_SETTINGS.CUSTOM_BACKGROUND.USE_GAME_BACKGROUND) {
            new BackgroundImgDownloaderTask(this, e.getNext().getId())
                    .execute();
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

    private static class BackgroundImgDownloaderTask extends SwingWorker<Image, Void> {
        private static final Pattern MINIMAP_ID_PATTERN = Pattern.compile("id=\"(\\d+)\".*minimap=\"(\\d+)\"");
        private static Map<Integer, Integer> mapBackgroundIds;

        private final MapDrawer mapDrawer;
        private final int mapId;

        public BackgroundImgDownloaderTask(MapDrawer mapDrawer, int mapId) {
            this.mapDrawer = mapDrawer;
            this.mapId = mapId;
        }

        @Override
        protected void done() {
            try {
                mapDrawer.backgroundImage = get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected Image doInBackground() {
            if (parseMinimapIds()) {
                try {
                    URL backgroundURL = new URL("https://darkorbit-22.bpsecure.com/spacemap/graphics/minimaps/minimap-"
                            + mapBackgroundIds.getOrDefault(mapId, mapId) + "-700.jpg");

                    return ImageIO.read(backgroundURL);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        private boolean parseMinimapIds() {
            if (mapBackgroundIds != null && !mapBackgroundIds.isEmpty())
                return true;

            try {
                InputStream is = Http.create("https://darkorbit-22.bpsecure.com/spacemap/graphics/maps-config.xml")
                        .getInputStream();

                Matcher matcher = MINIMAP_ID_PATTERN.matcher("");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    mapBackgroundIds = br.lines()
                            .map(String::trim)
                            .filter(s -> s.startsWith("<map "))
                            .map(matcher::reset)
                            .filter(Matcher::find)
                            .collect(Collectors.toMap(m -> Integer.parseInt(m.group(1)), m -> Integer.parseInt(m.group(2))));

                } catch (IOException e) {
                    e.printStackTrace();
                    mapBackgroundIds = null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                mapBackgroundIds = null;
            }

            return mapBackgroundIds != null && !mapBackgroundIds.isEmpty();
        }
    }
}
