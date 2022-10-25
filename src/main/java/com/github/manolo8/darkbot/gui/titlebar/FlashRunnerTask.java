package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.utils.LibSetup;
import com.github.manolo8.darkbot.utils.http.Method;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlashRunnerTask extends Thread {
    private static final Path RUNNER_PATH = Paths.get("lib", "KekkaRunner.exe");
    private static final Pattern PARAMS_PATTERN = Pattern.compile("src\":.\"([^\"]+).*width\":.(\\d+).*height\":.(\\d+).*(cdn[^}]+)");

    private static boolean LIB_CHECKED = false;

    private final BackpageManager backpageManager;
    private final JMenuItem menuItem;
    private final String name;

    public FlashRunnerTask(BackpageManager backpageManager, JMenuItem menuItem, String name) {
        super("FlashRunner: " + name);
        this.setDaemon(true);

        this.backpageManager = backpageManager;
        this.menuItem = menuItem;
        this.name = name;

        start();
    }

    @Override
    public void run() {
        if (!backpageManager.isInstanceValid() || !menuItem.isEnabled()) return;
        menuItem.setEnabled(false);

        if (!LIB_CHECKED) {
            LibSetup.downloadLib(RUNNER_PATH.getFileName().toString());
            LIB_CHECKED = true;
        }

        if (Files.notExists(RUNNER_PATH)) {
            menuItem.setEnabled(true);
            return;
        }

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
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        menuItem.setEnabled(true);
    }
}
