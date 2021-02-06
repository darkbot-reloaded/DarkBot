package eu.darkbot.api.extensions;

public interface FeatureInfo<T> {

    boolean isEnabled();
    void setEnabled(boolean enabled);

    String getName();
    String getDescription();

    T getInstance();

    Class<T> getFeatureClass();
    PluginInfo getPluginInfo();

    boolean canLoad();
}
