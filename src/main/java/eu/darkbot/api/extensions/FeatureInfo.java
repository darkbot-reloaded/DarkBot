package eu.darkbot.api.extensions;

import org.jetbrains.annotations.Nullable;

/**
 * The information about a feature provided by a plugin
 * @param <T> The type of the actual feature
 */
public interface FeatureInfo<T> {

    /**
     * @return If this feature is currently enabled by the user, defaults to {@link Feature#enabledByDefault()}
     */
    boolean isEnabled();

    /**
     * Set the feature to be enabled or disabled, this is something the user
     * should do via config, be extremely cautious when using this as it can confuse users.
     *
     * @param enabled If it should be set to enabled
     */
    void setEnabled(boolean enabled);

    /**
     * @return The name of this feature as defined in {@link Feature#name()}
     */
    String getName();

    /**
     * @return The name of this feature as defined in {@link Feature#description()}
     */
    String getDescription();

    /**
     * @return the current instance of the feature, if any
     */
    @Nullable T getInstance();

    /**
     * @return The class of the feature
     */
    Class<T> getFeatureClass();

    /**
     * @return The plugin this feature belongs to, or null for native feature
     */
    @Nullable PluginInfo getPluginInfo();

    /**
     * @return If the feature both is enabled, and didn't
     *         encounter any issues that prevent it from loading.
     */
    boolean canLoad();
}
