package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.ImageWrapper;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import eu.darkbot.api.config.util.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.Strings;
import eu.darkbot.api.config.ConfigSetting;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class ImagePicker extends JButton implements OptionEditor<ImageWrapper> {
    private JFileChooser fc;
    private ImageWrapper image;

    public ImagePicker(){
        putClientProperty("JButton.buttonType", "square");
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
                setText(Strings.fileName(image.getPath()));
            }
        });

    }
    @Override
    public void setText(String text) {
        if(text != null && text.contains("\\") && text.length() > 20){
            super.setText(".."+text.substring(text.lastIndexOf("\\", text.length()-20)));
        }else{
            super.setText(text);
        }

        setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 32, 0));
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<ImageWrapper> configSetting) {
        setEditing(configSetting.getValue());
        return this;
    }

    private void setEditing(ImageWrapper image){
        this.image = image == null ? new ImageWrapper() : image; //do i need this, i dont think its needed
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
