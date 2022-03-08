package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.core.entities.FakeNpc;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.Box;
import eu.darkbot.api.game.entities.Mine;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Pet;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.game.other.Movable;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.HeroAPI;

import java.util.Collection;
import java.util.Set;

@Feature(name = "Dynamic Entities Drawer", description = "Draws dynamic entities")
public class DynamicEntitiesDrawer implements Drawable {

    private final HeroAPI hero;

    private final Collection<? extends Npc> npcs;
    private final Collection<? extends Box> boxes;
    private final Collection<? extends Mine> mines;
    private final Collection<? extends Pet> pets;
    private final Collection<? extends Player> players;

    private final ConfigSetting<Set<DisplayFlag>> displayFlags;

    public DynamicEntitiesDrawer(HeroAPI hero, EntitiesAPI entities, ConfigAPI config) {
        this.hero = hero;

        this.boxes = entities.getBoxes();
        this.mines = entities.getMines();
        this.npcs = entities.getNpcs();
        this.pets = entities.getPets();
        this.players = entities.getPlayers();

        this.displayFlags = config.requireConfig("bot_settings.map_display.toggle");
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawBoxes(mg);
        drawMines(mg);

        drawDestinations(mg);
        drawNpcs(mg);
        drawPets(mg);
        drawPlayers(mg);

        drawHeroTarget(mg);
    }

    private void drawBoxes(MapGraphics mg) {
        mg.setColor("boxes");

        for (Box box : boxes) {
            drawEntity(mg, box, box.getInfo().shouldCollect());

            if (hasDisplayFlag(DisplayFlag.RESOURCE_NAMES))
                mg.drawString(box, box.getTypeName(), -5, MapGraphics.StringAlign.MID);
        }
    }

    private void drawMines(MapGraphics mg) {
        mg.setColor("mines");

        for (Mine mine : mines)
            drawEntity(mg, mine, true);
    }

    private void drawNpcs(MapGraphics mg) {
        mg.setColor("npcs");

        FakeNpc pingEntity = null;
        for (Npc npc : npcs) {
            if (npc instanceof FakeNpc)
                pingEntity = (FakeNpc) npc;

            drawEntity(mg, npc, npc.getInfo().getShouldKill());
        }

        if (pingEntity != null) {
            mg.setColor("ping");
            mg.drawOvalCentered(pingEntity, 15, true);
            mg.setColor("ping_border");
            mg.drawOvalCentered(pingEntity, 15, false);
        }
    }

    private void drawPlayers(MapGraphics mg) {
        for (Player player : players) {
            boolean isEnemy = player.getEntityInfo().isEnemy();
            mg.setColor(isEnemy ? "enemies" : "allies");
            drawEntity(mg, player, false);


            if (hasDisplayFlag(DisplayFlag.USERNAMES))
                mg.drawString(player, player.getEntityInfo().getUsername(), -5, MapGraphics.StringAlign.MID);
        }
    }

    private void drawPets(MapGraphics mg) {
        for (Pet pet : pets) {
            mg.setColor(pet.getEntityInfo().isEnemy() ? "enemies" : "allies");
            drawEntity(mg, pet, false);

            if (hasDisplayFlag(DisplayFlag.USERNAMES))
                mg.drawString(pet, pet.getEntityInfo().getUsername(), -5, MapGraphics.StringAlign.MID);
        }
    }

    private void drawDestinations(MapGraphics mg) {
        if (!hasDisplayFlag(DisplayFlag.SHOW_DESTINATION)) return;

        mg.setColor("going");
        for (Player player : players)
            player.getDestination().ifPresent(dest -> mg.drawLine(player, dest));

        for (Pet pet : pets)
            pet.getDestination().ifPresent(dest -> mg.drawLine(pet, dest));

        for (Npc npc : npcs)
            npc.getDestination().ifPresent(dest -> mg.drawLine(npc, dest));
    }

    private void drawHeroTarget(MapGraphics mg) {
        Lockable target = hero.getLocalTarget();

        if (target != null && target.isValid()) {
            if (target instanceof Movable) {
                ((Movable) target).getDestination().ifPresent(destination -> {
                    mg.setColor("going");
                    mg.drawLine(target, destination);
                });
            }

            mg.setColor("target");
            mg.drawRectCentered(target, 4, true);

            // target circle
//            mg.getGraphics2D().setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[] {3, 5},  0.0f));
//            mg.drawOvalCentered(target, false, 21);
//            mg.getGraphics2D().setStroke(new BasicStroke());

        }
    }

    private void drawEntity(MapGraphics mg, Locatable pos, boolean fill) {
        mg.drawRectCentered(pos, fill ? 4 : 3, fill);
    }

    private boolean hasDisplayFlag(DisplayFlag displayFlag) {
        return displayFlags.getValue().contains(displayFlag);
    }
}
