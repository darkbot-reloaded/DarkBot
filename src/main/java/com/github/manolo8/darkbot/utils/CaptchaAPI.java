package com.github.manolo8.darkbot.utils;

public interface CaptchaAPI {
    String SOLVER_PATH = "lib/captchasolver.jar";
    CaptchaAPI INSTANCE;

    static {
        try {
            INSTANCE = ReflectionUtils.createInstance("eu.darkbot.captcha.CaptchaSolver", SOLVER_PATH);
        } catch (Exception e) {
            System.out.printLn("No captcha resolver is configured, if you're not trying to use one you can safely ignore this message");
        }
    }

    static CaptchaAPI getInstance() {
        return INSTANCE;
    }

    /**
     * Pass a website page and it will parse the g-site-key and solve using external service.
     * @param webpage a page that has been consumed into a string
     * @return A solved g-captcha-response key from 2Captcha Solver Service
     */
    String solveCaptcha(String webpage);
}
