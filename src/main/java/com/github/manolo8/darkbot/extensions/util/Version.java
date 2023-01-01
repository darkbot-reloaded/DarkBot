package com.github.manolo8.darkbot.extensions.util;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonAdapter(Version.VersionAdapter.class)
@Getter
@EqualsAndHashCode
public class Version implements eu.darkbot.api.utils.Version {
    private static final Pattern VERSION = Pattern.compile("" +
            "([^0-9]*[0-9]+)" + // Major
            "(\\.([0-9]+))?" + // Minor
            "(\\.([0-9]+))?" + // Patch
            "(\\.([0-9]+))?" + // Revision
            "( ?b(eta)? ?([0-9]+)?)?" + // Beta
            "( ?a(lpha) ?([0-9]+)?)?"); // Alpha

    private final String version;
    private final int major, minor, patch, revision, beta, alpha;

    public Version(String version) {
        Matcher matcher = VERSION.matcher(version);
        if (!matcher.matches()) throw new IllegalArgumentException("Couldn't parse version " + version);
        this.major = Integer.parseInt(matcher.group(1));
        this.minor = getInt(matcher, 2);
        this.patch = getInt(matcher, 4);
        this.revision = getInt(matcher, 6);

        int beta = getInt(matcher, 9);
        int alpha = getInt(matcher, 12);
        this.beta = beta == -1 && alpha == -1 ? Integer.MAX_VALUE : beta;
        this.alpha = alpha == -1 ? Integer.MAX_VALUE : alpha;
        this.version = getVersionString();
    }

    public Version(int major, int minor, int patch) {
        this(major, minor, patch, -1, -1, -1);
    }

    public Version(int major, int minor, int patch, int revision, int beta, int alpha) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.revision = revision;
        this.beta = beta == -1 && alpha == -1 ? Integer.MAX_VALUE : beta;
        this.alpha = alpha == -1 ? Integer.MAX_VALUE : alpha;
        this.version = getVersionString();
    }

    private int getInt(Matcher m, int find) {
        if (m.group(find) == null) return -1;
        String num = m.group(find + 1);
        return num == null ? 0 : Integer.parseInt(m.group(find + 1));
    }

    public boolean isBeta() {
        return beta != Integer.MAX_VALUE;
    }

    public boolean isAlpha() {
        return alpha != Integer.MAX_VALUE;
    }

    private String getVersionString() {
        String version = String.valueOf(major);

        if (minor != -1) version += "." + minor;
        if (patch != -1) version += "." + patch;
        if (revision != -1) version += "." + revision;

        if (isBeta()) version += " b" + beta;
        if (isAlpha()) version += " a" + alpha;

        return version;

    }

    // Kept for backwards compat of plugins built against darkbot
    // Plugins built against api will use super.compareTo already
    public int compareTo(Version o) {
        return eu.darkbot.api.utils.Version.super.compareTo(o);
    }

    @Override
    public String toString() {
        return version;
    }

    public static class VersionAdapter extends TypeAdapter<Version> {
        @Override
        public void write(JsonWriter writer, Version value) throws IOException {
            writer.value(value.toString());
        }

        @Override
        public Version read(JsonReader in) throws IOException {
            return new Version(in.nextString());
        }
    }
}
