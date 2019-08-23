package com.github.manolo8.darkbot.extensions.util;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

@JsonAdapter(ByteArray.ByteArrayAdapter.class)
public class ByteArray {
    private final byte[] data;
    private int hash;

    public ByteArray(String data) {
        this(Base64.getDecoder().decode(data));
    }

    public ByteArray(byte[] data) {
        this.data = data;
        this.hash = Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteArray byteArray = (ByteArray) o;
        return hash == byteArray.hash &&
                Arrays.equals(data, byteArray.data);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static class ByteArrayAdapter extends TypeAdapter<ByteArray> {
        @Override
        public void write(JsonWriter writer, ByteArray value) throws IOException {
            writer.nullValue();
        }

        @Override
        public ByteArray read(JsonReader in) throws IOException {
            return new ByteArray(in.nextString());
        }
    }
}
