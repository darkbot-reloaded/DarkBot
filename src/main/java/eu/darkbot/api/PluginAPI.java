package eu.darkbot.api;

import eu.darkbot.api.plugins.PluginInfo;
import eu.darkbot.api.plugins.FeatureInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface PluginAPI extends API {

    /**
     * @param api to get
     * @param <T> type of api which extends {@link API}
     * @return instance of given {@link API} type
     */
    @Nullable <T extends API> T getAPI(@NotNull Class<T> api);

    /**
     * This method makes you sure that returned instance of given type is not null,
     * otherwise exception will be thrown.
     *
     * @param api to get
     * @param <T> type of api which extends {@link API}
     * @return instance of given {@link API} type
     * @throws UnsupportedOperationException if given api isn't supported
     */
    @NotNull <T extends API> T requireAPI(@NotNull Class<T> api) throws UnsupportedOperationException;

    /**
     * @return {@link PluginInfo} of your plugin.
     */
    PluginInfo getPluginInfo();

    /**
     * @return {@link Collection} of all available and loaded plugins.
     */
    Collection<PluginInfo> getPluginsInfo();

    /**
     * @param featureId to get instance of
     * @return instance of given feature
     * @throws ClassNotFoundException when given feature wasn't found
     */
    @NotNull <T> T getFeature(String featureId) throws ClassNotFoundException;
    @NotNull <T> T getFeature(Class<T> feature) throws ClassNotFoundException;

    /**
     * @param featureId of the feature
     * @return {@link FeatureInfo} of given feature.
     * @throws ClassNotFoundException when given feature wasn't found
     */
    @NotNull <T> FeatureInfo<T> getFeatureInfo(String featureId) throws ClassNotFoundException;
    @NotNull <T> FeatureInfo<T> getFeatureInfo(Class<T> feature) throws ClassNotFoundException;
}
