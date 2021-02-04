package eu.darkbot.api.entities;

public interface Station extends Entity {

    interface Hangar extends Station {
    }

    interface Repair extends Station {
    }

    interface Turret extends Station {
    }

    interface Refinery extends Station {
    }

    interface QuestGiver extends Station {
    }

    interface Headquarter extends Station {
    }
}
