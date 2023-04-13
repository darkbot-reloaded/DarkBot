package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.util.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CaptchaHandler {
    private final BackpageManager backpage;
    private final String base;
    private final String action;
    private final ConfigSetting<Boolean> setting;
    private CompletableFuture<Map<String, String>> captchaResponseFuture;

    public CaptchaHandler(BackpageManager backpage, ConfigAPI configAPI, String base, String action) {
        this.backpage = backpage;
        this.setting = configAPI.getConfig("miscellaneous.solve_backpage_captcha");
        this.base = base;
        this.action = action;
        CaptchaAPI.getInstance();
    }

    public boolean isSolvingCaptcha() {
        return captchaResponseFuture != null;
    }

    public boolean needsCaptchaSolve(URL url, String page) {
        return url.getQuery().contains("lostPilot") && page.contains("id=\"captchaScriptContainer\"");
    }

    public void solveCaptcha() throws IOException {
        if (setting == null || !setting.getValue() || CaptchaAPI.getInstance() == null) return;
        if (isSolvingCaptcha()) return;

        HttpURLConnection connection = backpage.getHttp(base).getConnection();
        String page = IOUtils.read(connection.getInputStream());

        if (needsCaptchaSolve(connection.getURL(), page)) {
            System.out.println("CaptchaHandler: Solving Captcha for " + this.action);
            captchaResponseFuture = CaptchaAPI.getInstance().solveCaptchaFuture(connection.getURL(), page)
                    .thenApply(r -> {
                        try {
                            if (r.isEmpty()) return r;
                            eu.darkbot.util.http.Http http = backpage.postHttp("ajax/lostpilot.php");
                            r.forEach(http::setParam);
                            http.setParam("desiredAction", action);
                            String response = http.getContent();

                            if (response.toString().contains("OK")) {
                                System.out.println("Captcha Solve Accepted");
                            } else {
                                System.out.println("Captcha Solve Denied: " + response);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return r;
                    });
            captchaResponseFuture.whenComplete((r, t) -> {
                System.out.println("CaptchaHandler: Done Solving for " + this.action);
                captchaResponseFuture = null;
            });
        }
    }
}
