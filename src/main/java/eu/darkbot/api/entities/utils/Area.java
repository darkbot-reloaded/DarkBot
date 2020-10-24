package eu.darkbot.api.entities.utils;

public interface Area {
    AreaType getAreaType();

    enum AreaType {
        RECTANGLE,
        CIRCULAR,
        TRIANGLE,
        UNKNOWN
    }
}