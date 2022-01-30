package com.github.manolo8.darkbot.config.utils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import eu.darkbot.api.config.types.PlayerTag;

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
            return com.github.manolo8.darkbot.config.PlayerTag.getTag(in.nextString());
        }
    }

}
