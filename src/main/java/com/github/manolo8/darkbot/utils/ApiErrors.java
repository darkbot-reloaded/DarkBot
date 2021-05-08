package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.gui.utils.Popups;
import com.sun.jna.Platform;

import javax.swing.*;
import java.nio.file.Paths;

public class ApiErrors {

    public static void displayException(int api, Throwable error) {
        Popups.showMessageAsync(
                "API failed to load",
                "<html>" + getMessage(api) + "<br><br>" +
                        "The API you had selected is not able to load.<br>" +
                        "The bot will start on no-operation API, change it in the settings and restart.<br><br>" +
                        "<strong>Exception:</strong><br>" + error.getLocalizedMessage(),
                JOptionPane.ERROR_MESSAGE);
    }

    private static String getMessage(int api) {
        if (api == 0 || api == 1)
            return "You're using an <strong>outdated API</strong>, use the recommended API!";
        if (api == 4)
            return "Error loading the no-op api, how did this happen?";
        if (api != 2)
            return "You're using an <strong>unsupported API</strong>, use the recommended API!";

        if (!Platform.is64Bit())
            return "The selected API requires 64-bit java, and it seems like you're not running a 64-bit JVM.";

        if (!Paths.get("lib", "DarkBoatApi.dll").toFile().exists())
            return "You do not have the required DLL in your lib folder.";

        String path = Paths.get("").toAbsolutePath().toString();
        String change = path.replaceAll("([^\\x00-\\x7F]+)", "<font color='red'><strong>$1</strong></font>");
        if (!path.equals(change))
            return "Your folder has non supported characters in the path, you must remove them:<br>  " + change + "";

        return "Unknown type of exception";
    }


}
