package com.github.manolo8.darkbot.utils;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
     * Create solving captcha parameters for the provided webpage html string.
     * @param url this webpage was obtained from
     * @param webpage as an HTML string
     * @return form parameters to include as captcha response solution
     */
    Map<String, String> solveCaptcha(URL url, String webpage);

    /**
     * Create solving captcha parameters for the provided webpage html string.
     * @param url this webpage was obtained from
     * @param webpage as an HTML string
     * @return requestID of captcha solver that can be retrieved later
     */
    String solveCaptchaRequestId(URL url, String webpage);

    /**
     * Fetches response for captcha that was sent prior
     * @param requestID request id that was received by {solveCaptchaRequestId}
     * @return status of captcha
     */
    boolean isCaptchaSolved(String requestID);

    /**
     * Fetches response for captcha that was sent prior
     * @param requestID request id that was received by {solveCaptchaRequestId}
     * @return form parameters to include as captcha response solution
     */
    Map<String, String> fetchCaptchaResponse(String requestID);
}
