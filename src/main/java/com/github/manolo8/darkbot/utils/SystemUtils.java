package com.github.manolo8.darkbot.utils;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

public class SystemUtils {

    public static void openUrl(String url) {
        openUrl(URI.create(url));
    }

    public static void openUrl(URI url) {
        try {
            Desktop.getDesktop().browse(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toClipboard(String str) {
        if (str == null || str.isEmpty()) return;
        StringSelection selection = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

}
