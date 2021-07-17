package eu.darkbot.api.game.galaxy;

public interface GateInfo {

    boolean isFinished();

    String getState();

    int getTotal();
    int getCurrent();
    int getId();
    int getPrepared();
    int getTotalWave();
    int getCurrentWave();
    int getLivesLeft();
    int getLifePrice();
}
