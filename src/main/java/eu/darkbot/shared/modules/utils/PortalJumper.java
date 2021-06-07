package eu.darkbot.shared.modules.utils;

import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.managers.MovementAPI;

public class PortalJumper {

    protected final MovementAPI movement;

    protected Portal last;
    protected long nextMoveClick;

    public PortalJumper(MovementAPI movement) {
        this.movement = movement;
    }

    public void reset() {
        this.last = null;
    }

    public void jump(Portal target) {
        if (movement.moveToPortal(target)) movement.jumpPortal(target);

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
