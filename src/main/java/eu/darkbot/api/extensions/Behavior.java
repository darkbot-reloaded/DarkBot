package eu.darkbot.api.extensions;

public interface Behavior {

    void onTickBehavior();

    default void onStoppedBehavior() {}
}
