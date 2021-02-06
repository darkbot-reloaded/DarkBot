package eu.darkbot.api.utils;

public interface PathFinder {

    boolean isOutOfMap(double x, double y);

    boolean canMove(double x, double y);

}
