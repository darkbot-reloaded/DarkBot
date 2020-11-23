package eu.darkbot.utils;

public class WrongThreadException extends RuntimeException {
    public WrongThreadException(String message) {
        super(message);
    }
}
