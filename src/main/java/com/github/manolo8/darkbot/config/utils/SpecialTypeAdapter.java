package com.github.manolo8.darkbot.config.utils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class SpecialTypeAdapter implements TypeAdapterFactory {
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (Ignorable.class.isAssignableFrom(type.getRawType()))
            return new IgnorableTypeAdapter<>(gson.getDelegateAdapter(this, type));

        return gson.getDelegateAdapter(this, type);
    }

    private static class IgnorableTypeAdapter<T> extends TypeAdapter<T> {
        private final TypeAdapter<T> delegate;

        public IgnorableTypeAdapter(TypeAdapter<T> delegate) {
            this.delegate = delegate;
        }

        public void write(JsonWriter out, T value) throws IOException {
            if (value instanceof Ignorable && ((Ignorable) value).ignore()) {
                // For named fields we are required to write a null, which will remove the field name.
                // For objects inside arrays we can simply not write anything and the builder will handle it.
                if (((Ignorable) value).writeAsNull()) delegate.write(out, null);
            } else delegate.write(out, value);
        }

        public T read(JsonReader in) throws IOException {
            return delegate.read(in);
        }
    }

}