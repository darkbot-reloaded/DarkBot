package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemInfo extends Item {
    private String name;
    private String localizationId;
    @SerializedName("C") private String category;
    @JsonAdapter(LevelsDeserializer.class) private List<Map<String, Object>> levels;

    public String getName() {
        return name;
    }

    public String getLocalizationId() {
        return localizationId;
    }

    public void setLocalizationId(String localizationId) {
        this.localizationId = localizationId;
    }

    public String getCategory() {
        return category;
    }

    public List<Map<String, Object>> getLevels() {
        return levels;
    }

    public BufferedImage getBufferedImage(IconType type) {
        try {
            return ImageIO.read(new URL("http://www.darkorbit.com/do_img/global/items/"
                    + getLocalizationId().replace("_", "/") + "_" + type.urlName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public enum IconType {
        SMALL("30x30"),MEDIUM("100x100"),TOP("top");
        String urlName;

        IconType(String name) {
            this.urlName = name;
        }

        @Override
        public String toString() {
            return this.urlName;
        }
    }

    @Override
    public String toString() {
        return "ItemInfo{" +
                "name='" + name + '\'' +
                ", localizationId='" + localizationId + '\'' +
                ", category='" + category + '\'' +
                ", levels=" + levels +
                "} " + super.toString();
    }

    private static class LevelsDeserializer implements JsonDeserializer<List<Map<String, Object>>> {
        @Override
        public List<Map<String, Object>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonArray()) return context.deserialize(json, typeOfT);
            if (json.isJsonObject()) {
                final JsonObject obj = json.getAsJsonObject();
                return obj.entrySet().stream().map(entry -> {
                    Map<String, Object> map = context.deserialize(entry.getValue(), Map.class);
                    map.put("level", entry.getKey());
                    return map;
                }).collect(Collectors.toList());
            }
            return null;
        }
    }
}
