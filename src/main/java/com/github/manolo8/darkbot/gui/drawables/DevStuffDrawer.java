package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathPoint;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;

import java.util.Collection;

@Feature(name = "DevStuff Drawer", description = "Draws dev infos")
public class DevStuffDrawer implements Drawable {
    private final ConfigSetting<Boolean> showDevStuff;
    private final Collection<? extends Entity> unknown;
    private final EntitiesAPI entities;
    private final Drive drive;

    public DevStuffDrawer(EntitiesAPI entities, ConfigAPI config, Drive drive) {


        this.unknown = entities.getUnknown();
        this.entities = entities;

        this.showDevStuff = config.requireConfig("bot_settings.other.dev_stuff");
        this.drive = drive;
    }

    @Override
    public void onDraw(MapGraphics mg) {
        if (!showDevStuff.getValue()) return;

        mg.setColor("unknown");
        for (Entity entity : unknown) {
            mg.drawRect(entity, false, 3);
        }

        for (PathPoint point : drive.pathFinder.points) {
            mg.drawRect(point, true, 2);
        }

        mg.setFont("tiny");
        entities.getAll().stream()
                //.filter(e -> e.getId() > 150_000_000 && e.getId() < 160_000_000 || e instanceof Mine)
                .filter(e -> e.getLocationInfo().isInitialized())
                .forEach(e -> {
                    Point pos = mg.translate(e);
                    int strWidth = mg.getGraphics2D().getFontMetrics().stringWidth(e.toString());

                    mg.setColor("texts_background");
                    mg.drawRect(Point.of(pos.getX() - (strWidth >> 1), pos.getY() - 7), true, strWidth, 8);
                    mg.setColor("text");
                    mg.drawString(e.toString(), e, 0, MapGraphics.Align.MID);
                });
    }
}
