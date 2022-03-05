package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.gui.trail.Line;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.HeroAPI;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

@Feature(name = "Trail Drawer", description = "Draws hero's trail")
public class TrailDrawer implements Drawable {

    private final ConfigSetting<Integer> trailLength;
    private final ConfigSetting<ColorScheme> cs;
    private final TreeMap<Long, Line> positions = new TreeMap<>();
    private final HeroAPI hero;
    private Location last = Location.of(0, 0);

    public TrailDrawer(HeroAPI hero, ConfigAPI config) {
        this.hero = hero;

        this.cs = config.requireConfig("bot_settings.map_display.cs");
        this.trailLength = config.requireConfig("bot_settings.map_display.trail_length");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawTrail(mg);
    }

    private void drawTrail(MapGraphics mg) {
        double distance = last.distanceTo(hero);

        if (distance > 500) {
            last = hero.getLocationInfo().copy();
        } else if (distance > 100) {
            positions.put(System.currentTimeMillis(), new Line(last, last = hero.getLocationInfo().copy()));
        }
        positions.headMap(System.currentTimeMillis() - trailLength.getValue() * 1000L).clear();

        if (positions.isEmpty()) return;

        mg.getGraphics2D().setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        List<List<Location>> paths = Line.getSmoothedPaths(positions.values());


        double max = paths.stream().mapToInt(Collection::size).sum() / 255d, curr = 0;

        for (List<Location> points : paths) {
            Location last = null;
            for (Location point : points) {
                mg.setColor(cs.getValue().getTrail()[(int) (curr++ / max)]);
                if (last != null) mg.drawLine(last, (Locatable) point);
                last = point;
            }
        }
        mg.getGraphics2D().setStroke(new BasicStroke());
    }
}
