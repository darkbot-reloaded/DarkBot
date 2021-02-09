package eu.darkbot.api.entities.other;

/**
 * Represents formations in-game.
 */
public enum Formation {

    /**
     * 2D formations.
     */
    STANDARD,
    TURTLE(0.1),
    ARROW,
    LANCE,
    STAR,
    PINCER,
    DOUBLE_ARROW(-0.2),
    DIAMOND(-0.3, 0, 0.01),
    CHEVRON(-0.2, 0),
    MOTH(0.2, 0, -0.05),
    CRAB,
    HEART(0.2, 0.2),
    BARRAGE,
    BAT,

    /**
     * 3D formations.
     */
    RING(0.85),
    DRILL(-0.25),
    VETERAN(-0.2, -0.2),
    DOME(0, 0.3, 0.005),
    WHEEL(0, 0, -0.05),
    X(0.08, 0),
    WAVY,
    MOSQUITO;

    private final double hp, sh, sps;

    Formation() {
        this(0);
    }

    Formation(double sh) {
        this(0, sh, 0);
    }

    Formation(double hp, double sh) {
        this(hp, sh, 0);
    }

    Formation(double hp, double sh, double sps) {
        this.hp = hp;
        this.sh = sh;
        this.sps = sps;
    }

    public static Formation of(int formationId) {
        if (formationId < 0 || formationId >= values().length) return STANDARD;
        return values()[formationId];
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
