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
     * Create solving captcha parameters for the provided webpage html string.
     * @param the url this webpage was obtained from
     * @param webpage as an HTML string
     * @return form parameters to include as captcha response solution
     */
    Map<String, String> solveCaptcha(URL url, String webpage);
}
