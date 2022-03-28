package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.ImageWrapper;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImagePicker extends JButton implements OptionEditor<ImageWrapper> {
    private JFileChooser fc;
    private ImageWrapper image;

    public ImagePicker() {
        addActionListener(e -> {
            if (fc == null) {
                String path = image == null ? "." : image.getPath();
                fc = new JFileChooser(new File(path)) {
                    @Override
                    protected JDialog createDialog(Component parent) throws HeadlessException {
                        JDialog dialog = super.createDialog(parent);
                        dialog.setAlwaysOnTop(true);
                        return dialog;
                    }
                };
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Images Files", "png", "jpeg", "jpg", "gif");
                fc.setFileFilter(filter);
                fc.setAcceptAllFileFilterUsed(false);
            }

            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                image.setPath(fc.getSelectedFile().getAbsolutePath());
                setEditing(image);
            }
        });

    }

    @Override
    public void setText(String text) {
        int sepIdx;
        if (text != null && text.length() > 30 && (sepIdx = text.indexOf(File.separator, text.length() - 30)) != -1) {
            super.setText(".." + text.substring(sepIdx));
        } else {
            super.setText(text);
        }

        setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 32, 0));
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<ImageWrapper> configSetting) {
        setEditing(configSetting.getValue());
        return this;
    }

    private void setEditing(ImageWrapper image) {
        this.image = image == null ? new ImageWrapper() : image;
        setText(image.getPath());
    }

    @Override
    public ImageWrapper getEditorValue() {
        return image;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
