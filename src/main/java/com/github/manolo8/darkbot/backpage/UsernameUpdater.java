package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.utils.Base62;
import com.github.manolo8.darkbot.utils.IOUtils;

import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Feature(name = "Username updater", description = "Updates player names & ids")
public class UsernameUpdater implements Task {

    private Config config;
    private BackpageManager backpageManager;


    @Override
    public void install(Main main) {
        this.config = main.config;
        this.backpageManager = main.backpage;
    }

    @Override
    public void tick() {
        PlayerInfo user = config.UNRESOLVED.poll();
        if (user == null) return;
        try {
            if (user.userId != -1) {
                HttpURLConnection conn = backpageManager.getConnection("ajax/user.php");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                IOUtils.write(conn.getOutputStream(), "command=loadUserInfo&userId=" + Base62.encode(user.userId));
                String json = IOUtils.read(conn.getInputStream());
                UserResponse response = Main.GSON.fromJson(json, UserResponse.class);
                if (response != null && Objects.equals(response.result, "OK") && !response.userName.isEmpty())
                    user.username = response.userName;
            } else if (user.username != null) {
                HttpURLConnection conn = backpageManager.getConnection("/ajax/pilotprofil.php");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                IOUtils.write(conn.getOutputStream(), "command=searchProfileFromExternalPPP&profileUsername=" + user.username);
                ProfileResponse response = Main.GSON.fromJson(IOUtils.read(conn.getInputStream()), ProfileResponse.class);
                if (Objects.equals(response.result, "OK")) {
                    if (Objects.equals(response.url, "false")) user.userId = -2;
                    else user.userId = response.getId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (user.userId != -1 && user.username != null)
            config.PLAYER_INFOS.put(user.userId, user);

        if (user.userId == -1 || user.username == null)
            config.UNRESOLVED.add(user);
    }

    private static class UserResponse {
        String result;
        String userName;
        boolean isBlockedByMe;
    }

    private static class ProfileResponse {
        String result;
        String url;

        private int getId() {
            Pattern p = Pattern.compile("/p/([A-Za-z0-9]+)-");
            Matcher m = p.matcher(url);
            if (m.find()) return Base62.decode(m.group(1));
            return -1;
        }
    }

}
