package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.ImageWrapper;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.gui.utils.window.FileChooserUtil;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;


public class ImagePicker extends JButton implements OptionEditor<ImageWrapper> {
    private JFileChooser fc;
    private ImageWrapper image;

    public ImagePicker() {
        addActionListener(e -> {
            if (fc == null)
                fc = FileChooserUtil.getChooser(image.getPath(),
                    new FileNameExtensionFilter("Images Files", "png", "jpeg", "jpg", "gif"),
                    false);
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                // FIXME: opening the file picker makes the tree lose focus, so cell save to existing object without possibility cancel changes.
                image.setPath(fc.getSelectedFile().getAbsolutePath());
                setEditing(image);
            }
        });

    }

    @Override
    public void setText(String text) {
        super.setText(Strings.shortFileName(text));
        setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 32, 0));
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<ImageWrapper> configSetting) {
        setEditing(configSetting.getValue());
        return this;
    }

    private void setEditing(ImageWrapper image) {
        this.image = image == null ? new ImageWrapper() : image;
        setText(this.image.getPath() == null ? "None" : this.image.getPath());
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
