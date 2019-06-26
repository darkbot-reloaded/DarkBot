package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.Strings;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class JFileOpener extends JLabel implements OptionEditor {

    private final JFileChooser fc = new JFileChooser(new File(".")) {
        @Override
        protected JDialog createDialog(Component parent) throws HeadlessException {
            JDialog dialog = super.createDialog(parent);
            dialog.setAlwaysOnTop(true);
            return dialog;
        }
    };

    public JFileOpener() {
        this.setPreferredSize(new Dimension(1000, 0)); // Make it wide, otherwise text gets cut when updated
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        setText(Strings.fileName(field.get()));
        SwingUtilities.invokeLater(() -> {
            if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
            field.set(fc.getSelectedFile().getAbsolutePath());
            setText(Strings.fileName(field.get()));
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
