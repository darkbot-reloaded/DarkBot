package com.github.manolo8.darkbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.ui.FlatNativeWindowBorder;
import com.formdev.flatlaf.util.SystemInfo;
import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.extensions.plugins.PluginClassLoader;
import com.github.manolo8.darkbot.utils.LibSetup;
import com.github.manolo8.darkbot.utils.LogUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.util.http.Http;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
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

            // Not recommended to keep for production
            //FlatInspector.install("ctrl shift alt X");
            //FlatUIDefaultsInspector.install("ctrl shift alt I");
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        Http.setGson(BackpageManager.GSON);
        LibSetup.setupLibraries();
        StartupParams params = new StartupParams(args);

        System.out.println("Starting DarkBot " + Main.VERSION);
        //noinspection ThrowableNotThrown
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                new Throwable("DarkBot shutdown peacefully!").printStackTrace()));

        SwingUtilities.invokeLater(() -> new Main(params));
    }

    private static void setupSecurityPolicy() {
        Policy.setPolicy(new Policy() {
            @Override
            public PermissionCollection getPermissions(ProtectionDomain domain) {
                // Externally loaded classes get no permissions
                if (domain.getClassLoader() instanceof PluginClassLoader) return new Permissions();

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
