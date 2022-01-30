package com.github.manolo8.darkbot.config.utils;

import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import eu.darkbot.api.config.types.Condition;

import java.io.IOException;

public class ConditionTypeAdapterFactory implements TypeAdapterFactory {

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (Condition.class.isAssignableFrom(type.getRawType()))
            //noinspection unchecked
            return (TypeAdapter<T>) ConditionTypeAdapter.INSTANCE;
        return gson.getDelegateAdapter(this, type);
    }

    private static class ConditionTypeAdapter extends TypeAdapter<Condition> {
        private static final ConditionTypeAdapter INSTANCE = new ConditionTypeAdapter();

        @Override
        public void write(JsonWriter writer, Condition condition) throws IOException {
            if (condition == null) writer.nullValue();
            else writer.value(condition.toString());
        }

        @Override
        public Condition read(JsonReader in) throws IOException {
            try {
                return ValueParser.parseCondition(in.nextString());
            } catch (SyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
