package eu.darkbot.api.objects;

import eu.darkbot.api.managers.HeroAPI;

public interface EntityInfo {

    /**
     * Returns true if ship is considered as enemy for {@link HeroAPI}.
     * <p>
     * Is <b>not</b> in ally clan, group?
     * Is from other faction or enemy clan.
     */
    boolean isEnemy();

    /**
     * @return {@link Faction}
     */
    Faction getFaction();

    /**
     * @return ship username.
     */
    String getUsername();

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

    /**
     * Represents in-game fractions.
     */
    enum Faction {
        NONE,
        MMO,
        EIC,
        VRU,
        SATURN;

        public static Faction of(int factionId) {
            if (factionId >= values().length || factionId < 0) return NONE;
            return values()[factionId];
        }
    }

    /**
     * Represents in-game clans diplomacy types.
     */
    enum Diplomacy {
        NONE,
        ALLIED,
        NOT_ATTACK_PACT,
        WAR;

        public static Diplomacy of(int diplomacyId) {
            if (diplomacyId >= values().length || diplomacyId < 0) return NONE;
            return values()[diplomacyId];
        }
    }
}
