package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.google.gson.*;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Type;
import java.util.function.Consumer;

public class FileEditor extends JButton implements OptionEditor<File> {
    private JFileChooser fc;
    private File file;
    private final Consumer<JTree> stopEditing = JTree::stopEditing;

    public FileEditor() {
        setFocusable(false);
        addActionListener(e -> stopEditing.accept((JTree) SwingUtilities.getAncestorOfClass(JTree.class, FileEditor.this)));
        addActionListener(e -> {
            if (fc == null) {
                String path = file == null ? new File(System.getProperty("user.dir")).getAbsolutePath() : file.getPath();
                fc = new JFileChooser(path) {
                    @Override
                    protected JDialog createDialog(Component parent) throws HeadlessException {
                        JDialog dialog = super.createDialog(parent);
                        dialog.setAlwaysOnTop(true);
                        return dialog;
                    }
                };
            }
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) setEditing(fc.getSelectedFile());
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
    public JComponent getEditorComponent(ConfigSetting<File> configSetting) {
        setEditing(configSetting.getValue());
        return this;
    }

    private void setEditing(File file) {
        this.file = file;
        setText(this.file == null ? "None" : this.file.getName());
    }

    @Override
    public File getEditorValue() {
        return file;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    public static class JsonAdapter implements JsonSerializer<File>, JsonDeserializer<File>{
        @Override
        public File deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new File(jsonElement.getAsString());
        }

        @Override
        public JsonElement serialize(File file, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(file.getAbsolutePath());
        }
    }
}
