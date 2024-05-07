package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.LibUtils;
import eu.darkbot.api.config.ConfigSetting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlashRunnerTask extends Thread {
    private static final Pattern PARAMS_PATTERN = Pattern.compile("src\":.\"([^\"]+).*width\":.(\\d+).*height\":.(\\d+).*(cdn[^}]+)");

    private static Path RUNNER_PATH = null;

    private final String name;
    private final Main main;
    private final BackpageManager backpageManager;
    private final Consumer<Boolean> onComplete;

    public FlashRunnerTask(String name, Main main, Consumer<Boolean> onComplete) {
        super("FlashRunner: " + name);
        this.setDaemon(true);

        this.name = name;
        this.main = main;
        this.backpageManager = main.backpage;
        this.onComplete = onComplete;

        start();
    }

    @Override
    public void run() {
        boolean result = false;

        if (RUNNER_PATH == null)
            RUNNER_PATH = LibUtils.getSharedLibrary("KekkaRunner.exe");

        if (backpageManager.isInstanceValid() && Files.exists(RUNNER_PATH)) {
            try {
                String url = "indexInternal.es?action=internal" + name;
                if (main.config.BOT_SETTINGS.API_CONFIG.FORCE_GAME_LANGUAGE) {
                    url += "&lang=" + I18n.getLocale().getLanguage();
                }
                String content = backpageManager.getHttp(url).getContent();
                Matcher matcher = PARAMS_PATTERN.matcher(content);

                if (matcher.find()) {
                    String movie = matcher.group(1);
                    String width = matcher.group(2);
                    String height = matcher.group(3);
                    String vars = matcher.group(4);

                    vars = vars.replaceAll("\": \"", "=")
                            .replaceAll("\",\"", "&");

                    new ProcessBuilder(RUNNER_PATH.toAbsolutePath().toString(),
                            "--sid", backpageManager.getSid(),
                            "--url", backpageManager.getInstanceURI().toString(),
                            "--movie", movie,
                            "--width", width,
                            "--height", height,
                            "--name", name + " | " + main.hero.playerInfo.getUsername(),
                            "--flash", LibUtils.getFlashOcxPath().toString(),
                            "--vars", vars) //vars must be last, ProcessBuilder weirdly handles space?
                            .start().waitFor();

                    result = true;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        onComplete.accept(result);
    }
}
