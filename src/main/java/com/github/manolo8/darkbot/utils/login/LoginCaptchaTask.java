package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.titlebar.BackpageTask;
import com.github.manolo8.darkbot.utils.CaptchaAPI;
import eu.darkbot.api.config.ConfigSetting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LoginCaptchaTask extends BackpageTask implements CaptchaAPI {
    private static final Version CAPTCHA_MIN_VERSION = new Version("1.3.0");
    // use only on first login
    static boolean firstLogin = true;

    LoginCaptchaTask() {
        super(null, null);
    }

    private String getCaptcha(String url, String siteKey) {
        String result = null;
        try {
            if (!BackpageTask.isSupported(CAPTCHA_MIN_VERSION))
                return null;

            Process process = BackpageTask.createBrowser("--captcha", siteKey,
                    "--exit", String.valueOf(30_000),
                    "--url", url);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String key = "[captchaResult]";
                int cIdx = line.indexOf(key);
                int eIdx = line.indexOf("[captchaFailed]");

                if (eIdx != -1) break;
                if (cIdx != -1) {
                    result = line.substring(key.length());
                    if (result.trim().isEmpty())
                        result = null;
                    break;
                }
            }
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Map<String, String> solveCaptcha(URL url, String webpage) {
        ConfigSetting<Boolean> showCaptcha = Main.INSTANCE.configHandler.requireConfig("bot_settings.other.always_show_captcha");

        MainGui gui = Main.INSTANCE.getGui();
        if (firstLogin || (gui != null && gui.hasFocus()) || showCaptcha.getValue()) {
            Pattern p = Pattern.compile("data-sitekey=\"([^\"]+)\"");
            Matcher matcher = p.matcher(webpage);

            if (matcher.find()) {
                String captcha = getCaptcha(url.toString(), matcher.group(1));
                if (captcha != null)
                    return Map.of("g-recaptcha-response", captcha,
                            "h-captcha-response", captcha);
            }
        }
        return Collections.emptyMap();
    }
}
