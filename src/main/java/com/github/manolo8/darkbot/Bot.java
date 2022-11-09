package com.github.manolo8.darkbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.LibSetup;
import com.github.manolo8.darkbot.utils.LogUtils;
import com.github.manolo8.darkbot.utils.StartupParams;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Bot {

    public static void main(String[] args) throws IOException {
        // You can enable hardware acceleration via adding jvm arg: -Dsun.java2d.opengl=True
        try {
            setupSecurityPolicy();
        } catch (Exception e) {
            System.out.println("Failed to setup security policy:");
            e.printStackTrace();
        }

        String path = Bot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.console() == null && (path.endsWith(".jar") || path.endsWith(".exe"))) {
            LogUtils.setOutputToFile();
        }
        try {
            UIManager.getFont("Label.font"); // Prevents a linux crash
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 0);
            UIManager.put("Component.arc", 0);
            UIManager.put("Button.default.boldText", false);
            UIManager.put("Table.cellFocusColor", new Color(0, 0, 0, 160));
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        LibSetup.setupLibraries();
        StartupParams params = new StartupParams(args);

        checkJavaVersion(params);

        System.out.println("Starting DarkBot " + Main.VERSION);
        SwingUtilities.invokeLater(() -> new Main(params));
    }

    static void checkJavaVersion(StartupParams params) {
        if (params.has(StartupParams.LaunchArg.NO_WARN)) return;
        String java = System.getProperty("java.version");

        if (!java.startsWith("11.") && !java.startsWith("17.") && !java.equals("17")) {
            Popups.showMessageSync(I18n.get("start.old_java_warn_title"), new JOptionPane(
                    I18n.get("start.old_java_warn_content"),
                    JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION));
        }
    }

    static void checkUniqueInstance(StartupParams params) {
        if (params.has(StartupParams.LaunchArg.NO_WARN)) return;

        Path filePath = Paths.get("curr.pid");
        long currentPid = ProcessHandle.current().pid();
        ProcessHandle processHandle = ProcessHandle.current();
        ProcessHandle.Info processInfo = processHandle.info();
        long currentStartTime = processInfo.startInstant().map(Instant::toEpochMilli).orElse(0L);

        try {
            List<String> fileContent = Files.exists(filePath) ?
                    Files.readAllLines(filePath, StandardCharsets.UTF_8) : Collections.emptyList();
            for (String line : fileContent) {
                try {
                    long filePid = 0;
                    long fileStartTime = 0;
                    String[] fileSplit = line.split(" ");
                    if (fileSplit.length == 2) {
                        filePid = Long.parseLong(fileSplit[0]);
                        fileStartTime = Long.parseLong(fileSplit[1]);
                    }

                    ProcessHandle externalProcessHandle = ProcessHandle.of(filePid).orElse(null);
                    long externalStartTime = 0;
                    if (externalProcessHandle != null) {
                        ProcessHandle.Info ExternalProcessInfo = externalProcessHandle.info();
                        externalStartTime = ExternalProcessInfo.startInstant().map(Instant::toEpochMilli).orElse(0L);
                    }

                    if (externalProcessHandle != null && externalStartTime == fileStartTime) {
                        JButton proceed = new JButton(I18n.get("start.same_folder_warn.button.proceed"), UIUtils.getIcon("warning"));
                        JButton cancel = new JButton(I18n.get("start.same_folder_warn.button.cancel"));
                        AtomicInteger result = new AtomicInteger(-1);

                        proceed.addActionListener(a -> {
                            SwingUtilities.getWindowAncestor(proceed).setVisible(false);
                            result.set(0);
                        });
                        cancel.addActionListener(a -> {
                            SwingUtilities.getWindowAncestor(cancel).setVisible(false);
                            result.set(1);
                        });

                        Popups.of(I18n.get("start.same_folder_warn.title"),
                                        I18n.get("start.same_folder_warn.content"),
                                        JOptionPane.WARNING_MESSAGE)
                                .options(new Object[]{proceed, cancel})
                                .initialValue(cancel)
                                .showOptionSync();

                        if (result.get() == 1) {
                            System.out.println(I18n.get("start.same_folder_warn.reject"));
                            System.exit(0);
                        }
                        break;
                    }
                } catch (java.lang.NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            Files.writeString(filePath, currentPid + " " + currentStartTime, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setupSecurityPolicy() {
        Policy.setPolicy(new Policy() {
            @Override
            public PermissionCollection getPermissions(ProtectionDomain domain) {
                // Externally loaded classes get no permissions
                if (domain.getClassLoader() instanceof URLClassLoader) return new Permissions();

                // Stuff from other loaders gets permissions
                Permissions permissions = new Permissions();
                permissions.add(new AllPermission());
                return permissions;
            }
        });
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if (perm.getName().equals("setPolicy") || perm.getName().equals("setSecurityManager"))
                    throw new SecurityException(perm.toString());

                // Enforce permissions for runtime or security purposes, ignore everything else.
                if (perm.getName().startsWith("loadLibrary.") ||
                        perm.getName().equals("createClassLoader")) super.checkPermission(perm);
            }
        });
    }

}
