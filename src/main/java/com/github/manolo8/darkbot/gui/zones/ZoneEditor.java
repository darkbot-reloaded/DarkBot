package com.github.manolo8.darkbot.gui.zones;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.gui.MapDrawer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ZoneEditor extends MapDrawer {

    private ZoneInfo zoneInfo = null;
    private boolean selecting;
    private Rect area = new Rect();

    private int startX, startY;

    private Consumer<Map> mapChange = null;

    private class Rect {
        int x1, x2, y1, y2;

        public void set(int x1, int y1, int x2, int y2) {
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
        }

        public void update(double divisions) {
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
            g2.drawRect(x1, y1, x2 - x1 - (x2 == width ? 1 : 0), y2 - y1 - (y2 == height ? 1 : 0));
        }
    }

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
        super.setup(main);
        if (mapChange != null) main.mapManager.mapChange.remove(mapChange);
        main.mapManager.mapChange.add(mapChange = map -> {
            zoneInfo = zonesByMap.computeIfAbsent(map.id, id -> new ZoneInfo(config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION));
            ZoneEditor.this.repaint();
        });
        zoneInfo = zonesByMap.get(main.hero.map.id);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = setupDraw(g);

        synchronized (Main.UPDATE_LOCKER) {
            drawZones(g2);
            drawStaticEntities(g2);
            drawMap(g2);
        }

        if (zoneInfo == null) return;

        int res = config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION;
        if (zoneInfo.resolution != res) zoneInfo.setResolution(res);
        drawCustomZones(g2);
        drawGrid(g2, res);

        if (!selecting && !hovering) return;

        g2.setColor(selecting ? cs.ZONE_EDITOR.SELECTING : cs.ZONE_EDITOR.HOVERING);
        area.update(res);
        area.draw(g2);
    }

    @Override
    protected void drawMap(Graphics2D g2) {
        g2.setColor(cs.TEXT_DARK);
        g2.setFont(cs.FONTS.BIG);
        drawString(g2, hero.map.name, mid, (height / 2) + 12, Align.MID);
    }

    @Override
    protected void drawCustomZones(Graphics2D g2) {
        g2.setColor(cs.ZONE_EDITOR.ZONE);
        drawCustomZone(g2, zoneInfo);
    }

    private void drawGrid(Graphics2D g2, int resolution) {
        g2.setColor(cs.ZONE_EDITOR.LINES);
        for (int i = 1; i < resolution; i++) {
            int x = i * width / resolution;
            g2.drawLine(x, 0, x, height);
        }
        for (int i = 1; i < resolution; i++) {
            int y = i * height / resolution;
            g2.drawLine(0, y, width, y);
        }
    }

    private void toggleSelection(boolean toggle, boolean def) {
        if (zoneInfo == null) return;
        area.update(zoneInfo.resolution);

        int startX = mapToGridX(area.x1), startY = mapToGridY(area.y1),
                endX = mapToGridX(area.x2), endY = mapToGridY(area.y2);

        if (toggle) {
            zoneInfo.toggle(startX, startY, endX, endY);
            config.changed = true;
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
        config.changed = true;
    }


}
