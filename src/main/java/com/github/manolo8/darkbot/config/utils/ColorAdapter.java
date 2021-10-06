package com.github.manolo8.darkbot.config.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.Base64;

public class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {

        public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive())
                return new Color(Integer.parseUnsignedInt(json.getAsString().replace("#", ""), 16), true);

            // Parse pre-adapter format, stored as json object
            if (json.isJsonObject())
                return new Color(json.getAsJsonObject().getAsJsonPrimitive("value").getAsInt(), true);

            throw new JsonParseException("Unable to parse color at: " + json);
        }

        public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(String.format("#%08X", src.getRGB()));
        }

}
