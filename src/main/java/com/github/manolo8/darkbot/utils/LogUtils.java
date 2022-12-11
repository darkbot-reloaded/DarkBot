package com.github.manolo8.darkbot.utils;

import org.jetbrains.annotations.NotNull;

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
            FileOutputStream fileOut = new FileOutputStream("logs/" + START_TIME + ".log");

            MultiOutputStream multiOut = new MultiOutputStream(System.out, fileOut);
            MultiOutputStream multiErr = new MultiOutputStream(System.err, fileOut);

            PrintStream stdout = new PrintStream(multiOut, true, "UTF-8");
            PrintStream stderr = new PrintStream(multiErr, true, "UTF-8");

            System.setOut(stdout);
            System.setErr(stderr);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
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
            String path = null;
            if (stack.length > 2) {
                path = stack[2].toString();
                path = path.replace("com.github.manolo8.darkbot", "db");
                path = path.replace("eu.darkbot.api", "api");
            }

            string = "[" + LocalDateTime.now().format(LOG_DATE)
                    + (path != null ? " | " + path : "")
                    + "] " + string;

            super.println(string);
        }
    }

    public static class MultiOutputStream extends OutputStream {
        private final OutputStream[] outputStreams;
        int count = 0;

        public MultiOutputStream(OutputStream... outputStreams) {
            this.outputStreams = outputStreams;
        }

        @Override
        public void write(int b) throws IOException {
            for (OutputStream out : outputStreams)
                out.write(b);
        }

        @Override
        public void write(@NotNull byte [] b) throws IOException {
            for (OutputStream out : outputStreams)
                out.write(b);
        }

        @Override
        public void write(@NotNull byte [] b, int off, int len) throws IOException {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            String path = null;
            if (stack.length > 2) {
                try {
                    path = stack[10].toString();
                    path = path.replace("com.github.manolo8.darkbot", "db");
                    path = path.replace("eu.darkbot.api", "api");
                } catch (java.lang.IndexOutOfBoundsException e) {
                    path = stack[9].toString();
                    path = path.replace("com.github.manolo8.darkbot", "db");
                    path = path.replace("eu.darkbot.api", "api");
                }
            }

            String string = "[" + LocalDateTime.now().format(LOG_DATE)
                    + (path != null ? " | " + path : "")
                    + "] ";

            for (OutputStream out : outputStreams) {
                if (count == 1)
                    out.write(string.getBytes());
                out.write(b, off, len);
            }
            count++;
            if (count > 1)
                count = 0;
        }

        @Override
        public void flush() throws IOException {
            for (OutputStream out : outputStreams)
                out.flush();
        }

        @Override
        public void close() throws IOException {
            for (OutputStream out : outputStreams)
                out.close();
        }
    }
}
