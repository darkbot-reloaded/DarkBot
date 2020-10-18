package eu.darkbot.api.objects;

public interface Health {

    int getHp();
    int getMaxHp();

    int getHull();
    int getMaxHull();

    int getShield();
    int getMaxShield();

    /**
     * @return percentage as double(0 to 1)
     */
    double hpPercent();
    double hullPercent();
    double shieldPercent();

    /**
     * @param time in milliseconds
     * @return true if was decreased in given time
     */
    boolean hpDecreasedIn(int time);

    /**
     * @param time in milliseconds
     * @return true if was increased in given time
     */
    boolean hpIncreasedIn(int time);

    boolean hullDecreasedIn(int time);
    boolean hullIncreasedIn(int time);

    boolean shieldDecreasedIn(int time);
    boolean shieldIncreasedIn(int time);
}
