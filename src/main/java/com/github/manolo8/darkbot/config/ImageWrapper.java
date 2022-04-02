package com.github.manolo8.darkbot.config;

import java.awt.Image;
import javax.swing.ImageIcon;

public class ImageWrapper {
    private String path;
    private transient Image image;
    private transient boolean loaded;

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
        this.loaded = false;
    }

    public Image getImage() {
        if (loaded) return image;
        loaded = true;
        try {
            return image = new ImageIcon(this.path).getImage();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
