package eu.darkbot.api.game.entities;

/**
 * In-game station area, includes home bases like x-1 & x-8, but also things like 5-2 or quest givers in x-4 & x-5 maps.
 *
 * @see Station.Headquarter
 * @see Station.Hangar
 * @see Station.Repair
 * @see Station.Turret
 * @see Station.Refinery
 * @see Station.QuestGiver
 * @see Station.HomeBase
 */
public interface Station extends Entity {

    /**
     * Center headquarters, in x-1 & x-8 maps
     */
    interface Headquarter extends Station {
    }

    /**
     * Hangar area to change equipment, in x-1 & x-8 maps
     */
    interface Hangar extends Station {
    }

    /**
     * Repair spot of the ship after reviving, in x-1 & x-8 maps
     */
    interface Repair extends Station {
    }

    /**
     * Small turrets surrounding home bases, in x-1 & x-8 maps
     */
    interface Turret extends Station {
    }

    /**
     * Refinery area where ores can be sold, in x-1, x-8 & 5-2 maps
     */
    interface Refinery extends Station {
    }

    /**
     * Quest giver areas where you can get quests, in x-1, x-4, x-5, x-8 & BL-x maps.
     */
    interface QuestGiver extends Station {
    }

    /**
     * Home base station that is standalone, this is an alternative Headquarter, in the 5-2 map
     */
    interface HomeBase extends Station {
    }

}
