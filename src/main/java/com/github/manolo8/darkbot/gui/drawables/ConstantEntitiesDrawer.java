package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.core.entities.Zone;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.SafetyInfo;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.Barrier;
import eu.darkbot.api.game.entities.BattleStation;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.entities.Mist;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.entities.Station;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Feature(name = "Constant Entities Drawer", description = "Draws not-movable entities")
public class ConstantEntitiesDrawer implements Drawable {

    protected final ConfigAPI config;
    protected final StarSystemAPI starSystem;

    protected final Collection<? extends Mist> mists;
    protected final Collection<? extends Barrier> barriers;
    protected final Collection<? extends Portal> portals;
    protected final Collection<? extends Station> stations;
    protected final Collection<? extends BattleStation> battleStations;

    protected final ConfigSetting<Set<DisplayFlag>> displayFlags;
    protected final ConfigSetting<Integer> zoneResolution;
    protected final ConfigSetting<Boolean> roamingSequential;

    public ConstantEntitiesDrawer(EntitiesAPI entities, ConfigAPI config, StarSystemAPI starSystem) {
        this.config = config;
        this.starSystem = starSystem;

        this.mists = entities.getMists();
        this.barriers = entities.getBarriers();
        this.portals = entities.getPortals();
        this.stations = entities.getStations();
        this.battleStations = entities.getBattleStations();

        this.displayFlags = config.requireConfig("bot_settings.map_display.toggle");
        this.zoneResolution = config.requireConfig("bot_settings.other.zone_resolution");
        this.roamingSequential = config.requireConfig("general.roaming.sequential");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawZones(mg);
        drawCustomZones(mg);

        drawPortals(mg);
        drawBattleStations(mg);
        drawStations(mg);
    }

    public void drawZones(MapGraphics mg) {
        for (Barrier barrier : barriers) {
            if (!barrier.use()) return;

            mg.setColor("barrier");
            mg.drawPoly(MapGraphics.PolyType.FILL_POLYGON, ((Zone) barrier).points);

            mg.setColor("barrier_border");
            mg.drawPoly(MapGraphics.PolyType.DRAW_POLYGON, ((Zone) barrier).points);
        }

        mg.setColor("no_cloack");
        for (Mist mist : mists) {
            mg.drawPoly(MapGraphics.PolyType.FILL_POLYGON, ((Zone)mist).points);
        }
    }

     public void drawCustomZones(MapGraphics mg) {
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

    public void drawCustomZone(MapGraphics mg, ZoneInfo zoneInfo) {
        if (zoneInfo == null) return;
        for (int x = 0; x < zoneInfo.getResolution(); x++) {
            for (int y = 0; y < zoneInfo.getResolution(); y++) {
                if (!zoneInfo.get(x, y)) continue;

                Point pos = Point.of(gridToMapX(mg, x), gridToMapY(mg, y));
                mg.drawRect(pos, gridToMapX(mg, x + 1) - pos.x(), gridToMapY(mg, y + 1) - pos.y(), true);
            }
        }
    }

    public void drawCustomZonePath(MapGraphics mg, ZoneInfo zoneInfo) {
        if (zoneInfo == null) return;

        List<ZoneInfo.Zone> sortedZones = zoneInfo.getSortedZones();
        Area.Rectangle mapBounds = starSystem.getCurrentMapBounds();

        for (int i = 0; i < sortedZones.size(); i++) {
            Locatable a = sortedZones.get(i).innerPoint(0.5, 0.5, mapBounds.getWidth(), mapBounds.getHeight());
            Locatable b = sortedZones.get((i + 1) % sortedZones.size()).innerPoint(0.5, 0.5, mapBounds.getWidth(), mapBounds.getHeight());
            mg.drawLine(a, b);
        }
    }

    public void drawSafeZone(MapGraphics mg, SafetyInfo safetyInfo) {
        if (safetyInfo == null) return;

        mg.drawOvalCentered(safetyInfo, mg.toScreenPointX(safetyInfo.getDiameter()),
                mg.toScreenPointY(safetyInfo.getDiameter()), true);
    }

    public void drawPortals(MapGraphics mg) {
        mg.setColor("portals");

        for (Portal portal : portals) {
            mg.drawOvalCentered(portal, 12, false);
        }
    }

    public void drawBattleStations(MapGraphics mg) {
        for (BattleStation bs : battleStations) {
            if (bs.getHullId() == 0) mg.setColor("meteroid");
            else if (bs.getEntityInfo().isEnemy()) mg.setColor("enemies");
            else mg.setColor("allies");

            if (bs.getHullId() >= 0 && bs.getHullId() < 255)
                mg.drawOvalCentered(bs, 11, 9, true);

            else mg.drawRectCentered(bs, 3, false);
        }
    }

    public void drawStations(MapGraphics mg) {
        for (Station station : stations) {
            if (station instanceof Station.Turret) {
                mg.setColor("bases");
                mg.drawOvalCentered(station, 2, true);

            } else {
                mg.setColor("base_spots");

                int radius = station instanceof Station.Headquarter ? 3500
                        : station instanceof Station.HomeBase ? 3000 : 1000;
                
                mg.drawOvalCentered(station, mg.toScreenPointX(radius), mg.toScreenPointY(radius), true);
            }

        }
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
