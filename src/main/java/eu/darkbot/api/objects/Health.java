package eu.darkbot.api.objects;

public interface Health {

    int getHp();
    int getHull();
    int getShield();

    int getMaxHp();
    int getMaxHull();
    int getMaxShield();

    /**
     * @return percentage as double(0 to 1)
     */
    default double hpPercent() {
        return getMaxHp() == 0 ? 1 :
                ((double) getHp() / (double) getMaxHp());
    }

    default double hullPercent() {
        return getMaxHull() == 0 ? 1 :
                ((double) getHull() / (double) getMaxHull());
    }
    default double shieldPercent() {
        return getMaxShield() == 0 ? 1 :
                ((double) getShield() / (double) getMaxShield());
    }

    /**
     * @param time in milliseconds
     * @return true if was decreased in given time
     */
    boolean hpDecreasedIn(int time);
    boolean hullDecreasedIn(int time);
    boolean shieldDecreasedIn(int time);

    /**
     * @param time in milliseconds
     * @return true if was increased in given time
     */
    boolean hpIncreasedIn(int time);
    boolean hullIncreasedIn(int time);
    boolean shieldIncreasedIn(int time);
}
