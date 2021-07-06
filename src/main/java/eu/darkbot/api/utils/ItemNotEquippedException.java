package eu.darkbot.api.utils;

public class ItemNotEquippedException extends Exception {

    public ItemNotEquippedException(EquippableItem item) {
        super("Item " + item.getName() + " was not equipped");
    }

    public ItemNotEquippedException(EquippableItem item, String fallback) {
        super("Item " + (item != null ? item.getName() : fallback) + " was not equipped");
    }
}