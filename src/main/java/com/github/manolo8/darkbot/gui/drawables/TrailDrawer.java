package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.config.ColorScheme;
import com.github.manolo8.darkbot.gui.trail.Line;
import com.github.manolo8.darkbot.utils.data.RecyclingQueue;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Draw;
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

@Feature(name = "Trail Drawer", description = "Draws hero's trail")
@Draw(value = Draw.Stage.HERO_TRAIL, attach = Draw.Attach.REPLACE)
public class TrailDrawer implements Drawable {

    private final ConfigSetting<Integer> trailLength;
    private final ConfigSetting<ColorScheme> cs;
    private final RecyclingQueue<Line> positions = new RecyclingQueue<>(Line::new);
    private final HeroAPI hero;
    private final Location last = Location.of(0, 0);

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
            last.setTo(hero);
        } else if (distance > 25) {
            positions.add().init(last, hero);
            last.setTo(hero);
        }

        long removeBefore = System.currentTimeMillis() - (trailLength.getValue() * 1000L);
        while (!positions.isEmpty()) {
            Line line = positions.get();
            if (line.getTime() > removeBefore) break;
            positions.remove();
        }

        if (positions.isEmpty()) return;

        Stroke stroke = mg.getGraphics2D().getStroke();
        mg.getGraphics2D().setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        List<List<Location>> paths = Line.getSmoothedPaths(positions);

        double max = paths.stream().mapToInt(Collection::size).sum() / 255d, curr = 0;
        for (List<Location> points : paths) {
            Location last = null;
            for (Location point : points) {
                mg.setColor(cs.getValue().getTrail()[(int) (curr++ / max)]);
                if (last != null) mg.drawLine(last, (Locatable) point);
                last = point;
            }
        }

        mg.getGraphics2D().setStroke(stroke);
    }
}
