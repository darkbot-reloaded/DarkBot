package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.config.types.DisplayFlag;
import eu.darkbot.api.extensions.Draw;
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
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.PetAPI;

import java.util.Collection;

@Feature(name = "Dynamic Entities Drawer", description = "Draws dynamic entities (eg: npcs, boxes, or players)")
@Draw(value = Draw.Stage.DYNAMIC_ENTITIES, attach = Draw.Attach.REPLACE)
public class DynamicEntitiesDrawer implements Drawable {

    private final HeroAPI hero;
    private final PetAPI pet;

    private final Collection<? extends Npc> npcs;
    private final Collection<? extends Box> boxes;
    private final Collection<? extends Mine> mines;
    private final Collection<? extends Pet> pets;
    private final Collection<? extends Player> players;

    public DynamicEntitiesDrawer(HeroAPI hero, PetAPI pet, EntitiesAPI entities) {
        this.hero = hero;
        this.pet = pet;

        this.boxes = entities.getBoxes();
        this.mines = entities.getMines();
        this.npcs = entities.getNpcs();
        this.pets = entities.getPets();
        this.players = entities.getPlayers();
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

            if (mg.hasDisplayFlag(DisplayFlag.RESOURCE_NAMES))
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

        for (Npc npc : npcs)
            drawEntity(mg, npc, npc.getInfo().getShouldKill());

        pet.getLocatorNpcLoc()
                .ifPresent(locator -> {
                    mg.setColor("ping");
                    mg.drawOvalCentered(locator, 16, true);
                    mg.setColor("ping_border");
                    mg.drawOvalCentered(locator, 16, false);
                });
    }

    private void drawPlayers(MapGraphics mg) {
        for (Player player : players) {
            boolean isEnemy = player.getEntityInfo().isEnemy();
            mg.setColor(isEnemy ? "enemies" : "allies");
            drawEntity(mg, player, false);

            if (mg.hasDisplayFlag(DisplayFlag.USERNAMES))
                mg.drawString(player, player.getEntityInfo().getUsername(), -5, MapGraphics.StringAlign.MID);
        }
    }

    private void drawPets(MapGraphics mg) {
        for (Pet pet : pets) {
            mg.setColor(pet.getEntityInfo().isEnemy() ? "enemies" : "allies");
            drawEntity(mg, pet, false);

            if (mg.hasDisplayFlag(DisplayFlag.USERNAMES))
                mg.drawString(pet, pet.getEntityInfo().getUsername(), -5, MapGraphics.StringAlign.MID);
        }
    }

    private void drawDestinations(MapGraphics mg) {
        if (!mg.hasDisplayFlag(DisplayFlag.SHOW_DESTINATION)) return;

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
        }
    }

    private void drawEntity(MapGraphics mg, Locatable pos, boolean fill) {
        if (!fill) mg.drawRect(mg.toScreenPointX(pos.getX()) - 2,
                mg.toScreenPointY(pos.getY()) - 2, 3, 3, false);
        else mg.drawRectCentered(pos, 4, true);
    }
}
