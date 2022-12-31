package com.github.manolo8.darkbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.ui.FlatNativeWindowBorder;
import com.formdev.flatlaf.util.SystemInfo;
import com.github.manolo8.darkbot.utils.LibSetup;
import com.github.manolo8.darkbot.utils.LogUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.util.Popups;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;

public class Bot {

    public static void main(String[] args) throws IOException {
        // You can enable hardware acceleration via adding jvm arg: -Dsun.java2d.opengl=True
        try {
            setupSecurityPolicy();
        } catch (Exception e) {
            System.out.println("Failed to setup security policy:");
            e.printStackTrace();
        }

        LogUtils.setupLogOutput();

        try {
            UIManager.getFont("Label.font"); // Prevents a linux crash

            // Set no padding when icon is removed
            UIManager.put("TitlePane.noIconLeftGap", 0);
            UIManager.put("OptionPane.showIcon", true);

            // enable custom window decorations - need on w7 also
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);

            // Load necessary native libraries
            FlatNativeWindowBorder.isSupported();

            UIManager.setLookAndFeel(new DarkLaf());
            UIManager.put("Button.arc", 0);
            UIManager.put("Component.arc", 0);
            UIManager.put("Button.default.boldText", false);
            UIManager.put("Table.cellFocusColor", new Color(0, 0, 0, 160));

            FlatInspector.install( "ctrl shift alt X" );
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        LibSetup.setupLibraries();
        StartupParams params = new StartupParams(args);

        checkJavaVersion(params);

        System.out.println("Starting DarkBot " + Main.VERSION);
        //noinspection ThrowableNotThrown
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                new Throwable("DarkBot shutdown peacefully!").printStackTrace()));

        SwingUtilities.invokeLater(() -> new Main(params));
    }

    private static void checkJavaVersion(StartupParams params) {
        if (params.has(StartupParams.LaunchArg.NO_WARN)) return;
        String java = System.getProperty("java.version");

        if (!java.startsWith("11.") && !java.startsWith("17.") && !java.equals("17")) {
            Popups.showMessageSync("Unsupported java version", new JOptionPane(
                    "You're currently using java version " + java + "\n" +
                    "This version is unsupported and may stop working on future bot releases.\n" +
                    "Please update to java 11 or java 17 to continue using future releases.",
                    JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION));
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

            @Override
            public PermissionCollection getPermissions(CodeSource codesource) {
                return new Permissions();
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

    public static class DarkLaf extends FlatDarkLaf {

        // support windows 7 too
        @Override
        public boolean getSupportsWindowDecorations() {
            if (SystemInfo.isProjector || SystemInfo.isWebswing || SystemInfo.isWinPE)
                return false;

            // return true if native border isn't supported
            return !(SystemInfo.isWindows_10_orLater && FlatNativeWindowBorder.isSupported());
        }
    }
}
