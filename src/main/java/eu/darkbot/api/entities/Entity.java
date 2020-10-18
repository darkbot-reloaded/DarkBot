package eu.darkbot.api.entities;

public interface Entity {
    int getId();

    boolean isRemoved();
    void setRemoved();
}
