package com.github.manolo8.darkbot.utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
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
import java.util.regex.Pattern;

public class LogUtils {
    public static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
    public static final DateTimeFormatter FILENAME_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss_SSS");
    public static final Path LOG_FOLDER = Paths.get("logs");
    public static final String START_TIME = LocalDateTime.now().format(FILENAME_DATE);
    private static final Pattern SID_PATTERN = Pattern.compile("sid=[a-z0-9]{32}");

    public static void setupLogOutput() {
        if (!Files.exists(LOG_FOLDER)) createFolder();
        else removeOld();

        OutputStream fileLogger = createLogFile(START_TIME);
        if (fileLogger == null) return;

        System.setOut(createPrintStream(System.out, fileLogger, System.getProperty("sun.stdout.encoding")));
        System.setErr(createPrintStream(System.err, fileLogger, System.getProperty("sun.stderr.encoding")));

    }

    private static PrintStream createPrintStream(PrintStream out, OutputStream fileLogger, String enc) {
        OutputStream bufferedOut = new BufferedOutputStream(new MultiOutputStream(out, fileLogger), 128);

        if (enc != null) {
            try {
                return new PrintStreamWithDate(bufferedOut, enc);
            } catch (UnsupportedEncodingException ignore) {}
        }
        return new PrintStreamWithDate(bufferedOut);
    }

    public static OutputStream createLogFile(String filename) {
        String fileName = "logs/" + filename + ".log";

        try {
            return new FileOutputStream(fileName, true);
        } catch (FileNotFoundException e) {
            System.out.println("Failed to create log file");
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
            synchronized (this) {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                String path = null;
                if (stack.length > 2) {
                    path = stack[2].toString()
                            .replace("com.github.manolo8.darkbot", "db")
                            .replace("eu.darkbot.api", "api");
                    path = SID_PATTERN.matcher(path).replaceAll("sid=...");
                }

                string = "[" + LocalDateTime.now().format(LOG_DATE)
                        + (path != null ? " | " + path : "")
                        + "] " + string;

                super.println(string);
            }
        }

        @Override
        public void println(Object object) {
            synchronized (this) {
                super.println(SID_PATTERN.matcher(String.valueOf(object)).replaceAll("sid=..."));
            }
        }
    }

    public static class MultiOutputStream extends FakeMultiOutputStream {

        OutputStream second;

        public MultiOutputStream(OutputStream out, OutputStream second) {
            super(out);
            this.second = second;
        }

        @Override
        public synchronized void write(int b) throws IOException {
            super.write(b);
            second.write(b);
        }

        @Override
        public synchronized void write(byte @NotNull [] b) throws IOException {
            super.write(b);
            second.write(b);
        }

        @Override
        public synchronized void write(byte @NotNull [] b, int off, int len) throws IOException {
            super.write(b, off, len);
            second.write(b, off, len);
        }

        @Override
        public synchronized void flush() throws IOException {
            super.flush();
            second.flush();
        }

        @Override
        public synchronized void close() throws IOException {
            try {
                super.close();
            } finally {
                second.close();
            }
        }
    }

    public static class FakeMultiOutputStream extends FilterOutputStream {

        public FakeMultiOutputStream(final OutputStream proxy) {
            super(proxy);
        }

        @SuppressWarnings("unused") // Possibly thrown from subclasses.
        protected void afterWrite(final int n) throws IOException {
            // noop
        }

        @SuppressWarnings("unused") // Possibly thrown from subclasses.
        protected void beforeWrite(final int n) throws IOException {
            // noop
        }

        @Override
        public void close() throws IOException {
            close(out);
        }

        @Override
        public void flush() throws IOException {
            try {
                out.flush();
            } catch (final IOException e) {
                handleIOException(e);
            }
        }

        protected void handleIOException(final IOException e) throws IOException {
            throw e;
        }

        @Override
        public void write(final byte @NotNull [] bts) throws IOException {
            try {
                final int len = length(bts);
                beforeWrite(len);
                out.write(bts);
                afterWrite(len);
            } catch (final IOException e) {
                handleIOException(e);
            }
        }

        @Override
        public void write(final byte @NotNull [] bts, final int st, final int end) throws IOException {
            try {
                beforeWrite(end);
                out.write(bts, st, end);
                afterWrite(end);
            } catch (final IOException e) {
                handleIOException(e);
            }
        }

        @Override
        public void write(final int idx) throws IOException {
            try {
                beforeWrite(1);
                out.write(idx);
                afterWrite(1);
            } catch (final IOException e) {
                handleIOException(e);
            }
        }

        public static void close(final Closeable closeable) throws IOException {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public static int length(final byte[] array) {
            return array == null ? 0 : array.length;
        }
    }
}