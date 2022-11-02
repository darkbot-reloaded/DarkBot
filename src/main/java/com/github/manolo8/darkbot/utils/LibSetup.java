package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.http.Http;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class LibSetup {

    private static final String BASE_URL = "https://gist.github.com/Pablete1234/2e43458bb3b644e16d146969069b1548/raw/";
    private static final Type LIB_LIST_TYPE = new TypeToken<Map<String, Lib>>(){}.getType();

    private static Map<String, Lib> libraries;

    public static class Lib {
        public String path;
        public String sha256;
        public Set<String> altSha256;
        public String download;
        public boolean auto;
    }

    public static void setupLibraries() {
        try {
            libraries = Http.create(BASE_URL + "/libs.json")
                    .consumeInputStream(is -> Main.GSON.fromJson(IOUtils.read(is), LIB_LIST_TYPE));
        } catch (Exception e) {
            System.out.println("Failed to download libraries file, this is safe to ignore if your libs are up-to-date");
            e.printStackTrace();
            return;
        }

        for (Lib lib : libraries.values()) {
            if (lib.auto) downloadLib(lib, null);
        }
    }

    private static Lib getLib(String path) {
        return libraries != null ? libraries.get(path) : null;
    }

    public static boolean downloadLib(Path path) {
        return downloadLib(path.getFileName().toString(), path);
    }

    public static boolean downloadLib(String path) {
        return downloadLib(getLib(path));
    }

    public static boolean downloadLib(String path, @Nullable Path libPath) {
        return downloadLib(getLib(path), libPath);
    }

    public static boolean downloadLib(Lib lib) {
        return downloadLib(lib, null);
    }

    public static boolean downloadLib(Lib lib, @Nullable Path libPath) {
        if (lib == null) return false;
        if (libPath == null) libPath = Paths.get(lib.path);

        if (Files.exists(libPath)) {
            try {
                String sha = FileUtils.calcSHA256(libPath);
                if (Objects.equals(sha, lib.sha256) || (lib.altSha256 != null && lib.altSha256.contains(sha))) return false;
            } catch (IOException e) {
                System.out.println("Exception checking library file SHA");
                e.printStackTrace();
            }
        } else {
            FileUtils.ensureDirectoryExists(libPath.getParent());
        }
        System.out.println("Downloading missing or outdated library file: " + libPath);

        try (InputStream is = new URL(lib.download).openConnection().getInputStream()) {
            Files.copy(is, libPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to download library file: " + libPath + " from " + lib.download);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Simple utility method to generate sha256 hashes
     */
    public static void main(String[] args) throws IOException {
        Path libFolder = Paths.get("lib");
        if (!Files.exists(libFolder) || !Files.isDirectory(libFolder)) {
            throw new RuntimeException("Cant create a libraries file without a lib folder");
        }

        for (Path path : Files.walk(libFolder, 1).collect(Collectors.toList())) {
            if (!Files.isRegularFile(path)) continue;
            System.out.format("%-20s\t%s\n", path.getFileName(), FileUtils.calcSHA256(path));
        }
    }

}
