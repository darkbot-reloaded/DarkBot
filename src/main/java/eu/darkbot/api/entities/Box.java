package eu.darkbot.api.entities;

public interface Box extends Entity {

    String getBoxType();
    boolean isCollected();
    void setCollected();
}
