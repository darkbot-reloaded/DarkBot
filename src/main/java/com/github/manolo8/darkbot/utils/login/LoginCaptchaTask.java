package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.titlebar.BackpageTask;
import com.github.manolo8.darkbot.utils.CaptchaAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginCaptchaTask extends BackpageTask implements CaptchaAPI {
    // use only on first login
    private static boolean used;

    public LoginCaptchaTask() {
        super(null, null);
    }

    private String getCaptcha(String url, String siteKey) {
        String result = null;
        try {
            Version version = readVersionFile();
            if (version == null || version.isOlderThan(new Version("1.3.0")))
                return "";

            Process process = new ProcessBuilder(BACKPAGE_PATH.resolve(EXECUTABLE_NAME).toAbsolutePath().toString(),
                    "--captcha", siteKey,
                    "--exit", String.valueOf(30_000),
                    "--url", url)
                    .start();

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
        if (used) return Collections.emptyMap();

        Pattern p = Pattern.compile("data-sitekey=\"([^\"]+)\"");
        Matcher matcher = p.matcher(webpage);

        if (matcher.find()) {
            String captcha = getCaptcha(url.toString(), matcher.group(1));
            if (captcha == null) throw new IllegalStateException();
            // return empty map if is used with old backpage version
            if (captcha.isEmpty()) return Collections.emptyMap();

            used = true;
            return Map.of("g-recaptcha-response", captcha,
                    "h-captcha-response", captcha);
        }

        throw new IllegalStateException();
    }
}
