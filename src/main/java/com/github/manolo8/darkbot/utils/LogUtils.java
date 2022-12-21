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
import java.nio.charset.Charset;
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
            OutputStream fileLogger = new FileOutputStream("logs/" + START_TIME + ".log");

            System.setOut(createPrintStream(new FileOutputStream(FileDescriptor.out), fileLogger));
            System.setErr(createPrintStream(new FileOutputStream(FileDescriptor.err), fileLogger));
        } catch (FileNotFoundException e) {
            System.out.println("Failed to redirect logs, file not found: " + "logs/" + START_TIME + ".log");
            e.printStackTrace();
        }
    }

    private static PrintStream createPrintStream(FileOutputStream multi, OutputStream fileLogger) {
        OutputStream multiOut = new MultiOutputStream(multi, fileLogger);
        OutputStream bufferedOut = new BufferedOutputStream(multiOut, 128);

        try {
            if (System.getProperty("file.encoding").equals(Charset.defaultCharset().toString()))
                return new PrintStreamWithDate(bufferedOut, "UTF-8");
            else
                return new PrintStreamWithDate(bufferedOut);
        } catch (UnsupportedEncodingException e) {
            System.out.println("UTF-8 not supported");
            return new PrintStreamWithDate(bufferedOut);
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

        public PrintStreamWithDate(OutputStream downstream, String encoding) throws UnsupportedEncodingException {
            super(downstream, true, encoding);
        }

        public PrintStreamWithDate(OutputStream downstream) {
            super(downstream, true);
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

        public MultiOutputStream(OutputStream... outputStreams) {
            this.outputStreams = outputStreams;
        }

        @Override
        public void write(int b) throws IOException {
            try {
                for (OutputStream out : outputStreams)
                    out.write(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void write(@NotNull byte[] b) throws IOException {
            try {
                for (OutputStream out : outputStreams)
                    out.write(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) throws IOException {
            try {
                for (OutputStream out : outputStreams)
                    out.write(b, off, len);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void flush() {
            try {
                for (OutputStream out : outputStreams)
                    out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() throws IOException {
            try {
                for (OutputStream out : outputStreams)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
