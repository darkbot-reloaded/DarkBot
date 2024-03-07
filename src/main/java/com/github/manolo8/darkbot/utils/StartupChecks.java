package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.data.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class StartupChecks {

    private static final Path PID_FILE = Paths.get("curr.pid");

    public static void checkJavaVersion(StartupParams params) {
        if (params.has(StartupParams.LaunchArg.NO_WARN)) return;
        String java = System.getProperty("java.version");

        if (!java.startsWith("11.") && !java.startsWith("17.") && !java.equals("17") && !java.startsWith("21.")) {
            Popups.of(I18n.get("start.old_java_warn_title"),
                            I18n.get("start.old_java_warn_content", java),
                            JOptionPane.WARNING_MESSAGE)
                    .optionType(JOptionPane.DEFAULT_OPTION)
                    .showSync();
        }
    }

    public static void checkUniqueInstance(StartupParams params) {
        if (params.has(StartupParams.LaunchArg.NO_WARN)) return;

        Pair<Long, Long> pidFileContent = readPidFile();

        if (pidFileContent != null) {
            checkUniqueInstance(pidFileContent);
        }

        writePidFile();
    }

    private static void checkUniqueInstance(@NotNull Pair<Long, Long> lastPid) {
        ProcessHandle externalProcessHandle = ProcessHandle.of(lastPid.getLeft()).orElse(null);
        // The old process isn't there anymore
        if (externalProcessHandle == null) return;

        ProcessHandle.Info ExternalProcessInfo = externalProcessHandle.info();
        long externalStartTime = ExternalProcessInfo.startInstant().map(Instant::toEpochMilli).orElse(0L);

        // Seems to be a diff process (diff start time)
        if (externalStartTime != lastPid.getRight()) return;

        // Seems like the process is the same as the file references, let the user know
        JButton proceed = new JButton(I18n.get("start.same_folder_warn.button.proceed"), UIUtils.getIcon("warning"));
        JButton cancel = new JButton(I18n.get("start.same_folder_warn.button.cancel"));
        AtomicBoolean refuse = new AtomicBoolean(false);

        proceed.addActionListener(a -> SwingUtilities.getWindowAncestor(proceed).setVisible(false));
        cancel.addActionListener(a -> {
            SwingUtilities.getWindowAncestor(cancel).setVisible(false);
            refuse.set(true);
        });

        Popups.of(I18n.get("start.same_folder_warn.title"),
                        I18n.get("start.same_folder_warn.content"),
                        JOptionPane.WARNING_MESSAGE)
                .options(proceed, cancel)
                .initialValue(cancel)
                .showOptionSync();

        if (refuse.get()) {
            System.out.println(I18n.get("start.same_folder_warn.reject"));
            System.exit(0);
        }
    }

    private static Pair<Long, Long> readPidFile() {
        List<String> fileContent = FileUtils.readAllLines(PID_FILE);
        if (fileContent == null || fileContent.size() != 1) return null;

        String[] fileSplit = fileContent.get(0).split(" ");
        if (fileSplit.length != 2) return null;
        try {
            return new Pair<>(Long.parseLong(fileSplit[0]), Long.parseLong(fileSplit[1]));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writePidFile() {
        long currentPid = ProcessHandle.current().pid();
        ProcessHandle processHandle = ProcessHandle.current();
        ProcessHandle.Info processInfo = processHandle.info();
        long currentStartTime = processInfo.startInstant().map(Instant::toEpochMilli).orElse(0L);
        FileUtils.writeString(PID_FILE,
                currentPid + " " + currentStartTime,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteIfExists(PID_FILE)));
    }


}
