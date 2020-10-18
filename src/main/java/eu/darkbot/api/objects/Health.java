package eu.darkbot.api.objects;

public interface Health {

    int getHp();
    int getMaxHp();
    int getHull();
    int getMaxHull();
    int getShield();
    int getMaxShield();

    double hpPercent();
    double hullPercent();
    double shieldPercent();

    boolean hpDecreasedIn(int time);
    boolean hpIncreasedIn(int time);

    boolean hullDecreasedIn(int time);
    boolean hullIncreasedIn(int time);

    boolean shieldDecreasedIn(int time);
    boolean shieldIncreasedIn(int time);
}
