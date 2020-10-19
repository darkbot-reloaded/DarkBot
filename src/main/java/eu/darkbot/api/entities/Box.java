package eu.darkbot.api.entities;

public interface Box extends Entity {

    String getTypeName();
    boolean isCollected();
    void setCollected();
}
