package eu.darkbot.api.plugin;

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
