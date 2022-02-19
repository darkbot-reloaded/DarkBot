package com.github.manolo8.darkbot.config;

import eu.darkbot.api.game.entities.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PlayerInfo implements eu.darkbot.api.config.types.PlayerInfo {
    public String username;
    public int userId;

    public long lastUpdate = System.currentTimeMillis();

    public Map<PlayerTag, Long> subscriptions = new HashMap<>();

    private transient PlayerTags tags = null;

    public PlayerInfo() {}

    public PlayerInfo(String username, int userId) {
        this.username = username;
        this.userId = userId;
    }

    public PlayerInfo(Player player) {
        this(player.getEntityInfo().getUsername(), player.getId());
    }

    public void setTag(PlayerTag tag, Long until) {
        subscriptions.put(tag, until == null ? -1 : until);
    }

    public void removeTag(PlayerTag tag) {
        subscriptions.remove(tag);
    }

    public boolean hasTag(PlayerTag tag) {
        Long until = subscriptions.get(tag);
        if (until != null && (until == -1 || until > System.currentTimeMillis())) return true;
        subscriptions.remove(tag);
        return false;
    }

    public boolean filter(String string) {
        return string == null
                || username.toLowerCase(Locale.ROOT).contains(string)
                || String.valueOf(userId).contains(string)
                || subscriptions.keySet().stream().anyMatch(tag -> tag.name.toLowerCase(Locale.ROOT).contains(string));
    }

    @Override
    public String toString() {
        return username + "(" + userId + ")";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public PlayerTags getTags() {
        if (tags == null) tags = new PlayerTagsImpl();
        return tags;
    }

    private class PlayerTagsImpl implements PlayerTags {
        @Override
        public boolean add(eu.darkbot.api.config.types.PlayerTag playerTag, @Nullable Instant instant) {
            long until = instant == null ? -1 : instant.toEpochMilli();
            return !Objects.equals(subscriptions.put((PlayerTag) playerTag, until), until);
        }

        @Override
        public boolean contains(eu.darkbot.api.config.types.PlayerTag playerTag) {
            return hasTag((PlayerTag) playerTag);
        }

        @Override
        public boolean remove(eu.darkbot.api.config.types.PlayerTag playerTag) {
            return subscriptions.remove((PlayerTag) playerTag) != null;
        }

        @NotNull
        @Override
        public Collection<? extends eu.darkbot.api.config.types.PlayerTag> get() {
            return subscriptions.keySet();
        }
    }

}
