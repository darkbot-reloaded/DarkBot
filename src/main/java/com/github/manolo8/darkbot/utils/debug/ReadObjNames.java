package com.github.manolo8.darkbot.utils.debug;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Reads object names from memory.
 * Also reads strings on that address.
 */
public class ReadObjNames {
    private static final String FILE_NAME = "replacements.txt";
    private static final File REPLACEMENTS_FILE = getReplacementsFile();
    private static Map<String, String> replacements = new HashMap<>();
    private static long lastModified;

    @RequiredArgsConstructor
    enum Type {
        OBJ("\u001B[34m", 8),
        STR("\u001B[32m", 8),
        BOOL("\u001B[32m", 4),
        INT("\u001B[32m", 4),
        LNG("\u001B[32m", 8),
        DBL("\u001B[32m", 8);

        private final String ansiColor;
        private final int size;

        @Override
        public String toString() {
            return ansiColor + name();
        }
    }

    public static void of(long address) {
        of(address, 0x400);
    }

    public static void of(long address, int maxOffset) {
        updateReplacements();
        String result = API.readMemoryString(address, 0x10, 0x28, 0x90);
        System.out.println("\n==========[" + replacements.getOrDefault(result, result) + "]==========");

        for (int offset = 0; offset < maxOffset; offset++) {
            long addr = API.readMemoryLong(address + offset);
            Type type = null;

            if (!isInvalid(addr)) {
                type = printObjName(API.readMemoryLong(addr, 0x10, 0x28, 0x90), offset);
            }

            if (type == null) {
                type = findType(address, offset);
            }

            if (type != null) offset += (type.size - 1);
        }
    }

    private static boolean isInvalid(long address) {
        return (address < 0xFF00000000L || address > 0xF000000000000L);
    }

    private static Type findType(long address, int offset) {
        if ((offset % 4) != 0) return null;

        String strVal = API.readMemoryStringFallback(address + offset, null);
        if (strVal != null) {
            printStr( "\"" + strVal + "\"", offset, Type.STR);
            return Type.STR;
        }

        long longVal = API.readMemoryLong(address + offset);
        if (longVal == 0) return null;

        double dblVal = Double.longBitsToDouble(longVal);

        // Probably a double, has a sensible exponent and is non-zero
        if (Math.abs(Math.getExponent(dblVal)) < 32 && BigDecimal.valueOf(dblVal).scale() < 5) {
            printStr(Double.toString(dblVal), offset, Type.DBL);
            return Type.DBL;
        }
        // Probably an int in the previous slot, the right-half is all 0s
        if (((longVal >> 32) << 32) == longVal) {
            return null;
        }
        // Assume it's an int
        if (Math.abs((int) longVal) <= 0xFFFFFF) {
            printStr(Integer.toString((int) longVal), offset, Type.INT);
            return Type.INT;
        }

        return null;
    }

    private static Type printObjName(long address, int offset) {
        String result = API.readMemoryStringFallback(address, null);

        if (result != null && !result.isEmpty()) {
            printStr(replacements.getOrDefault(result, result), offset, Type.OBJ);
            return Type.OBJ;
        }
        return null;
    }

    private static void printStr(String s, int offset, Type type) {
        System.out.println(type + "[" + offset + " | 0x" + Integer.toHexString(offset) + "]\u001B[0m " + s);
    }

    private static void updateReplacements() {
        if (REPLACEMENTS_FILE == null || lastModified == REPLACEMENTS_FILE.lastModified()) return;
        lastModified = REPLACEMENTS_FILE.lastModified();

        try (BufferedReader br = new BufferedReader(new FileReader(REPLACEMENTS_FILE))) {
            replacements = br.lines()
                    .map(s -> s.trim().split("="))
                    .filter(s -> s.length == 2)
                    .collect(Collectors.toMap(s -> s[0], s -> s[1]));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static File getReplacementsFile() {
        File file = new File(FILE_NAME);

        if (file.exists()) return file;
        return null;
    }
}
