package com.github.manolo8.darkbot.utils.debug;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.github.manolo8.darkbot.Main.API;

public class SWFUtils {

    public static void dumpMainSWF() {
        byte[] header = {'F', 'W', 'S', 21};
        long[] addresses = API.queryMemory(header, 10);

        for (long addr : addresses) {
            int size = API.readMemoryInt(addr + 4);
            if (size < 11_500_000 || size > 15_000_000) continue;

            try (FileOutputStream writer = new FileOutputStream("main.swf")) {
                writer.write(API.readMemory(addr, size));
                System.out.println("SWF saved - " + (size / (1024f * 1024f)) + "MB");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println("SWF not found, are you running the flash client?");
    }

}
