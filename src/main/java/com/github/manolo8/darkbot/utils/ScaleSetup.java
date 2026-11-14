package com.github.manolo8.darkbot.utils;

import javax.swing.ImageIcon;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ScaleSetup {

    /**
     * On linux java only detects the desktop scale from gnome gsettings or GDK_SCALE,
     * so on desktops like cinnamon (e.g. fractional "scale content down" mode) it renders at 1x,
     * while flatlaf scales only fonts based on Xft.dpi, mixing huge text with tiny components.
     * Takes the integer scale from Xft.dpi instead, must be called before AWT is initialized.
     * Can be overridden with -Dsun.java2d.uiScale=N
     */
    public static void setupLinuxScale() {
        if (!OSUtil.isLinux() || isEmpty(System.getenv("DISPLAY"))) return;
        if (System.getProperty("sun.java2d.uiScale") != null
                || "false".equals(System.getProperty("sun.java2d.uiScale.enabled"))
                || !isEmpty(System.getenv("GDK_SCALE"))
                || !isEmpty(System.getenv("J2D_UISCALE"))) return;

        int dpi = readXftDpi();
        int scale = dpi / 96;
        if (scale > 1) {
            System.setProperty("sun.java2d.uiScale", String.valueOf(scale));
            System.out.println("Using UI scale " + scale + " (Xft.dpi " + dpi + ")");
        }
    }

    /**
     * Java on linux drops window icons when their size (capped to 128px) multiplied by the UI scale
     * exceeds the _NET_WM_ICON length limit (e.g. 128px at 2x), showing the default java icon instead.
     * Returns a multi-resolution image with a logical size that fits, keeping a full-resolution variant.
     */
    public static Image getWindowIcon(Image icon) {
        if (!OSUtil.isLinux() || GraphicsEnvironment.isHeadless()) return icon;

        double scale = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration().getDefaultTransform().getScaleX();
        int size = Math.min(icon.getWidth(null), 128);
        if (size * scale < 256) return icon;

        while (size * scale >= 256) size /= 2;
        return new BaseMultiResolutionImage(getScaledImage(icon, size),
                getScaledImage(icon, (int) Math.round(size * scale)));
    }

    private static Image getScaledImage(Image image, int width) {
        return new ImageIcon(image.getScaledInstance(width, -1, Image.SCALE_SMOOTH)).getImage();
    }

    private static int readXftDpi() {
        try {
            Process process = new ProcessBuilder("xrdb", "-query").redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Xft.dpi:"))
                        return (int) Double.parseDouble(line.substring("Xft.dpi:".length()).trim());
                }
            } finally {
                process.destroy();
            }
        } catch (Exception e) {
            System.out.println("Failed to read Xft.dpi: " + e);
        }
        return -1;
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
