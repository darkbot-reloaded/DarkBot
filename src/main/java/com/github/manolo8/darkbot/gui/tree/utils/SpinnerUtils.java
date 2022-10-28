package com.github.manolo8.darkbot.gui.tree.utils;

import javax.swing.*;
import java.text.ParseException;

public class SpinnerUtils {

    public static void setError(JSpinner spinner, boolean error) {
        spinner.putClientProperty("JComponent.outline", error ? "error" : null);
        spinner.repaint();
    }

    public static boolean tryStopEditing(JSpinner spinner) {
        try {
            spinner.commitEdit();
            return true;
        } catch (ParseException e) {
            setError(spinner, true);
            return false;
        }
    }

}
