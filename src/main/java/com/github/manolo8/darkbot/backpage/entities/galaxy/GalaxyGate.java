package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Map;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @deprecated Use {@link eu.darkbot.api.game.galaxy.GalaxyGate} instead
 */
@Getter
@Deprecated
public enum GalaxyGate {
    ALPHA  ("alpha",    1,  "α"),
    BETA   ("beta",     2,  "β"),
    GAMMA  ("gamma",    3,  "γ"),
    DELTA  ("delta",    4,  "δ"),
    EPSILON("epsilon",  5,  "ε"),
    ZETA   ("zeta",     6,  "ζ"),
    KAPPA  ("kappa",    7,  "κ"),
    LAMBDA ("lambda",   8,  "λ"),
    // Just to get infos about Kronos
    KRONOS ("kronos",   12, "Kronos"),
    HADES  ("hades",    13, "Hades"),
    KUIPER ("streuner", 19, "ς");

    private final String name;
    private final int id;
    private final Set<Map> maps;

    GalaxyGate(String name, int id, String mapSymbol) {
        this.name = name;
        this.id   = id;
        this.maps = StarManager.getAllMaps().stream()
                .filter(map -> map.gg && map.name.contains(mapSymbol))
                .collect(Collectors.toSet());
    }

    public boolean isInGate(HeroManager hero) {
        return getMaps().stream().anyMatch(map -> map.id == hero.map.id);
    }

    boolean match(Object o) {
        if (o instanceof Integer) return o.equals(getId());
        if (o instanceof String) return o.equals(getName());
        return false;
    }
}