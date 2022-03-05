package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.BattleStation;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.entities.Station;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.EntitiesAPI;

import java.util.Collection;

@Feature(name = "Static Entities Drawer", description = "Draws static entities")
public class StaticEntitiesDrawer implements Drawable {

    protected final Collection<? extends Portal> portals;
    protected final Collection<? extends Station> stations;
    protected final Collection<? extends BattleStation> battleStations;

    public StaticEntitiesDrawer(EntitiesAPI entities) {
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

    protected void drawPortals(MapGraphics mg) {
        mg.setColor("portals");

        for (Portal portal : portals) {
            mg.drawOval(portal, false, 12);
        }
    }

    protected void drawBattleStations(MapGraphics mg) {
        for (BattleStation bs : battleStations) {
            if (bs.getHullId() == 0) mg.setColor("meteroid");
            else if (bs.getEntityInfo().isEnemy()) mg.setColor("enemies");
            else mg.setColor("allies");

            if (bs.getHullId() >= 0 && bs.getHullId() < 255)
                mg.drawOval(bs, true, 11, 9);

            else mg.drawRect(bs, false, 3);
        }
    }

    protected void drawStations(MapGraphics mg) {
        for (Station station : stations) {
            if (station instanceof Station.Turret) {
                mg.setColor("bases");
                mg.drawOval(station, true, 2);

            } else {
                mg.setColor("base_spots");

                int radius = station instanceof Station.Headquarter ? 3500
                        : station instanceof Station.HomeBase ? 3000 : 1000;

                Point p = mg.translate(radius, radius);
                mg.drawOval(station, true, p.x(), p.y());
            }

        }
    }
}
