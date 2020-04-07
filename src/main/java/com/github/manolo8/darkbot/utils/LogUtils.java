package com.github.manolo8.darkbot.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class LogUtils {
    private static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FILENAME_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss_SSS");
    private static final Path LOG_FOLDER = Paths.get("logs");

    public static void setOutputToFile() {
        if (!Files.exists(LOG_FOLDER)) createFolder();
        else removeOld();

        try {
            PrintStream output = getLogger();
            System.setOut(output);
            System.setErr(output);
        } catch (FileNotFoundException e) {
            System.out.println("Failed to redirect logs, file not found:");
            e.printStackTrace();
        }
    }

    private static PrintStream getLogger() throws FileNotFoundException {
        return new PrintStreamWithDate("logs/" + LocalDateTime.now().format(FILENAME_DATE) + ".log");
    }

    private static void createFolder() {
        try {
            Files.createDirectory(LOG_FOLDER);
        } catch (IOException e) {
            System.out.println("Failed to create log folder");
            e.printStackTrace();
        }
    }

    private static void removeOld() {
        try {
            Instant keep = Instant.now().minus(24, ChronoUnit.HOURS);
            Files.list(LOG_FOLDER)
                    .filter(p -> isBefore(p, keep))
                    .forEach(LogUtils::tryDelete);
        } catch (IOException e) {
            System.out.println("Failed to remove old logs");
            e.printStackTrace();
        }
    }

    private static boolean isBefore(Path path, Instant date) {
        try {
            return Files.getLastModifiedTime(path).toInstant().isBefore(date);
        } catch (IOException e) {
            System.out.println("Failed to determine log date, won't remove");
            e.printStackTrace();
            return false;
        }
    }

    private static void tryDelete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            System.out.println("Failed to delete log");
            e.printStackTrace();
        }
    }

    private static class PrintStreamWithDate extends PrintStream {
        public PrintStreamWithDate(String logfile) throws FileNotFoundException {
            super(logfile);
        }

        @Override
        public void println(String string) {
            super.println("[" + LocalDateTime.now().format(LOG_DATE) + "] " + string);
        }
    }
}
