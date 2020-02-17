package com.github.manolo8.darkbot.utils;

public class Base62 {

    public static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static final int BASE = ALPHABET.length();

    private Base62() {}

    public static String encode(int i) {
        StringBuilder sb = new StringBuilder("");
        if (i == 0) return "a";
        while (i > 0) i = encode(i, sb);
        return sb.reverse().toString();
    }

    private static int encode(int i, final StringBuilder sb) {
        int rem = i % BASE;
        sb.append(ALPHABET.charAt(rem));
        return i / BASE;
    }

    public static int decode(String str) {
        return decode(new StringBuilder(str).reverse().toString().toCharArray());
    }

    private static int decode(char[] chars) {
        int n = 0;
        for (int i = chars.length - 1; i >= 0; i--) {
            n += decode(ALPHABET.indexOf(chars[i]), i);
        }
        return n;
    }

    private static int decode(int n, int pow) {
        return n * (int) Math.pow(BASE, pow);
    }
}
