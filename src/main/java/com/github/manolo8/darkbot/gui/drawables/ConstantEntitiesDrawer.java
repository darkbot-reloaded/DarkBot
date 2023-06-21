package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.config.types.DisplayFlag;
import eu.darkbot.api.extensions.Draw;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.BattleStation;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.entities.Station;
import eu.darkbot.api.managers.EntitiesAPI;

import java.util.Collection;

@Feature(name = "Constant Entities Drawer", description = "Draws entities that are constantly on the map (eg: portals, CBS, and stations)")
@Draw(value = Draw.Stage.CONSTANT_ENTITIES, attach = Draw.Attach.REPLACE)
public class ConstantEntitiesDrawer implements Drawable {

    private final Collection<? extends Portal> portals;
    private final Collection<? extends Station> stations;
    private final Collection<? extends BattleStation> battleStations;

    public ConstantEntitiesDrawer(EntitiesAPI entities) {
        this.portals = entities.getPortals();
        this.stations = entities.getStations();
        this.battleStations = entities.getBattleStations();
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawPortals(mg);
        drawBattleStations(mg);
        drawStations(mg);
    }

    public void drawPortals(MapGraphics mg) {
        mg.setColor("portals");
        mg.setFont("small");

        for (Portal portal : portals) {
            mg.drawOvalCentered(portal, 12.0, false);
            portal.getTargetMap()
                    .ifPresent(gameMap -> mg.drawString(portal, gameMap.getShortName(), -8, MapGraphics.StringAlign.MID));
        }
    }

    public void drawBattleStations(MapGraphics mg) {
        for (BattleStation bs : battleStations) {
            if (bs instanceof BattleStation.Asteroid) {
                mg.setColor("meteroid");
            } else {
                mg.setColor(bs.getEntityInfo().isEnemy() ? "enemies" : "allies");
            }

            if (bs instanceof BattleStation.Module) {
                mg.drawRectCentered(bs, 3.0, false);
            } else {
                mg.drawOvalCentered(bs, 11.0, 9.0, true);

                if (mg.hasDisplayFlag(DisplayFlag.USERNAMES)) {
                    String name = bs instanceof BattleStation.Hull ? ("[" + bs.getEntityInfo().getClanTag() + "] ") : "";
                    name += bs.getEntityInfo().getUsername();

                    mg.drawString(bs, name, bs instanceof BattleStation.Hull ? -14 : -10, MapGraphics.StringAlign.MID);
                }
            }
        }
    }

    public void drawStations(MapGraphics mg) {
        for (Station station : stations) {
            if (station instanceof Station.Turret) {
                mg.setColor("bases");
                mg.drawOvalCentered(station, 2.0, true);

            } else {
                mg.setColor("base_spots");

                int size = station instanceof Station.Headquarter ? 3500
                        : station instanceof Station.HomeBase ? 3000 : 1000;

                mg.drawOvalCentered(station, mg.toScreenSizeW(size), mg.toScreenSizeH(size), true);
            }
        }
    }
}
