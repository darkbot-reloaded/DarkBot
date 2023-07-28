package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.gui.utils.Popups;

import javax.swing.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

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

            AtomicBoolean accepted = new AtomicBoolean(false);

            JButton run = new JButton("Run");
            run.addActionListener(a -> {
                if (isUserTriggered()) accepted.set(true);
                SwingUtilities.getWindowAncestor(run).setVisible(false);
            });

            Popups.of("Run external program?", new Object[]{message, alwaysAllow}, JOptionPane.WARNING_MESSAGE)
                    .alwaysOnTop(true)
                    .options(run, "Cancel")
                    .defaultButton(run)
                    .showSync();

            if (!accepted.get()) return false;
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
