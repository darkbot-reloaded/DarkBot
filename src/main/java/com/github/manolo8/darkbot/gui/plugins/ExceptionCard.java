package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.plugins.PluginLoadingException;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ExceptionCard extends JPanel {

    private static final Border ERROR_BORDER = BorderFactory.createLineBorder(UIUtils.RED);
    private static int ALPHA = 96 << 24;
    private static final Color ERROR_COLOR = new Color(UIUtils.RED.getRGB() + ALPHA, true);

    public ExceptionCard(PluginLoadingException exception) {
        super(new MigLayout("", "[grow]10px[]", ""));
        setBorder(ERROR_BORDER);
        setBackground(ERROR_COLOR);

        if (exception.getPlugin() != null) add(new JLabel(exception.getPlugin().getName()));
        JLabel label = new JLabel(exception.getMessage());
        if (exception.getCause() != null) {
            label.setToolTipText(exception.getCause().toString());
        }
        add(label);
    }

}
