package com.github.manolo8.darkbot.extensions.util;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonAdapter(Version.VersionAdapter.class)
public class Version implements Comparable<Version> {
    private static final Pattern VERSION = Pattern.compile("" +
            "([^0-9]*[0-9]+)" + // Major
            "(\\.([0-9]+))?" + // Minor
            "(\\.([0-9]+))?" + // Patch
            "(\\.([0-9]+))?" + // Revision
            "( ?beta ?([0-9]+)?)?" + // Beta
            "( ?alpha ?([0-9]+)?)?"); // Alpha

    private final String version;
    private final int major;
    private final int minor;
    private final int patch;
    private final int revision;
    private int beta;
    private int alpha;

    public Version(String version) {
        this.version = version;
        Matcher matcher = VERSION.matcher(version);
        if (!matcher.matches()) throw new IllegalArgumentException("Couldn't parse version " + version);
        major = Integer.parseInt(matcher.group(1));
        minor = getInt(matcher, 2);
        patch = getInt(matcher, 4);
        revision = getInt(matcher, 6);
        beta = getInt(matcher, 8);
        alpha = getInt(matcher, 10);
        if (beta == -1 && alpha == -1) beta = Integer.MAX_VALUE;
        if (alpha == -1) alpha = Integer.MAX_VALUE;
    }

    public Version(Version version) {
        this.version = version.version;
        major = version.major;
        minor = version.minor;
        patch = version.patch;
        revision = version.revision;
        beta = version.beta;
        alpha = version.alpha;
    }

    private int getInt(Matcher m, int find) {
        if (m.group(find) == null) return -1;
        String num = m.group(find + 1);
        return num == null ? 0 : Integer.parseInt(m.group(find + 1));
    }

    @Override
    public int compareTo(Version o) {
        if (major != o.major) return Integer.compare(major, o.major);
        if (minor != o.minor) return Integer.compare(minor, o.minor);
        if (patch != o.patch) return Integer.compare(patch, o.patch);
        if (revision != o.revision) return Integer.compare(revision, o.revision);
        if (beta != o.beta) return Integer.compare(beta, o.beta);
        if (alpha != o.alpha) return Integer.compare(alpha, o.alpha);
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version v = (Version) o;
        return major == v.major &&
                minor == v.minor &&
                patch == v.patch &&
                revision == v.revision &&
                beta == v.beta &&
                alpha == v.alpha;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, revision, beta, alpha);
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
