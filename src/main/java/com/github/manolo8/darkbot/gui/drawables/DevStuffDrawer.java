package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathPoint;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Draw;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.entities.Mine;
import eu.darkbot.api.game.entities.Ship;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.HeroAPI;

import java.awt.Color;
import java.awt.RenderingHints;
import java.util.Collection;

@Feature(name = "DevStuff Drawer", description = "Draws dev infos (eg: unknown entities, pathfinding points, and entity metadata)")
@Draw(value = Draw.Stage.DEV_STUFF, attach = Draw.Attach.REPLACE)
public class DevStuffDrawer implements Drawable {

    private static final Color PATH_COLOR = new Color(0, 128, 255, 64);

    private final Drive drive;

    private final Collection<? extends Entity> unknown;
    private final Collection<? extends Entity> allEntities;

    private final ConfigSetting<Boolean> showDevStuff;
    private final HeroAPI hero;

    public DevStuffDrawer(EntitiesAPI entities, ConfigAPI config, Drive drive, HeroAPI hero) {
        this.drive = drive;
        this.hero = hero;

        this.unknown = entities.getUnknown();
        this.allEntities = entities.getAll();

        this.showDevStuff = config.requireConfig("bot_settings.other.dev_stuff");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        if (!showDevStuff.getValue()) return;

        mg.setColor("unknown");
        for (Entity entity : unknown) {
            mg.drawRectCentered(entity, 3, false);
        }

        mg.setColor(PATH_COLOR);
        for (PathPoint point : ((HeroManager) hero).drive.pathFinder.getPathPoints()) {
            for (PathPoint other : point.lineOfSight) {
                mg.drawLine(mg.toScreenPointX(point.x),
                        mg.toScreenPointY(point.y),
                        mg.toScreenPointX(point.x + (other.x - point.x) / 3),
                        mg.toScreenPointY(point.y + (other.y - point.y) / 3));
            }
        }

        mg.setColor("unknown");
        for (Locatable point : drive.pathFinder.getPathPoints()) {
            mg.drawRectCentered(point, 3, true);
        }

        Object renderingHint = mg.getGraphics2D().getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        mg.getGraphics2D().setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        mg.setFont("tiny");
        mg.setColor("text");
        allEntities.stream()
                .filter(e -> e.getId() > 150_000_000 && e.getId() < 160_000_000 || e instanceof Mine || e instanceof Ship)
                .filter(e -> e.getLocationInfo().isInitialized())
                .forEach(e -> mg.drawBackgroundedText(e, e.toString(), -4, MapGraphics.StringAlign.MID));

        Point p = Point.of(mg.getWidthMiddle() - 20, mg.getHeight() - 40);

        mg.setFont("small");
        mg.drawBackgroundedText(p, hero.toString(), MapGraphics.StringAlign.RIGHT);

        Lockable target = hero.getLocalTarget();
        if (target != null && target.isValid()) {
            p = Point.of(mg.getWidth() - 20, mg.getHeight() - 40);
            mg.drawBackgroundedText(p, target.toString(), MapGraphics.StringAlign.RIGHT);
        }

        mg.getGraphics2D().setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, renderingHint);
    }
}
