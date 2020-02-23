package com.github.manolo8.darkbot.config;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

@JsonAdapter(PlayerTag.TagAdapter.class)
public class PlayerTag {
    public String name;
    public Color color;

    public PlayerTag(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerTag playerTag = (PlayerTag) o;
        return Objects.equals(name, playerTag.name) &&
                Objects.equals(color, playerTag.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color);
    }

    public static class TagAdapter extends TypeAdapter<PlayerTag> {
        @Override
        public void write(JsonWriter writer, PlayerTag tag) throws IOException {
            writer.value(String.format("#%02x%02x%02x,%s",
                    tag.color.getRed(), tag.color.getGreen(), tag.color.getBlue(), tag.name));
        }

        @Override
        public PlayerTag read(JsonReader in) throws IOException {
            String[] str = in.nextString().split(",", 2);
            return new PlayerTag(str[1], Color.decode(str[0]));
        }
    }

}
