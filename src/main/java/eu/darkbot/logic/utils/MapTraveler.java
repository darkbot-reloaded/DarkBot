package eu.darkbot.logic.utils;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.utils.Map;
import eu.darkbot.api.managers.*;
import eu.darkbot.api.objects.Location;
import eu.darkbot.api.utils.Listener;

import java.util.Collection;

public class MapTraveler {
    protected final PetAPI pet;
    protected final HeroAPI hero;
    protected final StarSystemAPI star;
    protected final MovementAPI movement;

    protected final Collection<Portal> portals;

    protected final Listener<Map> listener = this::onMapChange;
    protected final PortalJumper jumper;

    public Portal current;
    public Map target;

    protected int lastPortals;
    protected long shipTpWait = -1, mapChangeWait = -1;
    protected boolean done;

    public MapTraveler(PluginAPI api) {
        this.pet = api.requireAPI(PetAPI.class);
        this.hero = api.requireAPI(HeroAPI.class);
        this.star = api.requireAPI(StarSystemAPI.class);
        this.movement = api.requireAPI(MovementAPI.class);
        this.jumper = new PortalJumper(movement);

        this.star.addMapChangeListener(listener);
        this.portals = api.requireAPI(EntitiesAPI.class).getPortals();
    }

    public void setTarget(Map target) {
        shipTpWait = mapChangeWait = -1;
        this.target = target;
        this.done = false;
    }

    public boolean isDone() {
        return done;
    }

    public void tick() {
        if (star.getCurrentMap() == target) {
            done = true;
            return;
        }

        if (hero.getLocationInfo().getLast().distanceTo(hero) > 5000)
            shipTpWait = System.currentTimeMillis() + 2000;

        if ((shipTpWait == -1) != (mapChangeWait == -1)) {
            if (System.currentTimeMillis() < Math.max(shipTpWait, mapChangeWait)) return;
        }
        if (current == null || !current.isValid() || lastPortals != portals.size()) {
            current = star.findNext(target);
            lastPortals = portals.size();
            jumper.reset();
        }

        if (current == null) {
            if (System.currentTimeMillis() - mapChangeWait > 3000)
                done = true; // No port found after 3 secs, just go back.
            return;
        }
        shipTpWait = mapChangeWait = -1;

        if (current.getLocationInfo().distanceTo(hero) > 1500) // Portal very close, no need to disable pet
            pet.setEnabled(false);
        hero.setRunMode();

        if (!moveToCurrent()) return;
        jumper.jump(current);
    }

    protected boolean moveToCurrent() {
        double leniency = Math.min(200 + movement.getClosestDistance(current), 600);
        if (current.isValid() && movement.getDestination() != null &&
                movement.getDestination().distanceTo(current) > leniency) {

            movement.moveTo(Location.of(current, Math.random() * Math.PI * 2, Math.random() * 200));
            return false;
        }
        return hero.getLocationInfo().distanceTo(current) <= leniency && !movement.isMoving();
    }

    protected void onMapChange(Map map) {
        mapChangeWait = System.currentTimeMillis() + 2000;
        lastPortals = -1;
    }

}
