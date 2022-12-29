package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.DisplayFlag;
import eu.darkbot.api.config.types.SafetyInfo;
import eu.darkbot.api.config.types.ZoneInfo;
import eu.darkbot.api.extensions.Draw;
import eu.darkbot.api.extensions.Drawable;
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

@Feature(name = "Zones Drawer", description = "Draws zones (eg: barriers, mist, bot zones)")
@Draw(value = Draw.Stage.ZONES, attach = Draw.Attach.REPLACE)
public class ZonesDrawer implements Drawable {

    private final ConfigAPI config;
    private final StarSystemAPI starSystem;

    private final Collection<? extends Mist> mists;
    private final Collection<? extends Barrier> barriers;

    private final ConfigSetting<Integer> zoneResolution;
    private final ConfigSetting<Boolean> roamingSequential;


    public ZonesDrawer(EntitiesAPI entities, ConfigAPI config, StarSystemAPI starSystem) {
        this.config = config;
        this.starSystem = starSystem;

        this.mists = entities.getMists();
        this.barriers = entities.getBarriers();

        this.zoneResolution = config.requireConfig("bot_settings.other.zone_resolution");
        this.roamingSequential = config.requireConfig("general.roaming.sequential");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawZones(mg);
        drawCustomZones(mg);
    }

    public void drawZones(MapGraphics mg) {
        for (Barrier barrier : barriers) {
            if (!barrier.use()) return;

            mg.setColor("barrier");
            mg.drawArea(barrier.getZoneArea(), true);

            mg.setColor("barrier_border");
            mg.drawArea(barrier.getZoneArea(), false);
        }

        mg.setColor("no_cloack");
        for (Mist mist : mists) {
            mg.drawArea(mist.getZoneArea(), true);
        }
    }

    public void drawCustomZones(MapGraphics mg) {
        if (!mg.hasDisplayFlag(DisplayFlag.ZONES)) return;

        mg.setColor("prefer");
        drawCustomZone(mg, config.getLegacy().getPreferredZone(starSystem.getCurrentMap()));

        if (roamingSequential.getValue())
            drawCustomZonePath(mg, config.getLegacy().getPreferredZone(starSystem.getCurrentMap()));

        mg.setColor("avoid");
        drawCustomZone(mg, config.getLegacy().getAvoidedZone(starSystem.getCurrentMap()));

        mg.setColor("safety");
        for (SafetyInfo safety : config.getLegacy().getSafeties(starSystem.getCurrentMap())) {
            if (safety.getRunMode() == SafetyInfo.RunMode.NEVER
                || !safety.getEntity().filter(Entity::isValid).isPresent()) continue;

            drawSafeZone(mg, safety);
        }
    }

    public void drawCustomZone(MapGraphics mg, ZoneInfo zoneInfo) {
        if (zoneInfo == null) return;

        Area.Rectangle mapBounds = starSystem.getCurrentMapBounds();
        int width = (int) mg.toScreenSizeW(mapBounds.getWidth());
        int height = (int) mg.toScreenSizeH(mapBounds.getHeight());
        for (int x = 0; x < zoneInfo.getResolution(); x++) {
            for (int y = 0; y < zoneInfo.getResolution(); y++) {
                if (!zoneInfo.get(x, y)) continue;

                Point pos = Point.of(gridToMapX(mg, x, width), gridToMapY(mg, y, height));
                mg.drawRect(pos, gridToMapX(mg, x + 1, width) - pos.x(), gridToMapY(mg, y + 1, height) - pos.y(), true);
            }
        }
    }

    public void drawCustomZonePath(MapGraphics mg, ZoneInfo zoneInfo) {
        if (!(zoneInfo instanceof com.github.manolo8.darkbot.config.ZoneInfo)) return;

        List<com.github.manolo8.darkbot.config.ZoneInfo.Zone> sortedZones =
                ((com.github.manolo8.darkbot.config.ZoneInfo) zoneInfo).getSortedZones();

        Area.Rectangle mapBounds = starSystem.getCurrentMapBounds();
        Locatable[] zones = new Locatable[sortedZones.size()];

        for (int i = 0; i < sortedZones.size(); i++)
            zones[i] = sortedZones.get(i).innerPoint(0.5, 0.5, mapBounds.getWidth(), mapBounds.getHeight());

        mg.drawPoly(MapGraphics.PolyType.DRAW_POLYGON, zones);
    }

    public void drawSafeZone(MapGraphics mg, SafetyInfo safetyInfo) {
        if (safetyInfo == null) return;

        mg.drawOvalCentered(safetyInfo, mg.toScreenSizeW(safetyInfo.getDiameter()),
                mg.toScreenSizeH(safetyInfo.getDiameter()), true);
    }

    private int gridToMapX(MapGraphics mg, int x, int width) {
        return (int) (mg.toScreenPointX(0) + x * width / zoneResolution.getValue());
    }

    private int gridToMapY(MapGraphics mg, int y, int height) {
        return (int) (mg.toScreenPointY(0) + y * height / zoneResolution.getValue());
    }
}
