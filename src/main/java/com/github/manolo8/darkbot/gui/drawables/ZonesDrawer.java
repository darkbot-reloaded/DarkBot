package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.SafetyInfo;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.Barrier;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.entities.Mist;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Feature(name = "Zones Drawer", description = "Draws zones")
public class ZonesDrawer implements Drawable {

    protected final ConfigAPI config;
    protected final StarSystemAPI starSystem;

    protected final Collection<? extends Mist> mists;
    protected final Collection<? extends Barrier> barriers;

    protected final ConfigSetting<Set<DisplayFlag>> displayFlags;
    protected final ConfigSetting<Integer> zoneResolution;
    protected final ConfigSetting<Boolean> roamingSequential;

    public ZonesDrawer(EntitiesAPI entities, ConfigAPI config, StarSystemAPI starSystem) {
        this.config = config;
        this.starSystem = starSystem;

        this.mists = entities.getMists();
        this.barriers = entities.getBarriers();

        this.displayFlags = config.requireConfig("bot_settings.map_display.toggle");
        this.zoneResolution = config.requireConfig("bot_settings.other.zone_resolution");
        this.roamingSequential = config.requireConfig("general.roaming.sequential");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawZones(mg);
        drawCustomZones(mg);
    }

    protected void drawZones(MapGraphics mg) {
        for (Barrier barrier : barriers) {
            if (!barrier.use()) return;

            Area.Rectangle bounds = barrier.getZoneArea().getBounds();

            Point pos = mg.translate(bounds.getX(), bounds.getY());
            Point size = mg.translate(bounds.getWidth(), bounds.getHeight());

            mg.setColor("barrier");
            mg.drawRect(pos, true, size.x(), size.y());

            mg.setColor("barrier_border");
            mg.drawRect(pos, false, size.x(), size.y());
        }

        mg.setColor("no_cloack");
        for (Mist mist : mists) {
            Area.Rectangle bounds = mist.getZoneArea().getBounds();

            Point pos = mg.translate(bounds.getX(), bounds.getY());
            Point size = mg.translate(bounds.getWidth(), bounds.getHeight());

            mg.drawRect(pos, true, size.x(), size.y());
        }
    }

    protected void drawCustomZones(MapGraphics mg) {
        if (!hasDisplayFlag(DisplayFlag.ZONES)) return;

        mg.setColor("prefer");
        drawCustomZone(mg, (ZoneInfo) config.getLegacy().getPreferredZone(starSystem.getCurrentMap()));

        if (roamingSequential.getValue())
            drawCustomZonePath(mg, (ZoneInfo) config.getLegacy().getPreferredZone(starSystem.getCurrentMap()));

        mg.setColor("avoid");
        drawCustomZone(mg, (ZoneInfo) config.getLegacy().getAvoidedZone(starSystem.getCurrentMap()));

        mg.setColor("safety");
        for (SafetyInfo safety : config.getLegacy().getSafeties(starSystem.getCurrentMap())) {
            if (safety.getRunMode() == SafetyInfo.RunMode.NEVER
                || !safety.getEntity().filter(Entity::isValid).isPresent()) continue;

            drawSafeZone(mg, safety);
        }
    }

    protected void drawCustomZone(MapGraphics mg, ZoneInfo zoneInfo) {
        if (zoneInfo == null) return;
        for (int x = 0; x < zoneInfo.getResolution(); x++) {
            for (int y = 0; y < zoneInfo.getResolution(); y++) {
                if (!zoneInfo.get(x, y)) continue;

                Point pos = Point.of(gridToMapX(mg, x), gridToMapY(mg, y));
                mg.drawRect(pos, true, gridToMapX(mg, x + 1) - pos.x(), gridToMapY(mg, y + 1) - pos.y());
            }
        }
    }

    protected void drawCustomZonePath(MapGraphics mg, ZoneInfo zoneInfo) {
        if (zoneInfo == null) return;

        List<ZoneInfo.Zone> sortedZones = zoneInfo.getSortedZones();
        Area.Rectangle mapBounds = starSystem.getCurrentMapBounds();

        for (int i = 0; i < sortedZones.size(); i++) {
            Locatable a = sortedZones.get(i).innerPoint(0.5, 0.5, mapBounds.getWidth(), mapBounds.getHeight());
            Locatable b = sortedZones.get((i + 1) % sortedZones.size()).innerPoint(0.5, 0.5, mapBounds.getWidth(), mapBounds.getHeight());
            mg.drawLine(a, b);
        }
    }

    protected void drawSafeZone(MapGraphics mg, SafetyInfo safetyInfo) {
        if (safetyInfo == null) return;

        Point size = mg.translate(safetyInfo.getDiameter(), safetyInfo.getDiameter());
        mg.drawOval(safetyInfo, true, size.x(), size.y());
    }

    protected int gridToMapX(MapGraphics mg, int x) {
        return x * mg.getWidth() / zoneResolution.getValue();
    }

    protected int gridToMapY(MapGraphics mg, int y) {
        return y * mg.getHeight() / zoneResolution.getValue();
    }

    protected boolean hasDisplayFlag(DisplayFlag displayFlag) {
        return displayFlags.getValue().contains(displayFlag);
    }
}
