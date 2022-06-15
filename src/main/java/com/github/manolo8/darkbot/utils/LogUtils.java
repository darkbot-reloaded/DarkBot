package com.github.manolo8.darkbot.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class LogUtils {
    public static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
    public static final DateTimeFormatter FILENAME_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss_SSS");
    public static final Path LOG_FOLDER = Paths.get("logs");
    public static final String START_TIME = LocalDateTime.now().format(FILENAME_DATE);

    public static void setOutputToFile() {
        if (!Files.exists(LOG_FOLDER)) createFolder();
        else removeOld();

        try {
            PrintStream output = getLogger();
            System.setOut(output);
            System.setErr(output);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println("Failed to redirect logs, file not found:");
            e.printStackTrace();
        }
    }

    public static OutputStream createLogFile(String filename) {
        String fileName = "logs/" + filename + ".log";

        try {
            return new FileOutputStream(fileName, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PrintStream getLogger() throws FileNotFoundException, UnsupportedEncodingException {
        return new PrintStreamWithDate("logs/" + START_TIME + ".log");
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
        public PrintStreamWithDate(String logfile) throws FileNotFoundException, UnsupportedEncodingException {
            super(logfile, "UTF-8");
        }

        @Override
        public void println(String string) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            string = "[" + LocalDateTime.now().format(LOG_DATE)
                    + (stack.length > 2 ? " | " + stack[2] : "")
                    + "] " + string;

            super.println(string);
        }
    }
}
