package com.github.manolo8.darkbot.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.github.manolo8.darkbot.gui.utils.Popups.showMessageSync;

public class APIErrors {


    private final String RUNTIME_URL  = "https://darkbot.eu/downloads/Runtimes4DarkBot.exe";
    private final String RUNTIME_LINK = "Download link: <a href=\"#\">" + RUNTIME_URL;

    JPanel pnl = new JPanel();

    private void displayAndExit() {
        JOptionPane options = new JOptionPane(this.pnl,
                JOptionPane.ERROR_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null, new Object[]{}, null
        );
        showMessageSync("Error", options);
        System.exit(0);
    }

    public void displayUnsupportedJavaError() {
        JLabel unSuppJava    = new JLabel("<html>" + I18n.get("api.errors.unsupported_java"));
        JLabel clickableLink = new JLabel("<html>" + RUNTIME_LINK);
        clickableLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clickableLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(RUNTIME_URL));
                } catch (URISyntaxException | IOException ignored) {}
            }
        });
        this.pnl.setLayout(new GridLayout(2, 1));
        this.pnl.add(unSuppJava);
        this.pnl.add(clickableLink);
        this.displayAndExit();
    }

    public void displayInvalidPathError(String path) {
        String invalidPathMsg  = "<html>" + I18n.get("api.errors.non_ansi_path") + "<br/><br/>";
        String formattedPath   = this.formatPath(path);
        pnl.add(new JLabel(invalidPathMsg + formattedPath));
        this.displayAndExit();
    }

    public String formatPath(String str) {
        String[] folderNames = str.split("\\\\");
        String charPointers  = str
                .replaceAll("[\\x00-\\x7F]", "&nbsp")
                .replaceAll("[^\\x00-\\x7F]", "^");
        StringBuilder path = new StringBuilder("<p style=\"font-family: Consolas\">");
        for (String name : folderNames) {
            path.append(!name.matches("[\\x00-\\x7F]+")
                    ? "<font color=\"#ff7d7d\">" + name + "/</font>"
                    : name + '/'
            );
        }
        return path + "<br/>" + charPointers;
    }
}
