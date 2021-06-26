package com.github.manolo8.darkbot.utils;

import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Source: https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb
 */
public class DiscordWebhook {

    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<>();

    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }

    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void execute() throws IOException {
        if (this.content == null && this.embeds.isEmpty()) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        JsonObject json = new JsonObject();

        json.addProperty("content", this.content);
        json.addProperty("username", this.username);
        json.addProperty("avatar_url", this.avatarUrl);
        json.addProperty("tts", this.tts);

        if (!this.embeds.isEmpty()) {
            List<JsonObject> embedObjects = new ArrayList<>();

            for (EmbedObject embed : this.embeds) {
                JsonObject jsonEmbed = new JsonObject();

                jsonEmbed.addProperty("title", embed.getTitle());
                jsonEmbed.addProperty("description", embed.getDescription());
                jsonEmbed.addProperty("url", embed.getUrl());

                if (embed.getColor() != null) {
                    Color color = embed.getColor();
                    int rgb = color.getRed();
                    rgb = (rgb << 8) + color.getGreen();
                    rgb = (rgb << 8) + color.getBlue();

                    jsonEmbed.addProperty("color", rgb);
                }

                EmbedObject.Footer footer = embed.getFooter();
                EmbedObject.Image image = embed.getImage();
                EmbedObject.Thumbnail thumbnail = embed.getThumbnail();
                EmbedObject.Author author = embed.getAuthor();
                List<EmbedObject.Field> fields = embed.getFields();

                if (footer != null) {
                    JsonObject jsonFooter = new JsonObject();

                    jsonFooter.addProperty("text", footer.getText());
                    jsonFooter.addProperty("icon_url", footer.getIconUrl());
                    jsonEmbed.add("footer", jsonFooter);
                }

                if (image != null) {
                    JsonObject jsonImage = new JsonObject();

                    jsonImage.addProperty("url", image.getUrl());
                    jsonEmbed.add("image", jsonImage);
                }

                if (thumbnail != null) {
                    JsonObject jsonThumbnail = new JsonObject();

                    jsonThumbnail.addProperty("url", thumbnail.getUrl());
                    jsonEmbed.add("thumbnail", jsonThumbnail);
                }

                if (author != null) {
                    JsonObject jsonAuthor = new JsonObject();

                    jsonAuthor.addProperty("name", author.getName());
                    jsonAuthor.addProperty("url", author.getUrl());
                    jsonAuthor.addProperty("icon_url", author.getIconUrl());
                    jsonEmbed.add("author", jsonAuthor);
                }

                List<JsonObject> jsonFields = new ArrayList<>();
                for (EmbedObject.Field field : fields) {
                    JsonObject jsonField = new JsonObject();

                    jsonField.addProperty("name", field.getName());
                    jsonField.addProperty("value", field.getValue());
                    jsonField.addProperty("inline", field.isInline());

                    jsonFields.add(jsonField);
                }
//                jsonEmbed.add("fields", jsonFields.toArray());
                jsonFields.forEach(jsonField -> jsonEmbed.add("fields", jsonField));
                embedObjects.add(jsonEmbed);
            }
//            json.add("embeds", embedObjects.toArray());
            embedObjects.forEach(embedObject -> json.add("embeds", embedObject));
        }

        URL url = new URL(this.url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream stream = connection.getOutputStream();
        stream.write(json.toString().getBytes());
        stream.flush();
        stream.close();

        connection.getInputStream().close(); //I'm not sure why but it doesn't work without getting the InputStream
        connection.disconnect();
    }

    public static class EmbedObject {
        private String title;
        private String description;
        private String url;
        private Color color;

        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private final List<Field> fields = new ArrayList<>();

        public String getTitle() {
            return title;
        }

        public EmbedObject setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public EmbedObject setDescription(String description) {
            this.description = description;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public EmbedObject setUrl(String url) {
            this.url = url;
            return this;
        }

        public Color getColor() {
            return color;
        }

        public EmbedObject setColor(Color color) {
            this.color = color;
            return this;
        }

        public Footer getFooter() {
            return footer;
        }

        public Thumbnail getThumbnail() {
            return thumbnail;
        }

        public EmbedObject setThumbnail(String url) {
            this.thumbnail = new Thumbnail(url);
            return this;
        }

        public Image getImage() {
            return image;
        }

        public EmbedObject setImage(String url) {
            this.image = new Image(url);
            return this;
        }

        public Author getAuthor() {
            return author;
        }

        public List<Field> getFields() {
            return fields;
        }

        public EmbedObject setFooter(String text, String icon) {
            this.footer = new Footer(text, icon);
            return this;
        }

        public EmbedObject setAuthor(String name, String url, String icon) {
            this.author = new Author(name, url, icon);
            return this;
        }

        public EmbedObject addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        private class Footer {
            private final String text;
            private final String iconUrl;

            private Footer(String text, String iconUrl) {
                this.text = text;
                this.iconUrl = iconUrl;
            }

            private String getText() {
                return text;
            }

            private String getIconUrl() {
                return iconUrl;
            }
        }

        private class Thumbnail {
            private final String url;

            private Thumbnail(String url) {
                this.url = url;
            }

            private String getUrl() {
                return url;
            }
        }

        private class Image {
            private final String url;

            private Image(String url) {
                this.url = url;
            }

            private String getUrl() {
                return url;
            }
        }

        private class Author {
            private final String name;
            private final String url;
            private final String iconUrl;

            private Author(String name, String url, String iconUrl) {
                this.name = name;
                this.url = url;
                this.iconUrl = iconUrl;
            }

            private String getName() {
                return name;
            }

            private String getUrl() {
                return url;
            }

            private String getIconUrl() {
                return iconUrl;
            }
        }

        private class Field {
            private final String name;
            private final String value;
            private final boolean inline;

            private Field(String name, String value, boolean inline) {
                this.name = name;
                this.value = value;
                this.inline = inline;
            }

            private String getName() {
                return name;
            }

            private String getValue() {
                return value;
            }

            private boolean isInline() {
                return inline;
            }
        }
    }
}
