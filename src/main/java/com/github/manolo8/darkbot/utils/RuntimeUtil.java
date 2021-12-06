package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.core.manager.HeroManager;

import javax.swing.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RuntimeUtil {

    private static final Set<String> allowed = new HashSet<>();

    public static boolean execute(String... command) throws IOException {
        if (!allowed.contains(command[0])) {
            JCheckBox alwaysAllow = new JCheckBox("Always allow this command");

            String message = "A plugin is requesting to run the following program: \n" +
                    command[0] + "\n\n" +
                    "This can run anything, if you did not request it yourself, deny it.";

            int result = JOptionPane.showConfirmDialog(HeroManager.instance.main.getGui(),
                    new Object[]{message, alwaysAllow},
                    "Run external program?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result != JOptionPane.YES_OPTION) return false;
            allowed.add(command[0]);
        }

        Runtime.getRuntime().exec(command);
        return true;
    }

}
