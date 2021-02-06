package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.extensions.FeatureInfo;
import eu.darkbot.api.extensions.PluginInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface ExtensionsAPI extends API.Singleton {

    /**
     * @return {@link Collection} of all available and loaded plugins.
     */
    @NotNull Collection<? extends PluginInfo> getPluginInfos();

    /**
     * @param feature class to get instance of
     * @return instance of given feature
     */
    <T> Optional<T> getFeature(Class<T> feature);

    /**
     * @param feature class to get {@link FeatureInfo} of
     * @return {@link FeatureInfo} of given feature.
     */
    <T> FeatureInfo<T> getFeatureInfo(Class<T> feature);

}
