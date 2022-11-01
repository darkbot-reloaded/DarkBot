package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.core.manager.HeroManager;

import javax.swing.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class RuntimeUtil {

    private static final Set<String> allowed = new HashSet<>();

    public static boolean execute(String command) throws IOException {
        if (command.length() == 0)
            throw new IllegalArgumentException("Empty command");

        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken();

        return execute(cmdarray);
    }

    public static boolean execute(String... command) throws IOException {
        if (command == null || command.length == 0)
            throw new IllegalArgumentException("Empty command");

        if (!isUserTriggered() && !allowed.contains(command[0])) {
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
            if (alwaysAllow.isSelected()) allowed.add(command[0]);
        }

        Runtime.getRuntime().exec(command);
        return true;
    }

    private static boolean isUserTriggered() {
        if (!SwingUtilities.isEventDispatchThread()) return false;

        boolean isUser = false;
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();

            isUser |= className.equals("javax.swing.AbstractButton") && element.getMethodName().equals("doClick");
            isUser &= className.startsWith("java.awt.") ||
                    className.startsWith("javax.swing.") ||
                    className.startsWith("java.security.");
        }
        return isUser;
    }

}
