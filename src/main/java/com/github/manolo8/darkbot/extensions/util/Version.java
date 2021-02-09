package com.github.manolo8.darkbot.extensions.util;

import com.google.gson.annotations.JsonAdapter;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonAdapter(eu.darkbot.utils.Version.VersionAdapter.class)
public class Version implements Comparable<Version> {
    private static final Pattern VERSION = Pattern.compile("" +
            "([^0-9]*[0-9]+)" + // Major
            "(\\.([0-9]+))?" + // Minor
            "(\\.([0-9]+))?" + // Patch
            "(\\.([0-9]+))?" + // Revision
            "( ?beta ?([0-9]+)?)?" + // Beta
            "( ?alpha ?([0-9]+)?)?"); // Alpha

    private final String version;
    private final int major, minor, patch, revision, beta, alpha;

    public Version(String version) {
        this.version = version;
        Matcher matcher = VERSION.matcher(version);
        if (!matcher.matches()) throw new IllegalArgumentException("Couldn't parse version " + version);
        major = Integer.parseInt(matcher.group(1));
        minor = getInt(matcher, 2);
        patch = getInt(matcher, 4);
        revision = getInt(matcher, 6);

        int tmpBeta = getInt(matcher, 8);
        int tmpAlpha = getInt(matcher, 10);
        beta = tmpBeta == -1 && tmpAlpha == -1 ? Integer.MAX_VALUE : tmpBeta;
        alpha = tmpAlpha == -1 ? Integer.MAX_VALUE : tmpAlpha;
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

}
