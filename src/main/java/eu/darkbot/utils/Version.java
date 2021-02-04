package eu.darkbot.utils;

public class Version extends com.github.manolo8.darkbot.extensions.util.Version {

    public Version(String version) {
        super(version);
    }

    public static Version of(String version) {
        return new Version(version);
    }
}
