package com.github.manolo8.darkbot.utils;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public interface CaptchaAPI {
    String SOLVER_PATH = "lib/captchasolver.jar";
    CaptchaAPI INSTANCE = Files.exists(Paths.get(SOLVER_PATH)) ? ReflectionUtils.createInstance("eu.darkbot.captcha.CaptchaSolver", SOLVER_PATH) : null;
/*
//Can not initialize in interface
static{
    try {
            INSTANCE = ReflectionUtils.createInstance("eu.darkbot.captcha.CaptchaSolver", SOLVER_PATH);
    } catch (Exception e) {
        System.out.println("No captcha resolver is configured, if you're not trying to use one you can safely ignore this message");
    }
    }
 */

    static CaptchaAPI getInstance() {
        return INSTANCE;
    }

    /**
     * Create solving captcha parameters for the provided webpage html string.
     * @param url this webpage was obtained from
     * @param webpage as an HTML string
     * @return form parameters to include as captcha response solution
     */
    Map<String, String> solveCaptcha(URL url, String webpage);
}
