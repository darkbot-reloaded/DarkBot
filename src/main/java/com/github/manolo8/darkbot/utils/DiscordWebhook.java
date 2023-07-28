package com.github.manolo8.darkbot.utils;

import com.google.gson.annotations.SerializedName;
import eu.darkbot.util.http.Http;
import eu.darkbot.util.http.Method;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptation of original source: <a href="https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb">source</a>
 */
public class DiscordWebhook {
    private String content;
    private String username;
    @SerializedName("avatar_url") private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<>();

    public DiscordWebhook setContent(String content) {
        this.content = content;
        return this;
    }

    public DiscordWebhook setUsername(String username) {
        this.username = username;
        return this;
    }

    public DiscordWebhook setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public DiscordWebhook setTts(boolean tts) {
        this.tts = tts;
        return this;
    }

    public DiscordWebhook addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
        return this;
    }

    public int execute(String url) throws IOException {
        if (this.content == null && this.embeds.isEmpty()) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        return Http.create(url, Method.POST)
                .setRawHeader("Content-Type", "application/json; charset=UTF-8")
                .setJsonBody(this)
                .getConnection().getResponseCode();
    }

    public static class EmbedObject {

        private String title;
        private String description;
        private String url;
        private Color color;
        private Footer footer;
        private Image image;
        private Thumbnail thumbnail;
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

        public EmbedObject setFooter(String text, String icon) {
            this.footer = new Footer(text, icon);
            return this;
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

        public EmbedObject setAuthor(Author author) {
            this.author = author;
            return this;
        }

        public EmbedObject setAuthor(String name, String url, String icon) {
            return setAuthor(new Author(name, url, icon));
        }

        public List<Field> getFields() {
            return fields;
        }

        public EmbedObject addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        private static class Footer {
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

        private static class Thumbnail {
            private final String url;

            private Thumbnail(String url) {
                this.url = url;
            }

            private String getUrl() {
                return url;
            }
        }

        private static class Image {
            private final String url;

            private Image(String url) {
                this.url = url;
            }

            private String getUrl() {
                return url;
            }
        }

        private static class Author {
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

        private static class Field {
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
