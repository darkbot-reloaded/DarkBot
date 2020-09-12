package com.github.manolo8.darkbot.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IOUtils {

    public static void write(OutputStream output, String str) throws IOException {
        output.write(str.getBytes());
    }

    public static String read(InputStream input) throws IOException {
        return read(input, false);
    }

    public static String read(InputStream input, boolean closeStream) throws IOException {
        return new String(readByteArray(input, closeStream), StandardCharsets.UTF_8);
    }

    public static byte[] readByteArray(InputStream input, boolean closeStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        if (closeStream) input.close();

        return result.toByteArray();
    }
}
