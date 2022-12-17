package com.github.manolo8.darkbot.utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
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

    public static void setOutputToFileAndConsole() {
        if (!Files.exists(LOG_FOLDER)) createFolder();
        else removeOld();

        try {
            OutputStream consoleOut = new FileOutputStream(FileDescriptor.out);
            OutputStream consoleErr = new FileOutputStream(FileDescriptor.err);

            OutputStream file = new FileOutputStream("logs/" + START_TIME + ".log");

            OutputStream multiOut = new MultiOutputStream(consoleOut, file);
            OutputStream bufferedOut = new BufferedOutputStream(multiOut, 128);
            PrintStream printStreamOut = new PrintStreamWithDate(bufferedOut);

            OutputStream multiErr = new MultiOutputStream(consoleErr, file);
            OutputStream bufferedErr = new BufferedOutputStream(multiErr, 128);
            PrintStream printStreamErr = new PrintStreamWithDate(bufferedErr);

            System.setOut(printStreamOut);
            System.setErr(printStreamErr);
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

        public PrintStreamWithDate(OutputStream downstream) throws FileNotFoundException, UnsupportedEncodingException {
            super(downstream, true, "UTF-8");
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
        public void write(@NotNull byte[] b) throws IOException {
            for (OutputStream out : outputStreams)
                out.write(b);
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) throws IOException {
            for (OutputStream out : outputStreams)
                out.write(b, off, len);
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
