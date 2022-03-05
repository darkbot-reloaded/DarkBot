package com.github.manolo8.darkbot.gui.zones.safety;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.SafetyInfo;
import com.github.manolo8.darkbot.gui.MapDrawer;
import com.github.manolo8.darkbot.gui.drawables.StaticEntitiesDrawer;
import com.github.manolo8.darkbot.gui.drawables.ZonesDrawer;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

public class SafetiesDisplay extends MapDrawer {

    private final SafetiesEditor editor;
    private SafetiesDisplayDrawer<MapDrawer> safetiesDisplay;

    SafetiesDisplay(SafetiesEditor editor) {
        this.editor = editor;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                safetiesDisplay.updateClosest(e, true);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                safetiesDisplay.updateClosest(e, false);
            }
        });
    }

    @Override
    public void setup(Main main) {
        super.setup(main);

        this.safetiesDisplay = new SafetiesDisplayDrawer<>(main.pluginAPI, this);
        this.safetiesDisplay.setup(editor);
    }

    @Override
    protected void onPaint() {
        safetiesDisplay.onDraw(mapGraphics);
    }

    public static class SafetiesDisplayDrawer<T extends MapDrawer> extends ZonesDrawer {

        protected final HeroAPI hero;
        protected final StaticEntitiesDrawer staticEntitiesDrawer;
        protected final T drawer;

        private SafetyInfo closest;
        private SafetiesEditor safetiesEditor;

        public SafetiesDisplayDrawer(PluginAPI api, T mapDrawer) {
            super(api.requireAPI(EntitiesAPI.class), api.requireAPI(ConfigAPI.class), api.requireAPI(StarSystemAPI.class));

            this.hero = api.requireAPI(HeroAPI.class);
            this.drawer = mapDrawer;
            this.staticEntitiesDrawer = api.requireInstance(StaticEntitiesDrawer.class);
        }

        private void setup(SafetiesEditor safetiesEditor) {
            this.safetiesEditor = safetiesEditor;
        }

        @Override
        public void onDraw(MapGraphics mg) {
            synchronized (Main.UPDATE_LOCKER) {
                drawZones(mg);
                staticEntitiesDrawer.onDraw(mg);
                drawMap(mg);

                if (safetiesEditor.safetyInfos == null) return;
                drawCustomZones(mg);
            }
        }

        @Override
        protected void drawCustomZones(MapGraphics mg) {
            if (drawer.hovering && closest != null && closest != safetiesEditor.editing) {
                mg.setColor("safety_editor.zone_highlight");
                drawSafeZone(mg, closest);

                mg.setColor("safety_editor.zone_solid");

                Point size = mg.translate(closest.diameter(), closest.diameter());
                mg.drawOval(closest, false, size.x(), size.y());
            }

            if (safetiesEditor.editing != null) {
                mg.setColor("safety_editor.zone_selected");
                drawSafeZone(mg, safetiesEditor.editing);
            }
        }

        protected void drawMap(MapGraphics mg) {
            mg.setColor("text_dark");
            mg.setFont("big");

            mg.drawString(hero.getMap().getName(), mg.getMiddle(), mg.getHeight() / 2 + 12, MapGraphics.Align.MID);
        }

        private void updateClosest(MouseEvent e, boolean edit) {
            Locatable click = drawer.mapGraphics.undoTranslate(e.getX(), e.getY());
            closest = safetiesEditor.safetyInfos.stream()
                    .filter(s -> s.entity != null && !s.entity.removed)
                    .min(Comparator.comparingDouble(s -> Math.pow(s.x - click.getX(), 2) + Math.pow(s.y - click.getY(), 2)))// squared distance
                    .orElse(null);

            drawer.repaint();

            if (closest != null) safetiesEditor.edit(closest);
        }
    }
}
