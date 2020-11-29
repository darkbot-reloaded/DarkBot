package eu.darkbot.logic.modules;

import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.utils.Map;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.MovementAPI;
import eu.darkbot.api.managers.PetAPI;
import eu.darkbot.api.managers.StarAPI;
import eu.darkbot.api.objects.Location;
import eu.darkbot.api.utils.Listener;
import eu.darkbot.logic.utils.PortalJumper;

import java.util.Collection;

public class MapModule extends TemporalModule {

    private Traveler traveler;

    public MapModule(PluginAPI api, Map target) {
        super(api);
        if (traveler == null)
            this.traveler = new Traveler(api);

        this.traveler.setTarget(target);
    }

    public void setTarget(Map target) {
        this.traveler.setTarget(target);
    }

    @Override
    public String getStatus() {
        return  traveler.current != null ?
                I18n.get("module.map_travel.status.has_next", traveler.target.getName(),
                        traveler.current.getTargetMap().map(Map::getName).orElse("unknown?")) :
                I18n.get("module.map_travel.status.no_next", traveler.target.getName());
    }

    @Override
    public void onTickModule() {
        if (!traveler.isDone()) traveler.tick();
        if (traveler.isDone()) goBack();
    }

    private static class Traveler {
        private final PetAPI pet;
        private final HeroAPI hero;
        private final StarAPI star;
        private final MovementAPI movement;

        private final Collection<Portal> portals;

        private final Listener<Map> listener = this::onMapChange;
        private final PortalJumper jumper;

        public Portal current;
        public Map target;
        private int lastPortals;
        private long shipTpWait = -1, mapChangeWait = -1;
        private boolean done;

        public Traveler(PluginAPI api) {
            this.pet = api.requireAPI(PetAPI.class);
            this.hero = api.requireAPI(HeroAPI.class);
            this.star = api.requireAPI(StarAPI.class);
            this.movement = api.requireAPI(MovementAPI.class);
            this.jumper = new PortalJumper(movement);

            this.star.addMapChangeListener(listener);
            this.portals = api.requireAPI(EntitiesAPI.class).getEntities(Portal.class);
        }

        public void setTarget(Map target) {
            shipTpWait = mapChangeWait = -1;
            this.target = target;
            this.done = false;
        }

        public boolean isDone() {
            return done;
        }

        private Location last;
        public void tick() {
            if (star.getCurrentMap() == target) {
                done = true;
                return;
            }

            if (last != null && hero.getLocationInfo().distanceTo(last) > 5000)
                shipTpWait = System.currentTimeMillis() + 2000;

            last = hero.getLocationInfo().copy();

            if ((shipTpWait == -1) != (mapChangeWait == -1)) {
                if (System.currentTimeMillis() < Math.max(shipTpWait, mapChangeWait)) return;
            }
            if (current == null || current.isRemoved() || lastPortals != portals.size()) {
                current = star.findNext(target);
                lastPortals = portals.size();
                jumper.reset();
            }

            if (current == null) {
                if (System.currentTimeMillis() - mapChangeWait > 3000) done = true; // No port found after 3 secs, just go back.
                return;
            }
            shipTpWait = mapChangeWait = -1;

            if (current.getLocationInfo().distanceTo(hero) > 1500) // Portal very close, no need to disable pet
                pet.setEnabled(false);
            hero.setRunMode();

            if (!moveToCurrent()) return;
            jumper.jump(current);
        }

        private boolean moveToCurrent() {
            double leniency = Math.min(200 + movement.getClosestDistance(current), 600);
            if (current.isValid() && movement.getDestination() != null &&
                    movement.getDestination().distanceTo(current) > leniency) {

                movement.moveTo(Location.of(current, Math.random() * Math.PI * 2, Math.random() * 200));
                return false;
            }
            return hero.getLocationInfo().distanceTo(current) <= leniency && !movement.isMoving();
        }

        private void onMapChange(Map map) {
            mapChangeWait = System.currentTimeMillis() + 2000;
            lastPortals = -1;
        }

    }
}
