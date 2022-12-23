package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.config.types.suppliers.BrowserApi;
import com.github.manolo8.darkbot.gui.utils.Popups;

import javax.swing.*;
import java.nio.file.Paths;

public class ApiErrors {

    public static void displayException(BrowserApi api, Throwable error) {
        String message = getMessage(api);
        message = message == null ? "" : message + "<br><br>";
        Popups.of(
                        "API failed to load",
                        "<html>" + message +
                                "The API you had selected is not able to load.<br>" +
                                "The bot will start on no-operation API, change it in the settings and restart.<br><br>" +
                                "<strong>Exception:</strong><br>" + error.getLocalizedMessage(),
                        JOptionPane.ERROR_MESSAGE)
                .showAsync();
    }

    private static String getMessage(BrowserApi api) {
        if (api == null)
            return "You're using a <strong>removed API</strong>, use the recommended API!";
        if (api == BrowserApi.NO_OP_API)
            return "Error loading the no-op api, how did this happen?";
        if (api != OSUtil.getDefaultAPI())
            return "You're using an <strong>unsupported API</strong>, use the recommended API!";

        String bits = System.getProperty("sun.arch.data.model");
        if (bits == null || !bits.equals("64"))
            return "The selected API requires 64-bit java, and it seems like you're not running a 64-bit JVM.";

        if (!Paths.get("lib", "DarkBoatApi.dll").toFile().exists())
            return "You do not have the required DLL in your lib folder.";

        String path = Paths.get("").toAbsolutePath().toString();
        String change = path.replaceAll("([^\\x00-\\x7F]+)", "<font color='red'><strong>$1</strong></font>");
        if (!path.equals(change))
            return "Your folder has non supported characters in the path, you must remove them:<br>  " + change + "";

        return null;
    }


}
