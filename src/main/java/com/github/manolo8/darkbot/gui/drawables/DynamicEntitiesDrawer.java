package com.github.manolo8.darkbot.gui.drawables;

import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.DisplayFlag;
import eu.darkbot.api.extensions.Draw;
import eu.darkbot.api.extensions.Drawable;
import eu.darkbot.api.extensions.MapGraphics;
import eu.darkbot.api.game.entities.Box;
import eu.darkbot.api.game.entities.Mine;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Pet;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.entities.Relay;
import eu.darkbot.api.game.entities.SpaceBall;
import eu.darkbot.api.game.entities.StaticEntity;
import eu.darkbot.api.game.group.GroupMember;
import eu.darkbot.api.game.group.PartialGroupMember;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.game.other.Movable;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.GroupAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.PetAPI;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Feature(name = "Dynamic Entities Drawer", description = "Draws dynamic entities (eg: npcs, boxes, or players)")
@Draw(value = Draw.Stage.DYNAMIC_ENTITIES, attach = Draw.Attach.REPLACE)
public class DynamicEntitiesDrawer implements Drawable {

    private final HeroAPI hero;
    private final PetAPI pet;
    private final GroupAPI group;

    private final ConfigSetting<Boolean> roundEntities;

    private final Collection<? extends Npc> npcs;
    private final Collection<? extends Box> boxes;
    private final Collection<? extends Mine> mines;
    private final Collection<? extends Pet> pets;
    private final Collection<? extends Player> players;
    private final Collection<? extends Relay> relays;
    private final Collection<? extends SpaceBall> spaceBalls;
    private final Collection<? extends StaticEntity> staticEntities;

    public DynamicEntitiesDrawer(HeroAPI hero, PetAPI pet, GroupAPI group, ConfigAPI config, EntitiesAPI entities) {
        this.hero = hero;
        this.pet = pet;
        this.group = group;

        this.roundEntities = config.requireConfig("bot_settings.map_display.round_entities");

        this.boxes = entities.getBoxes();
        this.mines = entities.getMines();
        this.npcs = entities.getNpcs();
        this.pets = entities.getPets();
        this.players = entities.getPlayers();

        this.relays = entities.getRelays();
        this.spaceBalls = entities.getSpaceBalls();
        this.staticEntities = entities.getStaticEntities();
    }

    @Override
    public void onDraw(MapGraphics mg) {
        drawBoxes(mg);
        drawMines(mg);

        drawRelays(mg);
        drawSpaceBalls(mg);
        drawStaticEntities(mg);

        drawDestinations(mg);
        drawNpcs(mg);
        drawPets(mg);
        drawPlayers(mg);

        drawHeroTarget(mg);
    }

    private void drawRelays(MapGraphics mg) {
        mg.setColor("low_relays");
        for (Relay relay : relays) {
            drawEntity(mg, relay, 4, true);
        }
    }

    private void drawSpaceBalls(MapGraphics mg) {
        mg.setColor("space_balls");
        for (SpaceBall spaceBall : spaceBalls) {
            drawEntity(mg, spaceBall, 6, true);
        }
    }

    private void drawStaticEntities(MapGraphics mg) {
        mg.setColor("other_entities");
        for (StaticEntity staticEntity : staticEntities) {
            drawEntity(mg, staticEntity, 2, false);
        }
    }

    private void drawBoxes(MapGraphics mg) {
        mg.setColor("boxes");

        for (Box box : boxes) {
            drawEntity(mg, box, 3, box.getInfo().shouldCollect());

            if (mg.hasDisplayFlag(DisplayFlag.RESOURCE_NAMES))
                mg.drawString(box, box.getTypeName(), -5, MapGraphics.StringAlign.MID);
        }
    }

    private void drawMines(MapGraphics mg) {
        mg.setColor("mines");

        for (Mine mine : mines) {
            drawEntity(mg, mine, 3, true);
        }
    }

