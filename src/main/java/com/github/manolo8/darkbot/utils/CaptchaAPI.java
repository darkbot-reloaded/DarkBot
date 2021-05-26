package com.github.manolo8.darkbot.utils;

public interface CaptchaAPI {
    String TWOCAPTCHA_PATH = "lib/twocaptcha.jar";
    CaptchaAPI INSTANCE = ReflectionUtils.createInstance("com.pikapika.twocaptcha", TWOCAPTCHA_PATH);
    static CaptchaAPI getInstance(){
        return INSTANCE;
    }

    /**
     * Pass a website page and it will parse the g-site-key and solve using external service.
     * @param webpage a page that has been consumed into a string
     * @return A solved g-captcha-response key from 2Captcha Solver Service
     */
    String solveCaptcha(String webpage);
}
