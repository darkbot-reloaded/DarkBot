package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Attackable;
import eu.darkbot.api.objects.Obstacle;

import java.util.Collection;

public interface BattleStation extends Zone, Obstacle, Attackable {

    int getHullId();

    Collection<Module> getModules();

    /**
     * BattleStation's module
     */
    interface Module extends Zone, Obstacle, Attackable /*should be attacker but...*/ {

        String getModuleId();
    }
}
