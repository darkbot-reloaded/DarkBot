package eu.darkbot.api.plugin;

public interface Module {

    void onTickModule();

    void onStoppedModule();

    default boolean canRefresh() {
        return true;
    }

    default String status() {
        return null;
    }

    default String onStoppedStatus() {
        return null;
    }
}
