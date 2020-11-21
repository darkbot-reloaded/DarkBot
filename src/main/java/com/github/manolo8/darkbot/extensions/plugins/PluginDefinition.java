package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.extensions.util.Version;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class PluginDefinition {

    public PluginDefinition(PluginDefinition pluginDefinition) {
        name = pluginDefinition.name;
        author = pluginDefinition.author;
        version = new Version(pluginDefinition.version);
        minVersion = new Version(pluginDefinition.minVersion);
        supportedVersion = new Version(pluginDefinition.supportedVersion);
        features = pluginDefinition.features.clone();
        try {
            URI donate = pluginDefinition.donation;
            donation = donate == null ? null : new URI(donate.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            URL down = pluginDefinition.download;
            download = down == null ? null : new URL(down.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            URL up = pluginDefinition.update;
            update = up == null ? null : new URL(up.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // Name of the plugin
    public String name;
    // Author of the plugin
    public String author;
    // Version of the plugin
    public Version version;
    // Minimum version this plugin requires to run
    public Version minVersion;
    // Latest tested version this plugin runs on
    public Version supportedVersion;
    // Array of fully qualified class names of modules
    public String[] features;
    // Donation url
    public URI donation;
    // URL to download the plugin from
    public URL download;
    // URI to get an updated plugin definition (a plugin.json)
    public URL update;
}
