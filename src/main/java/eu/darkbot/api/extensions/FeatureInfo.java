package eu.darkbot.api.extensions;

public interface FeatureInfo<T> {

    boolean isEnabled();
    void setEnabled(boolean enabled);

    String getName();
    String getDescription();

    T getInstance();

    Class<T> getFeatureClass();
    PluginInfo getPluginInfo();

    // TODO: 31.12.2020 issues
    //boolean hasIssues();
}
