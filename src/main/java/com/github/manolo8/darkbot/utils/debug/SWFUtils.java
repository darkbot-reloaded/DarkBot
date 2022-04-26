package com.github.manolo8.darkbot.utils.debug;

import com.github.manolo8.darkbot.core.api.util.ByteBufferReader;
import com.github.manolo8.darkbot.core.api.util.DataBuffer;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.github.manolo8.darkbot.Main.API;

public class SWFUtils {

    public static void dumpMainSWF() {
        byte[] header = {'F', 'W', 'S', 21};
        long[] addresses = API.queryMemory(header, 10);

        for (long addr : addresses) {
            int size = API.readMemoryInt(addr + 4);
            if (size < 11_500_000 || size > 13_000_000) continue;

            try (FileOutputStream writer = new FileOutputStream("main.swf")) {
                for (int i = 0; i < size; i += DataBuffer.MAX_CHUNK_SIZE) {
                    try (DataBuffer data = API.readData(addr + i, Math.min(DataBuffer.MAX_CHUNK_SIZE, size - i))) {
                        if (!(data instanceof ByteBufferReader))
                            throw new UnsupportedOperationException("Cannot dump main SWF with this data reader");
                        writer.getChannel().write(((ByteBufferReader) data).getByteBuffer(), i);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println("SWF not found, are you running the flash client?");
    }
}
