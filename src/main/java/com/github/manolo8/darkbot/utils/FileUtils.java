package com.github.manolo8.darkbot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class FileUtils {
    private static final MessageDigest SHA_256_DIGEST;

    static {
        try {
            SHA_256_DIGEST = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm SHA-256 not available, are you running in a proper JVM?");
        }
    }

    public static void ensureDirectoryExists(Path path) {
        if (Files.exists(path)) return;
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            System.err.println("Failed to create: " + path);
            e.printStackTrace();
        }
    }

    public static Path createDirectories(Path path) throws IOException {
        if (Files.exists(path)) return path;
        return Files.createDirectories(path);
    }

    public static String calcSHA256(Path path) throws IOException {
        SHA_256_DIGEST.reset();
        try (InputStream input = Files.newInputStream(path)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                SHA_256_DIGEST.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return Base64Utils.encodeBytes(SHA_256_DIGEST.digest());
        }
    }

    public static List<String> readAllLines(Path path) {
        try {
            if (Files.exists(path)) return Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static void writeString(Path path, String text, StandardOpenOption... options) {
        try {
            Files.writeString(path, text, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
