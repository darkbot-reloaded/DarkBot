package eu.darkbot.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

@JsonAdapter(Version.VersionAdapter.class)
public class Version extends com.github.manolo8.darkbot.extensions.util.Version {

    public Version(String version) {
        super(version);
    }

    public static Version of(String version) {
        return new Version(version);
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
