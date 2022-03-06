package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Point;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.MovementAPI;

import java.util.List;

@Feature(name = "Hero drawer", description = "Draws hero & pet on the bot map")
public class HeroDrawer implements Drawable {

    private final HeroAPI hero;
    private final MovementAPI movement;
    private final MapManager mapManager;

    public HeroDrawer(HeroAPI hero, MovementAPI movement, MapManager mapManager) {
        this.hero = hero;
        this.movement = movement;
        this.mapManager = mapManager;
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawConfiguration(mg);

        if (!hero.isValid() || !hero.getLocationInfo().isInitialized()) return;

        drawHeroDestination(mg);

        mg.setColor("hero");
        mg.drawOval(hero, 8, true);

        drawScreenBounds(mg);
        drawPet(mg);
    }

    private void drawConfiguration(MapGraphics mg) {
        mg.setColor("text");
        mg.setFont("small");

        mg.drawString(12, mg.getHeight() - 12, hero.getConfiguration().toString(), MapGraphics.StringAlign.LEFT);
    }

    private void drawHeroDestination(MapGraphics mg) {
        mg.setColor("going");

        Locatable begin = hero;
        List<? extends Locatable> paths = movement.getPath();
        if (!paths.isEmpty()) {
            for (Locatable path : paths)
                mg.drawLine(begin, begin = path);

        } else {
            hero.getDestination().ifPresent(destination -> mg.drawLine(hero, destination));
        }
    }

    private void drawScreenBounds(MapGraphics mg) {
        MapManager.ViewBounds view = mapManager.viewBounds;
        if (view.address == 0) return;

        Point tl = mg.toScreenPoint(view.leftTopX, view.leftTopY);
        Point tr = mg.toScreenPoint(view.rightTopX, view.rightTopY);
        Point br = mg.toScreenPoint(view.rightBotX, view.rightBotY);
        Point bl = mg.toScreenPoint(view.leftBotX, view.leftBotY);

        mg.setColor("barrier_border");
        mg.drawPoly(MapGraphics.PolyType.DRAW_POLYGON, tl, tr, br, bl);
    }

    private void drawPet(MapGraphics mg) {
        hero.getPet().ifPresent(pet -> {
            mg.setColor("pet");
            mg.drawRect(pet, 6, true);

            mg.setColor("pet_in");
            mg.drawRect(pet, 4, true);
        });
    }
}
