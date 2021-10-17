package com.github.manolo8.darkbot.gui.zones.safety;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.SafetyInfo;
import com.github.manolo8.darkbot.gui.MapDrawer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

class SafetiesDisplay extends MapDrawer {

    private SafetiesEditor editor;
    private SafetyInfo closest;

    SafetiesDisplay(SafetiesEditor editor) {
        this.editor = editor;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                updateClosest(e);
                if (closest != null) editor.edit(closest);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateClosest(e);
                repaint();
            }
        });
    }

    private void updateClosest(MouseEvent e) {
        double x = undoTranslateX(e.getX()), y = undoTranslateY(e.getY());
        closest = editor.safetyInfos.stream()
                .filter(s -> s.entity != null && !s.entity.removed)
                .min(Comparator.comparingDouble(s -> Math.pow(s.x - x, 2) + Math.pow(s.y - y, 2)))// squared distance
                .orElse(null);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = setupDraw(g);
        synchronized (Main.UPDATE_LOCKER) {
            drawZones(g2);
            drawStaticEntities(g2);
            drawMap(g2);
            if (editor.safetyInfos == null) return;
            drawCustomZones(g2);
        }
    }

    @Override
    protected void drawMap(Graphics2D g2) {
        g2.setColor(cs.TEXT_DARK);
        g2.setFont(cs.FONTS.BIG);
        drawString(g2, hero.map.name, mid, (height / 2) + 12, Align.MID);
    }

    @Override
    protected void drawCustomZones(Graphics2D g2) {
        if (hovering && closest != null && closest != editor.editing) {
            g2.setColor(cs.SAFETY_EDITOR.ZONE_HIGHLIGHT);
            drawSafeZone(g2, closest);
            g2.setColor(cs.SAFETY_EDITOR.ZONE_SOLID);
            int radius = closest.radius();
            g2.drawOval(translateX(closest.x - radius), translateY(closest.y - radius),
                        translateX(closest.diameter()), translateY(closest.diameter()));
        }
        if (editor.editing != null) {
            g2.setColor(cs.SAFETY_EDITOR.ZONE_SELECTED);
            drawSafeZone(g2, editor.editing);
        }
    }

}
