package com.github.manolo8.darkbot.config.utils;

import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class PlayerTagTypeAdapterFactory implements TypeAdapterFactory {

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (PlayerTag.class.isAssignableFrom(type.getRawType()))
            //noinspection unchecked
            return (TypeAdapter<T>) TagAdapter.INSTANCE;
        return gson.getDelegateAdapter(this, type);
    }

    public static class TagAdapter extends TypeAdapter<PlayerTag> {
        private static final TagAdapter INSTANCE = new TagAdapter();

        @Override
        public void write(JsonWriter writer, PlayerTag tag) throws IOException {
            if (tag == null) writer.nullValue();
            else writer.value(tag.toString());
        }

        @Override
        public PlayerTag read(JsonReader in) throws IOException {
            return PlayerTag.getTag(in.nextString());
        }
    }

}
