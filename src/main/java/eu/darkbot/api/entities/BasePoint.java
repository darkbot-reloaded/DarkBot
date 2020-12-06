package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Attackable;

public interface BasePoint extends Entity {

    interface Hangar extends BasePoint {
    }

    interface Repair extends BasePoint {
    }

    interface Turret extends BasePoint {
    }

    interface Station extends BasePoint {
    }

    interface Refinery extends BasePoint {
    }

    interface QuestGiver extends BasePoint {
    }

    interface Headquarter extends BasePoint {
    }

    /**
     * Relay static entity in LoW gate.
     */
    interface Relay extends Attackable, BasePoint {
    }
}
