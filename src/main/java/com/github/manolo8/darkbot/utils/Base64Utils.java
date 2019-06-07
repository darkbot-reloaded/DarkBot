package com.github.manolo8.darkbot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Utils{

    public static String base64Decode(InputStream input) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        StringBuilder responseb = new StringBuilder();
        String currentLine;

        while ((currentLine = in.readLine()) != null){
            responseb.append(currentLine);
        }

        input.close();
        in.close();

        return base64Decode(responseb.toString());
    }

    public static String base64Decode(String text) {
        return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    }

    public static String base64Encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}
