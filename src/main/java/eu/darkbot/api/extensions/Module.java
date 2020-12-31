package eu.darkbot.api.extensions;

public interface Module {

    void onTickModule();

    default void onStoppedModule() {
    }

    default boolean canRefresh() {
        return true;
    }

    default String getStatus() {
        return null;
    }

    default String getStoppedStatus() {
        return null;
    }
}
