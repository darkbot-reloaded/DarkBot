package com.github.manolo8.darkbot.gui.zones;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.gui.MapDrawer;
import com.github.manolo8.darkbot.gui.zones.safety.SafetiesDisplay;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ZoneEditor extends MapDrawer {

    private final Rect area = new Rect();

    private ZoneEditorDrawer zonesDrawer;

    private boolean selecting;
    private int startX, startY;

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
                zonesDrawer.toggleSelection(SwingUtilities.isMiddleMouseButton(e), SwingUtilities.isLeftMouseButton(e));
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
        super.setup(main);
        this.zonesDrawer = new ZoneEditorDrawer(main.pluginAPI, this, zonesByMap);
    }

    @Override
    protected void onPaint() {
        zonesDrawer.onDraw(mapGraphics);
    }

    public static class ZoneEditorDrawer extends SafetiesDisplay.SafetiesDisplayDrawer<ZoneEditor> implements Listener {

        private final java.util.Map<Integer, ZoneInfo> zonesByMap;
        private ZoneInfo zoneInfo;

        public ZoneEditorDrawer(PluginAPI api, ZoneEditor zoneEditor, java.util.Map<Integer, ZoneInfo> zonesByMap) {
            super(api, zoneEditor);

            this.zonesByMap = zonesByMap;
            this.zoneInfo = zonesByMap.get(hero.getMap().getId());

            api.requireAPI(EventBrokerAPI.class).registerListener(this);
        }

        @Override
        public void onDraw(MapGraphics mg) {
            synchronized (Main.UPDATE_LOCKER) {
                drawZones(mg);
                staticEntitiesDrawer.onDraw(mg);
                drawMap(mg);
            }

            if (zoneInfo == null) return;

            int res = zoneResolution.getValue();
            if (zoneInfo.resolution != res) zoneInfo.setResolution(res);

            drawCustomZones(mg);
            drawGrid(mg, res);

            if (!drawer.selecting && !drawer.hovering) return;

            mg.setColor(drawer.selecting ? "zone_editor.selecting" : "zone_editor.hovering");
            drawer.area.update(drawer.getWidth(), drawer.getHeight(), res);
            drawer.area.draw(mg);

        }

        @Override
        protected void drawCustomZones(MapGraphics mg) {
            mg.setColor("zone_editor.zone");
            drawCustomZone(mg, zoneInfo);
        }

        protected void drawMap(MapGraphics mg) {
            mg.setColor("text_dark");
            mg.setFont("big");

            mg.drawString(hero.getMap().getName(), mg.getMiddle(), mg.getHeight() / 2 + 12, MapGraphics.Align.MID);
        }

        private void drawGrid(MapGraphics mg, int resolution) {
            mg.setColor("zone_editor.lines");

            for (int i = 1; i < resolution; i++) {
                int x = i * drawer.getWidth() / resolution;
                mg.getGraphics2D().drawLine(x, 0, x, drawer.getHeight());
            }
            for (int i = 1; i < resolution; i++) {
                int y = i * drawer.getHeight() / resolution;
                mg.getGraphics2D().drawLine(0, y, drawer.getWidth(), y);
            }
        }

        private void toggleSelection(boolean toggle, boolean def) {
            if (zoneInfo == null) return;
            drawer.area.update(drawer.getWidth(), drawer.getHeight(), zoneInfo.resolution);

            int startX = mapToGridX(drawer.area.x1), startY = mapToGridY(drawer.area.y1),
                    endX = mapToGridX(drawer.area.x2), endY = mapToGridY(drawer.area.y2);

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
        public void onMapChange(StarSystemAPI.MapChangeEvent m) {
            zoneInfo = zonesByMap.computeIfAbsent(m.getNext().getId(), id -> new ZoneInfo(zoneResolution.getValue()));
            drawer.repaint();
        }

        protected int mapToGridX(int x) {
            return (x + 1) * zoneResolution.getValue() / drawer.mapGraphics.getWidth();
        }

        protected int mapToGridY(int y) {
            return (y + 1) * zoneResolution.getValue() / drawer.mapGraphics.getHeight();
        }
    }

    private static class Rect {
        private int x1, x2, y1, y2;

        public void set(int x1, int y1, int x2, int y2) {
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
        }

        public void update(int width, int height, double divisions) {
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

        private void draw(MapGraphics mg) {
            mg.getGraphics2D().drawRect(x1, y1, x2 - x1 - (x2 == mg.getWidth() ? 1 : 0),
                    y2 - y1 - (y2 == mg.getHeight() ? 1 : 0));
        }
    }
}
