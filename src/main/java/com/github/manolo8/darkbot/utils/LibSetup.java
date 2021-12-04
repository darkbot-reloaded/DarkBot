package com.github.manolo8.darkbot.utils;


import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.http.Http;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LibSetup {

    private static final String BASE_URL = "https://gist.github.com/Pablete1234/25e057781868db1397f1b3a8414203e1/raw/";
    private static final String LIBS_FILE = "libs.json";

    private static final Type LIB_LIST_TYPE = new TypeToken<List<Lib>>(){}.getType();

    private static class Lib {
        private String path;
        private String sha256;
        private String download;
        private boolean keep;
    }

    public static void setupLibraries() {
        List<Lib> libraries;
        try {
            libraries = Http.create(BASE_URL + "/libs.json")
                    .consumeInputStream(is -> Main.GSON.fromJson(IOUtils.read(is), LIB_LIST_TYPE));
        } catch (Exception e) {
            System.out.println("Failed to download libraries file, this is safe to ignore if your libs are up-to-date");
            e.printStackTrace();
            return;
        }

        for (Lib lib : libraries) {
            Path libPath = Paths.get(lib.path);

            if (Files.exists(libPath)) {
                if (lib.keep) continue;
                try {
                    if (Objects.equals(FileUtils.calcSHA256(libPath), lib.sha256)) continue;
                } catch (IOException e) {
                    System.out.println("Exception checking library file SHA");
                    e.printStackTrace();
                }
            } else {
                FileUtils.ensureDirectoryExists(libPath.getParent());
            }
            System.out.println("Downloading missing or outdated library file: " + lib.path);

            try (InputStream is = new URL(lib.download).openConnection().getInputStream()) {
                Files.copy(is, libPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Failed to download library file: " + lib.path + " from " + lib.download);
                e.printStackTrace();
            }
        }
    }

    /**
     * Simple utility method to generate the libs file
     */

    public static void main(String[] args) throws IOException {
        List<Lib> libraries = new ArrayList<>();

        Path libFolder = Paths.get("lib"), libFile = libFolder.resolve(LIBS_FILE);
        if (!Files.exists(libFolder) || !Files.isDirectory(libFolder)) {
            throw new RuntimeException("Cant create a libraries file without a lib folder");
        }

        for (Path path : Files.walk(libFolder, 1).collect(Collectors.toList())) {
            if (!Files.isRegularFile(path)) continue;
            if (Files.isSameFile(path, libFile)) continue;
            Lib l = new Lib();
            l.path = path.toString().replace("\\", "/");
            l.sha256 = FileUtils.calcSHA256(path);
            l.download = BASE_URL + path.getFileName().toString();
            l.keep = false;
            libraries.add(l);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("lib", LIBS_FILE))) {
            Main.GSON.toJson(libraries, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
