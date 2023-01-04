package com.github.manolo8.darkbot.gui.zones;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.gui.MapDrawer;
import com.github.manolo8.darkbot.gui.drawables.ConstantEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.InfosDrawer;
import com.github.manolo8.darkbot.gui.drawables.ZonesDrawer;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class ZoneEditor extends MapDrawer implements Listener {

    private final Rect area = new Rect();
    private ZoneInfo zoneInfo = null;
    private boolean selecting;
    private int startX, startY;

    private Map<Integer, ZoneInfo> zonesByMap;
    private InfosDrawer infosDrawer;
    private ConstantEntitiesDrawer constantEntitiesDrawer;
    private ZonesDrawer zonesDrawer;

    ZoneEditor() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                area.set(startX = e.getX(), startY = e.getY(), e.getX(), e.getY());
                selecting = true;
                ZoneEditor.super.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selecting = false;
                area.set(startX, startY, e.getX(), e.getY());
                toggleSelection(SwingUtilities.isMiddleMouseButton(e), SwingUtilities.isLeftMouseButton(e));
                area.set(e.getX(), e.getY(), e.getX(), e.getY());
                ZoneEditor.super.repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                area.set(startX, startY, e.getX(), e.getY());
                ZoneEditor.super.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                area.set(e.getX(), e.getY(), e.getX(), e.getY());
                ZoneEditor.super.repaint();
            }
        });
    }

    void setup(Main main, java.util.Map<Integer, ZoneInfo> zonesByMap) {
        this.main = main;
        this.mapGraphics = main.pluginAPI.requireInstance(FixedScaleMapGraphicsImpl.class);

        main.pluginAPI.requireAPI(EventBrokerAPI.class).registerListener(this);

        this.infosDrawer = main.pluginAPI.requireInstance(InfosDrawer.class);
        this.zonesDrawer = main.pluginAPI.requireInstance(ZonesDrawer.class);
        this.constantEntitiesDrawer = main.pluginAPI.requireInstance(ConstantEntitiesDrawer.class);

        this.zonesByMap = zonesByMap;
        this.zoneInfo = zonesByMap.get(main.hero.map.id);
    }

    @Override
    protected void onPaint() {
        zonesDrawer.drawZones(mapGraphics);
        constantEntitiesDrawer.onDraw(mapGraphics);
        infosDrawer.drawMap(mapGraphics);

        if (zoneInfo == null) return;
        int res = main.config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION;
        if (zoneInfo.resolution != res) zoneInfo.setResolution(res);

        mapGraphics.setColor("zone_editor.zone");
        zonesDrawer.drawCustomZone(mapGraphics, zoneInfo);
        drawGrid(mapGraphics, res);

        if (!selecting && !hovering) return;

        mapGraphics.setColor(selecting ? "zone_editor.selecting" : "zone_editor.hovering");
        area.update(res);
        area.draw(mapGraphics.getGraphics2D());
    }

    private void drawGrid(MapGraphics mg, int resolution) {
        mg.setColor("zone_editor.lines");

        for (int i = 1; i < resolution; i++) {
            int x = i * mg.getWidth() / resolution;
            mg.getGraphics2D().drawLine(x, 0, x, mg.getHeight());
        }
        for (int i = 1; i < resolution; i++) {
            int y = i * mg.getHeight() / resolution;
            mg.getGraphics2D().drawLine(0, y, mg.getWidth(), y);
        }
    }

    private void toggleSelection(boolean toggle, boolean def) {
        if (zoneInfo == null) return;
        area.update(zoneInfo.resolution);

        int startX = mapToGridX(area.x1), startY = mapToGridY(area.y1),
                endX = mapToGridX(area.x2), endY = mapToGridY(area.y2);

        if (toggle) {
            zoneInfo.toggle(startX, startY, endX, endY);
            ConfigEntity.changed();
            return;
        }

        boolean differ = false;
        boolean sel = zoneInfo.get(startX, startY);
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                if (zoneInfo.get(x, y) != sel && (differ = true)) break;
            }
            if (differ) break;
        }

        zoneInfo.set(startX, startY, endX, endY, !differ ? !sel : def);
        ConfigEntity.changed();
    }

    @EventHandler
    public void onMapChange(StarSystemAPI.MapChangeEvent event) {
        zoneInfo = zonesByMap.computeIfAbsent(event.getNext().getId(),
                id -> new ZoneInfo(main.config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION));
        repaint();
    }

    protected int mapToGridX(int x) {
        return (x + 1) * main.config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION / mapGraphics.getWidth();
    }

    protected int mapToGridY(int y) {
        return (y + 1) * main.config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION / mapGraphics.getHeight();
    }

    private class Rect {
        int x1, x2, y1, y2;

        public void set(int x1, int y1, int x2, int y2) {
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
        }

        public void update(double divisions) {
            int width = getWidth();
            int height = getHeight();

            double widthDivision = width / divisions, heightDivision = height / divisions;
            x1 -= x1 % widthDivision;
            y1 -= y1 % heightDivision;
            x2 += widthDivision - (x2 % widthDivision);
            y2 += heightDivision - (y2 % heightDivision);

            if (x1 < 0) x1 = 0;
            if (y1 < 0) y1 = 0;
            if (x2 > width) x2 = width;
            if (y2 > height) y2 = height;
        }

        private void draw(Graphics2D g2) {
            g2.drawRect(x1, y1, x2 - x1 - (x2 == getWidth() ? 1 : 0), y2 - y1 - (y2 == getHeight() ? 1 : 0));
        }
    }

    public static class FixedScaleMapGraphicsImpl extends MapDrawer.MapGraphicsImpl {

        public FixedScaleMapGraphicsImpl(StarSystemAPI star, ConfigAPI config) {
            super(star, config);
        }

        @Override
        protected double getMapZoom() {
            return 1;
        }
    }
}
