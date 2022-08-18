package com.github.manolo8.darkbot.gui.utils.window;

import javax.swing.*;
import java.awt.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class FileChooserUtil {
    public static JFileChooser getChooser(String path) {
        return getChooser(path, null, false);
    }

    public static JFileChooser getChooser(String path, FileNameExtensionFilter filter, boolean acceptAllFileFilter) {
        if (path == null) path = new File(System.getProperty("user.dir")).getAbsolutePath();
        JFileChooser fc = new JFileChooser(path) {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                dialog.setAlwaysOnTop(true);
                return dialog;
            }
        };

        if (filter != null) fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(acceptAllFileFilter);
        return fc;
    }
}
