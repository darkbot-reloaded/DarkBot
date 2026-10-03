package com.github.manolo8.darkbot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

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
        if (closeStream) {
            try (input) {
                return input.readAllBytes();
            }
        } else return input.readAllBytes();
    }

    /**
     * Wraps the stream in a {@link GZIPInputStream} if it starts with the gzip magic bytes.
     * Response may be gzip-compressed even without a Content-Encoding header,
     * and {@link java.net.HttpURLConnection} doesn't decompress it.
     *
     * @see eu.darkbot.util.http.Http#getInputStream()
     */
    public static InputStream unwrapGzip(InputStream input) throws IOException {
        PushbackInputStream in = new PushbackInputStream(input, 2);

        byte[] header = in.readNBytes(2);
        in.unread(header);

        if (header.length == 2 && header[0] == (byte) 0x1f && header[1] == (byte) 0x8b) {
            return new GZIPInputStream(in);
        }
        return in;
    }
}
