package com.github.manolo8.darkbot.gui.zones.safety;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.SafetyInfo;
import com.github.manolo8.darkbot.gui.MapDrawer;
import com.github.manolo8.darkbot.gui.drawables.ConstantEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.InfosDrawer;
import com.github.manolo8.darkbot.gui.drawables.ZonesDrawer;
import com.github.manolo8.darkbot.gui.zones.ZoneEditor;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Locatable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

class SafetiesDisplay extends MapDrawer {

    private final SafetiesEditor editor;

    private SafetyInfo closest;

    private InfosDrawer infosDrawer;
    private ConstantEntitiesDrawer constantEntitiesDrawer;
    private ZonesDrawer zonesDrawer;

    SafetiesDisplay(SafetiesEditor editor) {
        this.editor = editor;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                updateClosest(e, true);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateClosest(e, false);
            }
        });
    }

    @Override
    public void setup(Main main) {
        this.main = main;
        this.mapGraphics = main.pluginAPI.requireInstance(ZoneEditor.FixedScaleMapGraphicsImpl.class);

        this.infosDrawer = main.pluginAPI.requireInstance(InfosDrawer.class);
        this.zonesDrawer = main.pluginAPI.requireInstance(ZonesDrawer.class);
        this.constantEntitiesDrawer = main.pluginAPI.requireInstance(ConstantEntitiesDrawer.class);
    }

    @Override
    protected void onPaint() {
        zonesDrawer.drawZones(mapGraphics);
        constantEntitiesDrawer.onDraw(mapGraphics);
        infosDrawer.drawMap(mapGraphics);

        if (editor.safetyInfos == null) return;
        drawCustomZones(mapGraphics);
    }

    private void drawCustomZones(MapGraphics mg) {
        if (hovering && closest != null && closest != editor.editing) {
            mg.setColor("safety_editor.zone_highlight");
            zonesDrawer.drawSafeZone(mg, closest);

            mg.setColor("safety_editor.zone_solid");
            mg.drawOvalCentered(closest, mg.toScreenSizeW(closest.diameter()), mg.toScreenSizeH(closest.diameter()), false);
        }

        if (editor.editing != null) {
            mg.setColor("safety_editor.zone_selected");
            zonesDrawer.drawSafeZone(mg, editor.editing);
        }
    }

    private void updateClosest(MouseEvent e, boolean edit) {
        Locatable click = mapGraphics.toGameLocation(e.getX(), e.getY());
        closest = editor.safetyInfos.stream()
                .filter(s -> s.entity != null && !s.entity.removed)
                .min(Comparator.comparingDouble(s -> Math.pow(s.x - click.getX(), 2) + Math.pow(s.y - click.getY(), 2)))// squared distance
                .orElse(null);

        repaint();
        if (edit && closest != null) editor.edit(closest);
    }
}
