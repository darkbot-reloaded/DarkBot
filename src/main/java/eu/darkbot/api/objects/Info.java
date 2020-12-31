package eu.darkbot.api.objects;

import eu.darkbot.api.managers.HeroAPI;

public interface Info {

    /**
     * Returns true if ship is considered as enemy for {@link HeroAPI}.
     * <p>
     * Is <b>not</b> in ally clan, group?
     * Is from other faction or enemy clan.
     */
    boolean isEnemy();

    /**
     * @return {@link Fraction}
     */
    Fraction getFraction();

    /**
     * @return ship username.
     */
    String getUsername();

    /**
     *
     * @return title id
     */
    String getTitleId();

    /**
     * @return ship's clan tag
     */
    String getClanTag();

    /**
     * @return clan id
     */
    int getClanId();

    /**
     * @return {@link Diplomacy}
     */
    Diplomacy getClanDiplomacy();

    /**
     * Probably id of rank icon.
     */
    int getRankIconId();

    /**
     * Probably id of gate circles icon, above rank
     */
    int getGalaxyRankIconId();

    int getReputation();

    /**
     * Represents in-game fractions.
     */
    enum Fraction {
        NONE,
        MMO,
        EIC,
        VRU,
        SATURN
    }

    /**
     * Represents in-game clans diplomacy types.
     */
    enum Diplomacy {
        NONE,
        ALLIED,
        NOT_ATTACK_PACT,
        WAR
    }
}
