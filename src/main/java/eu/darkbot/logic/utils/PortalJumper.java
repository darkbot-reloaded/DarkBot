package eu.darkbot.logic.utils;

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
            movement.moveRandom(target, 200);
            nextMoveClick = System.currentTimeMillis() + 10000;
        }
    }

}
