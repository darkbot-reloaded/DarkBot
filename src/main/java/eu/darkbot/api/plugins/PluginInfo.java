package eu.darkbot.api.plugins;

import eu.darkbot.api.utils.Version;

import java.net.URL;
import java.util.Collection;
import java.util.Optional;

public interface PluginInfo {

    String getName();
    String getAuthor();

    Version getVersion();

    Version getMinimumVersion();
    Version getSupportedVersion();

    <T> Optional<T> getFeature(String featureId);
    <T> Optional<T> getFeature(Class<T> feature);

    Collection<FeatureInfo<?>> getFeatures();

    URL getUpdateURL();
    URL getDonationURL();
    URL getDownloadURL();
}
