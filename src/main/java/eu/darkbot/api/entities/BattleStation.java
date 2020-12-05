package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Attackable;
import eu.darkbot.api.objects.Obstacle;

public interface BattleStation extends Zone, Obstacle, Attackable {

    int getHullId();

    /**
     * BattleStation's module
     */
    interface Module extends Zone, Obstacle, Attackable /*should be attacker but...*/ {

        String getModuleId();
    }
}
