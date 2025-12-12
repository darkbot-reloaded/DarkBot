package com.github.manolo8.darkbot.gui.utils;

import eu.darkbot.util.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class Strings {
    public static final DecimalFormat ONE_PLACE_FORMAT = new DecimalFormat("0.0");

    private static final Pattern SIMPLIFY_NAME_MATCHES = Pattern.compile("^[^\\d]+\\d{1,3}$");
    private static final Pattern SIMPLIFY_NAME_REPLACEMENT = Pattern.compile("\\d{1,3}$");

    public static String fileName(String path) {
        if (path == null || path.isEmpty()) return "-";
        int split = path.lastIndexOf(File.separatorChar);
        return split > 0 ? path.substring(split + 1) : path;
    }

    public static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String toTooltip(String str) {
        if (str != null && str.trim().isEmpty()) return null;
        return str;
    }

    public static String simplifyName(String name) {
        if (!SIMPLIFY_NAME_MATCHES.matcher(name).matches()) return name;
        return SIMPLIFY_NAME_REPLACEMENT.matcher(name).replaceAll(" *");
    }

    public static String fuzzyMatcher(String string) {
        return StringUtils.fuzzyNpcName(string);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static String shortFileName(String text) {
        int sepIdx;
        if (text != null && text.length() > 30 && (sepIdx = text.indexOf(File.separator, text.length() - 30)) != -1)
            return ".." + text.substring(sepIdx);
        else return text;
    }

    public static String exceptionToString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
