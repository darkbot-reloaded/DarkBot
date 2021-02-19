package eu.darkbot.api.entities.other;

/**
 * Represents formations in-game.
 */
public enum Formation {

    /**
     * 2D formations.
     */
    STANDARD("default"),
    TURTLE("f-01-tu" ,0.1),
    ARROW("f-02-ar"),
    LANCE("f-03-la"),
    STAR("f-04-st"),
    PINCER("f-05-pi"),
    DOUBLE_ARROW("f-06-da", -0.2),
    DIAMOND("f-07-di", -0.3, 0, 0.01),
    CHEVRON("f-08-ch", -0.2, 0),
    MOTH("f-09-mo", 0.2, 0, -0.05),
    CRAB("f-10-cr"),
    HEART("f-11-he", 0.2, 0.2),
    BARRAGE("f-12-ba"),
    BAT("f-13-bt"),

    /**
     * 3D formations.
     */
    RING("f-3d-rg", 0.85),
    DRILL("f-3d-dr", -0.25),
    VETERAN("f-3d-vt", -0.2, -0.2),
    DOME("f-3d-dm", 0, 0.3, 0.005),
    WHEEL("f-3d-wl", 0, 0, -0.05),
    X("f-3d-x", 0.08, 0),
    WAVY("f-3d-wv"),
    MOSQUITO(null),

    /**
     * Not sure what is it but exists in-game source with ID 42.
     */
    X2(null);

    private final String id;
    private final double hp, sh, sps;

    Formation(String id) {
        this(id, 0);
    }

    Formation(String id, double sh) {
        this(id, 0, sh, 0);
    }

    Formation(String id, double hp, double sh) {
        this(id, hp, sh, 0);
    }

    Formation(String id, double hp, double sh, double sps) {
        this.id = id;
        this.hp = hp;
        this.sh = sh;
        this.sps = sps;
    }

    public static Formation of(int formationId) {
        if (formationId == 42) return X2;
        if (formationId < 0 || formationId >= values().length) return STANDARD;
        return values()[formationId];
    }

    public static Formation of(String id) {
        for (Formation formation : values())
            if (formation.id != null && id.endsWith(formation.id))
                return formation;

        return null;
    }

    public double getShieldMultiplier() {
        return sh;
    }

    public double getHealthMultiplier() {
        return hp;
    }

    /**
     * @return shield regen % amount per second.
     */
    public double getShieldRegen() {
        return sps;
    }
}
