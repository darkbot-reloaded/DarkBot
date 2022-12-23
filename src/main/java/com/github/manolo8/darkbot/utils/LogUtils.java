package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.utils.itf.ThrowingConsumer;
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
import java.util.regex.Pattern;

public class LogUtils {
    public static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
    public static final DateTimeFormatter FILENAME_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss_SSS");
    public static final Path LOG_FOLDER = Paths.get("logs");
    public static final String START_TIME = LocalDateTime.now().format(FILENAME_DATE);

    public static void setupLogOutput() {
        if (!Files.exists(LOG_FOLDER)) createFolder();
        else removeOld();

        OutputStream fileLogger = createLogFile(START_TIME);
        if (fileLogger == null) return;

        System.setOut(createPrintStream(FileDescriptor.out, fileLogger, System.getProperty("sun.stdout.encoding")));
        System.setErr(createPrintStream(FileDescriptor.err, fileLogger, System.getProperty("sun.stderr.encoding")));

    }

    private static PrintStream createPrintStream(FileDescriptor descriptor, OutputStream fileLogger, String enc) {
        OutputStream multiOut = new MultiOutputStream(new FileOutputStream(descriptor), fileLogger);
        OutputStream bufferedOut = new BufferedOutputStream(multiOut, 128);

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

    private static final Pattern SID_PATTERN = Pattern.compile("sid=[a-z0-9]{32}");

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

    public static class MultiOutputStream extends OutputStream {
        private final OutputStream[] outputStreams;

        public MultiOutputStream(OutputStream... outputStreams) {
            this.outputStreams = outputStreams;
        }

        @Override
        public void write(int b) throws IOException {
            redirect(out -> out.write(b));
        }

        @Override
        public void write(byte @NotNull [] b) throws IOException {
            redirect(out -> out.write(b));
        }

        @Override
        public void write(byte @NotNull [] b, int off, int len) throws IOException {
            redirect(out -> out.write(b, off, len));
        }

        @Override
        public void flush() throws IOException {
            redirect(OutputStream::flush);
        }

        @Override
        public void close() throws IOException {
            redirect(OutputStream::close);
        }

        private void redirect(ThrowingConsumer<OutputStream, IOException> consumer) throws IOException {
            IOException lastEx = null;
            for (OutputStream out : outputStreams) {
                try {
                    consumer.accept(out);
                } catch (IOException e) {
                    // If any previous exception exists, print it as we'll replace it.
                    if (lastEx != null)
                        lastEx.printStackTrace();
                    lastEx = e;
                }
            }
            // Throw whatever the last exception was, if any
            if (lastEx != null) throw lastEx;
        }

    }
}
