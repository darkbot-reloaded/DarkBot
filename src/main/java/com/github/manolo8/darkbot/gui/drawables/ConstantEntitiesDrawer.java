package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.core.manager.EffectManager;
import com.github.manolo8.darkbot.extensions.features.Feature;
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

        for (Portal portal : portals) {
            mg.drawOvalCentered(portal, 12, false);
        }
    }

    public void drawBattleStations(MapGraphics mg) {
        for (BattleStation bs : battleStations) {
            if (bs.getHullId() == 0) mg.setColor("meteroid");
            else if (bs.getEntityInfo().isEnemy()) mg.setColor("enemies");
            else mg.setColor("allies");

            if (bs.getHullId() >= 0 && bs.getHullId() < 255) {
                mg.drawOvalCentered(bs, 11, 9, true);


//                if (bs.hasEffect(EffectManager.Effect.DEFLECTOR_SHIELD.getId())) {
//                    mg.setColor("meteroid");
//                    mg.drawOvalCentered(bs, 30, 25, false);
//                }
            }

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

                int size = station instanceof Station.Headquarter ? 3500
                        : station instanceof Station.HomeBase ? 3000 : 1000;

                mg.drawOvalCentered(station, mg.toScreenPointX(size), mg.toScreenPointY(size), true);
            }

        }
    }
}
