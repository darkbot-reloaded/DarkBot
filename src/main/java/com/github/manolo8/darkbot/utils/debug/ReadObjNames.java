package com.github.manolo8.darkbot.utils.debug;

import com.github.manolo8.darkbot.utils.BetterLogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    public static void of(long address) {
        of(address, 0x400);
    }

    public static void of(long address, int maxOffset) {
        updateReplacements();
        String result = API.readMemoryString(address, 0x10, 0x28, 0x90);
        BetterLogUtils.getInstance().PrintLn("\n==========[" + replacements.getOrDefault(result, result) + "]==========");

        for (int offset = 0; offset < maxOffset; offset++) {
            long addr = API.readMemoryLong(address + offset);
            //prints string at offset
            print(addr, offset, false);

            if (isInvalid(addr)) continue;
            addr = API.readMemoryLong(addr + 0x10);
            if (isInvalid(addr)) continue;
            addr = API.readMemoryLong(addr + 0x28);
            if (isInvalid(addr)) continue;

            //prints object name at offset
            print(API.readMemoryLong(addr + 0x90), offset, true);
        }
    }

    private static boolean isInvalid(long address) {
        return (address < 0xFF00000000L || address > 0xF000000000000L);
    }

    private static void print(long address, int offset, boolean isObject) {
        String result = API.readMemoryStringFallback(address, null);

        if (result != null && !result.isEmpty())
            BetterLogUtils.getInstance().PrintLn(formatString(replacements.getOrDefault(result, result), offset, isObject));
    }

    private static String formatString(String s, int offset, boolean isObject) {
        return (isObject ? "\u001B[34mOBJ" : "\u001B[32mSTR") + "[" + offset + " | 0x" + Integer.toHexString(offset) + "]\u001B[0m " + s;
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
            BetterLogUtils.getInstance().PrintLn(e.getMessage());
        }
    }

    private static File getReplacementsFile() {
        File file = new File(FILE_NAME);

        if (file.exists()) return file;
        return null;
    }
}
