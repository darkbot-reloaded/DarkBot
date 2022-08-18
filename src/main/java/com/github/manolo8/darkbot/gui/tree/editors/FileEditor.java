package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.gui.utils.window.FileChooserUtil;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Type;

public class FileEditor extends JButton implements OptionEditor<File> {
    private JFileChooser fc;
    private File file;

    public FileEditor() {
        addActionListener(e -> {
            if (fc == null) fc = FileChooserUtil.getChooser(file.getPath());
            JTree tree = (JTree) SwingUtilities.getAncestorOfClass(JTree.class, FileEditor.this);
            TreePath path = tree.getEditingPath();
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                // FIXME: opening the file picker makes the tree lose focus, so cell can't save, meaning we have to force it.
                setEditing(fc.getSelectedFile());
                tree.getModel().valueForPathChanged(path, getEditorValue());
            }
        });
    }

    @Override
    public void setText(String text) {
        super.setText(Strings.shortFileName(text));
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

    public static class JsonAdapter implements JsonSerializer<File>, JsonDeserializer<File> {
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
