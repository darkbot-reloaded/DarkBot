package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.config.UnresolvedPlayer;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.Base62;
import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.Task;

import javax.swing.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Feature(name = "Username updater", description = "Updates player names & ids")
public class UsernameUpdater implements Task {

    private static final Pattern PROFILE_ID = Pattern.compile("/p/([A-Za-z0-9]+)-");

    private final Main main;
    private final BackpageManager backpageManager;

    private Config config;

    public UsernameUpdater(Main main, BackpageManager backpageManager) {
        this.main = main;
        this.backpageManager = backpageManager;

        this.config = main.config;
    }

    @Override
    public void onTickTask() {
        this.config = main.config;

        UnresolvedPlayer user = config.UNRESOLVED.poll();
        if (user == null || (user.userId == -1 && user.username == null)) return;
        if (user.shouldWait()) {
            reQueue(user);
            return;
        }

        try {
            boolean byId = user.userId != -1;

            UserResponse response = backpageManager.postHttp("ajax/" + (byId ? "user" : "pilotprofil") + ".php")
                    .setParam("command", (byId ? "loadUserInfo" : "searchProfileFromExternalPPP"))
                    .setParam(byId ? "userId" : "profileUsername", byId ? Base62.encode(user.userId) : user.username)
                    .fromJson(UserResponse.class);

            if (response == null || !Objects.equals(response.result, "OK")) {
                reQueue(user);
                return;
            }

            int id = response.getId();
            if (!byId && (Objects.equals(response.url, "false") || id == -1)) {
                Popups.of(I18n.get("gui.players.not_found.title"),
                        I18n.get("gui.players.not_found.by_name", user.username), JOptionPane.WARNING_MESSAGE).showAsync();
                return; // Don't re-queue
            }

            if (byId && (response.userName == null || response.userName.isEmpty())) {
                if (user.username != null) return; // Probably just updating the player, ignore
                Popups.of(I18n.get("gui.players.not_found.title"),
                        I18n.get("gui.players.not_found.by_id", user.userId), JOptionPane.WARNING_MESSAGE).showAsync();
                return; // Don't re-queue
            }

            if (byId) user.username = response.getUsername();
            else user.userId = id;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (user.userId != -1 && user.username != null) {
            PlayerInfo pl = config.PLAYER_INFOS.get(user.userId);
            if (pl != null) {
                pl.username = user.username;
                pl.lastUpdate = System.currentTimeMillis();
            } else {
                config.PLAYER_INFOS.put(user.userId, new PlayerInfo(user.username, user.userId));
            }

            config.PLAYER_UPDATED.send(user.userId);
            ConfigEntity.changed();
        } else reQueue(user);
    }

    private void reQueue(UnresolvedPlayer user) {
        user.retries++;
        user.lastUpdate = System.currentTimeMillis();
        config.UNRESOLVED.add(user);
    }

    private static class UserResponse {
        String result;
        String userName;
        String url;

        private String getUsername() {
            return URLDecoder.decode(userName, StandardCharsets.UTF_8);
        }

        private int getId() {
            if (url == null) return -1;
            Matcher m = PROFILE_ID.matcher(url);
            if (!m.find()) return -1;
            int id = Base62.decode(m.group(1));
            return id == 0 ? -1 : id;
        }
    }

}
