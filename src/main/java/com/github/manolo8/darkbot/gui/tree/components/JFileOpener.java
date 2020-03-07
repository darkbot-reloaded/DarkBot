package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.Strings;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class JFileOpener extends JButton implements OptionEditor {

    private JFileChooser fc;

    private ConfigField field;

    public JFileOpener() {
        putClientProperty("JButton.buttonType", "square");
        addActionListener(e -> {
            if (fc == null) {
                fc = new JFileChooser(new File(".")) {
                    @Override
                    protected JDialog createDialog(Component parent) throws HeadlessException {
                        JDialog dialog = super.createDialog(parent);
                        dialog.setAlwaysOnTop(true);
                        return dialog;
                    }
                };
            }
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                field.set(fc.getSelectedFile().getAbsolutePath());
                setText(Strings.fileName(field.get()));
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = field;
        setText(Strings.fileName(field.get()));
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 16, 0));
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
