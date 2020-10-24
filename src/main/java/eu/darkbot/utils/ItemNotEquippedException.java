package eu.darkbot.utils;

public class ItemNotEquippedException extends Exception {
        public ItemNotEquippedException(ThrowableItem item) {
            super(item.getMessage());
        }
    }