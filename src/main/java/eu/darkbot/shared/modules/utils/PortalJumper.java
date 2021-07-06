package eu.darkbot.shared.modules.utils;

import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.utils.GameMap;
import eu.darkbot.api.managers.GroupAPI;
import eu.darkbot.api.managers.MovementAPI;
import eu.darkbot.api.objects.Location;
import eu.darkbot.api.utils.Inject;

public class PortalJumper {

    protected final MovementAPI movement;
    protected final GroupAPI group;

    protected Portal last;
    protected long nextMoveClick;

    @Inject
    public PortalJumper(MovementAPI movement, GroupAPI group) {
        this.movement = movement;
        this.group = group;
    }

    public void reset() {
        this.last = null;
    }

    public void travelAndJump(Portal target) {
        if (travel(target)) jump(target);
    }

    public boolean travel(Portal target) {
        double leniency = Math.min(200 + movement.getClosestDistance(target), 600);
        if (target.getLocationInfo().isInitialized() && movement.getDestination().distanceTo(target) > leniency) {
            movement.moveTo(Location.of(target, Math.random() * Math.PI * 2, Math.random() * 200));
            return false;
        }
        return movement.getCurrentLocation().distanceTo(target) <= leniency && !movement.isMoving();
    }

    public void jump(Portal target) {
        // Low & hades, wait for group before trying to jump
        // This prevents the J key being written while typing out player names for invites
        int minGroupSize = target.getTargetMap().map(GameMap::getId)
                .map(id -> id == 200 ? 3 // LoW
                        : id == 203 ? 4  // Hades
                        : 0).orElse(0);

        if (minGroupSize > 0 && (!group.hasGroup() || group.getSize() < minGroupSize))
            return;

        movement.jumpPortal(target);

        if (target != last) {
            last = target;
            nextMoveClick = System.currentTimeMillis() + 5000;
        } else if (System.currentTimeMillis() > nextMoveClick && !target.isSelectable()) {
            /*TODO:
               This movement should go straight, not use pathfinding like it is right now
               This is supposed to fix client-server de-syncs, and this movement will not fix them.
               The intentional method would be drive.clickCenter(target), making a real click on that location*/
            movement.moveTo(target);
            nextMoveClick = System.currentTimeMillis() + 10000;
        }
    }

}
