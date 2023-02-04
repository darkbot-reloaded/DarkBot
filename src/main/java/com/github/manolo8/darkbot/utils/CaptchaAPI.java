package com.github.manolo8.darkbot.utils;


import com.github.manolo8.darkbot.extensions.util.Version;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CaptchaAPI {
    Path SOLVER_PATH = Paths.get("lib", "captchasolver.jar");
    CaptchaAPI INSTANCE = createInstance();

    static CaptchaAPI createInstance() {
        try {
            return ReflectionUtils.createInstance("eu.darkbot.captcha.CaptchaSolver", SOLVER_PATH);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("No captcha resolver is configured, if you're not trying to use one you can safely ignore this message");
        return null;
    }

    static CaptchaAPI getInstance() {
        return INSTANCE;
    }

    /**
     * @return version of captcha solver
     */

    default Version getVersion() {
        return new Version("0.0.0");
    }

    /**
     * Create solving captcha parameters for the provided webpage html string.
     *
     * @param url     this webpage was obtained from
     * @param webpage as an HTML string
     * @return form parameters to include as captcha response solution
     */
    Map<String, String> solveCaptcha(URL url, String webpage);


    /**
     * Creates CompletableFuture
     * solving captcha parameters for the provided webpage html string.
     *
     * @param url     this webpage was obtained from
     * @param webpage as an HTML string
     * @return CompletableFuture with form parameters to include as captcha response solution
     */
    default CompletableFuture<Map<String, String>> solveCaptchaFuture(URL url, String webpage) {
        return CompletableFuture.supplyAsync(() -> solveCaptcha(url, webpage));
    }
}
