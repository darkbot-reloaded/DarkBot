package eu.darkbot.api.entities.other;

public enum PortalType {

    STANDARD(1),
    TUTORIAL(55),
    GROUP_GATE(34),
    PIRATE(51), PIRATE_BROKEN(52),

    //GGs
    ALPHA(2), BETA(3), GAMMA(4), DELTA(5),
    EPSILON(53), ZETA(54), KAPPA(70), LAMBDA(71),
    KRONOS(72), HADES(74), KUIPER(82), QUARANTINE(84),

    //Event
    BIRTHDAY(11), SIXTH_BIRTHDAY(15), HIGHSCORE(14),
    INVASION_1(41), INVASION_2(42), INVASION_3(43),
    SOCCER_LEFT(61), SOCCER_RIGHT(62),
    PET_ATTACK_LOW(79), PET_ATTACK_HIGH(79),
    PAYLOAD(85), DOMINATION(75),
    GOP(24), GOP_EASY(235),  //Gauntlet of Plutus
    TOT(81), ZETA_FROST(83), //Tunnel of Terror

    //?
    INVISIBLE(18), //4-5 center portal?
    BREACH(22),
    LOW_LEFT(77),
    HIGH_RIGHT(78);

    private final int id;

    PortalType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
