package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigManager;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigPicker extends JComboBox<String> {

    private Main main;

    public ConfigPicker() {
        addActionListener(a -> {
            if (main != null) main.setConfig((String) getSelectedItem());
        });
    }

    public void setup(Main main) {
        this.main = null; // Ensure no updates happen
        removeAllItems();
        addItem(ConfigManager.DEFAULT);
        try {
            Files.list(Paths.get(ConfigManager.CONFIG_FOLDER))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(n -> !n.endsWith(ConfigManager.BACKUP + ConfigManager.EXTENSION))
                    .filter(n -> n.endsWith(ConfigManager.EXTENSION))
                    .map(n -> n.substring(0, n.length() - ConfigManager.EXTENSION.length()))
                    .forEach(this::addItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSelectedItem(main.configManager.getConfigName());
        this.main = main;
    }

}
