package com.github.manolo8.darkbot.config.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.awt.*;
import java.lang.reflect.Type;

public class FontAdapter implements JsonSerializer<Font>, JsonDeserializer<Font> {

        public Font deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                String[] parts = json.getAsString().split(":", 3);
                if (parts.length != 2 && parts.length != 3)
                    throw new JsonParseException("Invalid font definition in config");

                String name = parts[1];
                int size = Integer.parseInt(parts[0]);
                int type = parts.length == 2 ? Font.PLAIN : Integer.parseInt(parts[2]);
                return new Font(name, type, parts.length == 2 && name.equals("Consolas") && size == 12 ? 13 : size);
            }

            // Parse pre-adapter format, stored as json object
            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                return new Font(obj.getAsJsonPrimitive("name").getAsString(),
                        Font.PLAIN,
                        obj.getAsJsonPrimitive("size").getAsInt());
            }

            throw new JsonParseException("Unable to parse font at: " + json);
        }

        public JsonElement serialize(Font src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getSize() + ":" + src.getName() + ":" + src.getStyle());
        }

}
