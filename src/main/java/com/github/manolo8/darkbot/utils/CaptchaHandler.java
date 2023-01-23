package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.util.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CaptchaHandler {
    private final BackpageManager backpage;
    private final String base, action;
    private final ConfigSetting<Boolean> setting;
    private CompletableFuture<Map<String, String>> captchaResponseFuture;

    public CaptchaHandler(BackpageManager backpage, ConfigSetting<Boolean> setting, String base, String action) {
        this.backpage = backpage;
        this.setting = setting;
        this.base = base;
        this.action = action;
    }

    public boolean isSolvingCaptcha() {
        return captchaResponseFuture != null;
    }

    public boolean needsCaptchaSolve(String page) {
        return page.contains("id=\"captchaScriptContainer\"");
    }

    public boolean solveCaptcha() throws IOException {
        if (!setting.getValue() || CaptchaAPI.getInstance() == null) return false;
        if (isSolvingCaptcha()) return false;

        HttpURLConnection conn = backpage.getHttp(base).getConnection();
        conn.setInstanceFollowRedirects(false);
        if (!conn.getHeaderField("Location").isEmpty()) {
            HttpURLConnection connection = backpage.getHttp(conn.getHeaderField("Location")).getConnection();
            captchaResponseFuture = CaptchaAPI.getInstance().solveCaptchaFuture(connection.getURL(), IOUtils.read(connection.getInputStream(), true))
                    .whenComplete((r, t) -> {
                        try {
                            if (r.isEmpty()) return;
                            eu.darkbot.util.http.Http http = backpage.getHttp("ajax/lostpilot.php")
                                    .setParam("command", "checkReCaptcha")
                                    .setParam("desiredAction", action);
                            r.forEach(http::setParam);
                            http.closeInputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            captchaResponseFuture.whenComplete((r, t) -> captchaResponseFuture = null);
            return true;
        }
        return false;
    }

}
