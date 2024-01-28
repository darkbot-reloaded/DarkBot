package com.github.manolo8.darkbot.gui.tree.utils;

import javax.swing.*;
import java.text.ParseException;

public class SpinnerUtils {
    private static final JToolTip TOOL_TIP = new JToolTip();
    private static final JPopupMenu POPUP = new JPopupMenu();

    static {
        POPUP.add(TOOL_TIP);
        POPUP.setBorder(BorderFactory.createEmptyBorder());
    }

    public static void setError(JSpinner spinner, boolean error) {
        Object outline = spinner.getClientProperty("JComponent.outline");
        if (error && outline == null) {
            spinner.putClientProperty("JComponent.outline", "error");
            spinner.repaint();
        } else if (!error && outline != null) {
            spinner.putClientProperty("JComponent.outline", null);
            spinner.repaint();
        }
    }

    public static boolean tryStopEditing(JSpinner spinner) {
        try {
            spinner.commitEdit();
            setError(spinner, false);
            return true;
        } catch (ParseException e) {
            SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();

            TOOL_TIP.setTipText(e.getMessage() + "\n"
                    + "From " + model.getMinimum() + " to " + model.getMaximum() + " (step " + model.getStepSize() + ")");
            POPUP.show(spinner, 0, spinner.getHeight());
            setError(spinner, true);
            return false;
        }
    }

}
