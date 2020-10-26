package eu.darkbot.utils;

public class ItemNotEquippedException extends Exception {
    public ItemNotEquippedException(EquippableItem item) {
        super("Item " + item.getName() + " was not equipped");
    }
}