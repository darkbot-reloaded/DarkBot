package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.utils.LibSetup;
import com.github.manolo8.darkbot.utils.http.Method;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlashRunnerTask extends Thread {
    private static final Path RUNNER_PATH = Paths.get("lib", "KekkaRunner.exe");
    private static final Pattern PARAMS_PATTERN = Pattern.compile("src\":.\"([^\"]+).*width\":.(\\d+).*height\":.(\\d+).*(cdn[^}]+)");

    private static boolean LIB_CHECKED = false;

    private final String name;
    private final BackpageManager backpageManager;
    private final Consumer<Boolean> onComplete;

    public FlashRunnerTask(String name, BackpageManager backpageManager, Consumer<Boolean> onComplete) {
        super("FlashRunner: " + name);
        this.setDaemon(true);

        this.name = name;
        this.backpageManager = backpageManager;
        this.onComplete = onComplete;

        start();
    }

    @Override
    public void run() {
        boolean result = false;

        if (!LIB_CHECKED) {
            LibSetup.downloadLib(RUNNER_PATH.getFileName().toString());
            LIB_CHECKED = true;
        }

        if (backpageManager.isInstanceValid() && Files.exists(RUNNER_PATH)) {
            try {
                String content = backpageManager.getConnection("indexInternal.es?action=internal" + name, Method.GET).getContent();
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
                            "--name", name,
                            //todo add flash path
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
