package com.github.manolo8.darkbot.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtils {
    private static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FILENAME_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss_SSS");

    public static void setOutputToFile() throws FileNotFoundException {
        checkIfLogFolderExist();

        PrintStream output = getLogger();
        System.setOut(output);
        System.setErr(output);
    }

    private static PrintStream getLogger() throws FileNotFoundException {
        return new PrintStreamWithDate("logs/" + LocalDateTime.now().format(FILENAME_DATE) + ".log");
    }

    private static void checkIfLogFolderExist() {
        Path logsDirectory = Paths.get("logs");
        if (Files.exists(logsDirectory)) return;

        try {
            Files.createDirectory(logsDirectory);
        } catch (IOException e) {
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
