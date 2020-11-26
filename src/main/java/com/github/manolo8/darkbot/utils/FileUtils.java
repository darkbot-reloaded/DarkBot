package com.github.manolo8.darkbot.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static void createDirectory(Path path) {
        if (Files.exists(path)) return;
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            System.err.println("Failed to create: " + path);
            e.printStackTrace();
        }
    }
}