    private void drawNpcs(MapGraphics mg) {
        mg.setColor("npcs");
        mg.setFont("small");

        for (Npc npc : npcs) {
            drawEntity(mg, npc, 4, npc.getInfo().getShouldKill());
            if (mg.hasDisplayFlag(DisplayFlag.NPC_NAMES))
                mg.drawString(npc, npc.getEntityInfo().getUsername(), -6, MapGraphics.StringAlign.MID);
        }

        pet.getLocatorNpcLoc()
                .ifPresent(locator -> {
                    mg.setColor("ping");
                    mg.drawOvalCentered(locator, 16.0, true);
                    mg.setColor("ping_border");
                    mg.drawOvalCentered(locator, 16.0, false);
                });
    }

    private void drawPlayers(MapGraphics mg) {
        Set<Integer> oosMembers = group.getMembers().stream()
                .map(PartialGroupMember::getId)
                .collect(Collectors.toSet());

        Color ally = mg.getColor("allies");
        Color enemy = mg.getColor("enemies");
        Color groupMember = mg.getColor("group_member");

        for (Player player : players) {
            boolean isEnemy = player.getEntityInfo().isEnemy();
            boolean isGroupMember = oosMembers.remove(player.getId()) && group.hasGroup();

            Color color = isGroupMember ? groupMember : isEnemy ? enemy : ally;
            mg.setColor(color);

            drawEntity(mg, player, 4, isGroupMember);
            if (mg.hasDisplayFlag(DisplayFlag.USERNAMES))
                mg.drawString(player, player.getEntityInfo().getUsername(), -6, MapGraphics.StringAlign.MID);
        }

        if (group.hasGroup()) {
            mg.setColor(groupMember.darker());

            for (GroupMember member : group.getMembers()) {
                if (member.getMapId() == hero.getMap().getId() && oosMembers.contains(member.getId())) {
                    drawEntity(mg, member.getLocation(), 4, false);
                    if (mg.hasDisplayFlag(DisplayFlag.USERNAMES))
                        mg.drawString(member.getLocation(), member.getUsername(), -6, MapGraphics.StringAlign.MID);
                }
            }
        }
    }

    private void drawPets(MapGraphics mg) {
        for (Pet pet : pets) {
            mg.setColor(pet.getEntityInfo().isEnemy() ? "enemies" : "allies");

            drawEntity(mg, pet, 4, false);

            if (mg.hasDisplayFlag(DisplayFlag.USERNAMES))
                mg.drawString(pet, pet.getEntityInfo().getUsername(), -6, MapGraphics.StringAlign.MID);
        }
    }

    private void drawDestinations(MapGraphics mg) {
        if (!mg.hasDisplayFlag(DisplayFlag.SHOW_DESTINATION)) return;

        mg.setColor("going");
        for (Player player : players)
            player.getDestination()
                    .filter(dest -> player.distanceTo(dest) > 10)
                    .ifPresent(dest -> mg.drawLine(player, dest));

        for (Pet pet : pets)
            pet.getDestination()
                    .filter(dest -> pet.distanceTo(dest) > 10)
                    .ifPresent(dest -> mg.drawLine(pet, dest));

        for (Npc npc : npcs)
            npc.getDestination()
                    .filter(dest -> npc.distanceTo(dest) > 10)
                    .ifPresent(dest -> mg.drawLine(npc, dest));
    }

    private void drawHeroTarget(MapGraphics mg) {
        Lockable target = hero.getLocalTarget();

        if (target != null && target.isValid()) {
            if (target instanceof Movable) {
                ((Movable) target).getDestination()
                        .filter(dest -> target.distanceTo(dest) > 10)
                        .ifPresent(destination -> {
                            mg.setColor("going");
                            mg.drawLine(target, destination);
                        });
            }

            mg.setColor("target");
            drawEntity(mg, target, 4, true, true);
        }
    }

    private void drawEntity(MapGraphics mg, Locatable entity, double size, boolean fill) {
        drawEntity(mg, entity, size, fill, false);
    }

    private void drawEntity(MapGraphics mg, Locatable entity, double size, boolean fill, boolean target) {
        if (!target && entity == hero.getLocalTarget()) return; // don't paint entity from loop if is a target
        if (fill) size += 1;

        if (roundEntities.getValue())
            mg.drawOvalCentered(entity, size + 2, fill);
        else mg.drawRectCentered(entity, size, fill);
    }
}
