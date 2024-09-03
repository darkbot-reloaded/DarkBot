package com.github.manolo8.darkbot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Utils {

    @Deprecated public static String base64Encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    @Deprecated public static String base64Decode(InputStream input) throws IOException {
        return base64Decode(IOUtils.read(input));
    }

    @Deprecated public static String base64Decode(String text) {
        return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    }

    public static String decode(InputStream input) throws IOException {
        return decode(IOUtils.read(input));
    }

    public static InputStream decodeStream(InputStream input) {
        return Base64.getDecoder().wrap(input);
    }

    public static String decode(String text) {
        return new String(decodeBytes(text), StandardCharsets.UTF_8);
    }

    public static byte[] decodeBytes(String text) {
        return Base64.getDecoder().decode(text);
    }

    public static String encode(String text) {
        return encodeBytes(text.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeBytes(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

}
