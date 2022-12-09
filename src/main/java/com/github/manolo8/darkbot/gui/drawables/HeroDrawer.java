package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.pathfinder.PolygonImpl;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.extensions.Draw;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.MovementAPI;

import java.util.List;

@Feature(name = "Hero drawer", description = "Draws hero & pet on the bot map")
@Draw(value = Draw.Stage.HERO_AND_PET, attach = Draw.Attach.REPLACE)
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
        mg.drawOvalCentered(hero, 8, true);

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
        PolygonImpl view = mapManager.viewBounds.polygon;
        if (view.isEmpty()) return;

        mg.setColor("barrier_border");
        mg.drawArea(view, false);
    }

    private void drawPet(MapGraphics mg) {
        hero.getPet().ifPresent(pet -> {
            mg.setColor("pet");
            mg.drawRectCentered(pet, 6, true);

            mg.setColor("pet_in");
            mg.drawRectCentered(pet, 4, true);
        });
    }
}
