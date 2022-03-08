package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathPoint;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.entities.Mine;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;

import java.util.Collection;

@Feature(name = "DevStuff Drawer", description = "Draws dev infos")
public class DevStuffDrawer implements Drawable {

    private final Drive drive;
    private final EntitiesAPI entities;

    private final Collection<? extends Entity> unknown;

    private final ConfigSetting<Boolean> showDevStuff;

    public DevStuffDrawer(EntitiesAPI entities, ConfigAPI config, Drive drive) {
        this.drive = drive;
        this.entities = entities;

        this.unknown = entities.getUnknown();

        this.showDevStuff = config.requireConfig("bot_settings.other.dev_stuff");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        if (!showDevStuff.getValue()) return;

        mg.setColor("unknown");
        for (Entity entity : unknown) {
            mg.drawRectCentered(entity, 3, false);
        }

        for (PathPoint point : drive.pathFinder.points) {
            mg.drawRectCentered(point, 2, true);
        }

        mg.setFont("tiny");
        mg.setColor("text");
        entities.getAll().stream()
                .filter(e -> e.getId() > 150_000_000 && e.getId() < 160_000_000 || e instanceof Mine)
                .filter(e -> e.getLocationInfo().isInitialized())
                .forEach(e -> mg.drawBackgroundedText(e, e.toString(), -4, MapGraphics.StringAlign.MID));
    }
}
