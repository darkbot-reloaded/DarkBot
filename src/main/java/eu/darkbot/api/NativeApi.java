package eu.darkbot.api;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class NativeApi {
    static {
        System.loadLibrary("lib/core-api");
    }

    public native boolean createBot(int botId);
    public native boolean isValid(int botId);
    public native boolean removeBot(int botId);
    public native boolean sendMessage(int botId, String message);
    public boolean sendMessage(int botId, Object... params) {
        String command = Arrays.stream(params).map(Objects::toString).collect(Collectors.joining("|")) + "#";
        return sendMessage(botId, command);
    }

    public native void mouseClick(int botId, int delay, int x, int y);

    public native long[] queryMemoryInt (int botId, int    value, int maxQuantity);
    public native long[] queryMemoryLong(int botId, long   value, int maxQuantity);
    public native long[] queryMemory    (int botId, byte[] query, int maxQuantity);

    public native byte[]  readMemory       (int botId, long address, int length);
    public native int     readMemoryInt    (int botId, long address);
    public native long    readMemoryLong   (int botId, long address);
    public native float   readMemoryFloat  (int botId, long address);
    public native double  readMemoryDouble (int botId, long address);
    public native boolean readMemoryBoolean(int botId, long address);

    public native boolean writeMemoryInt    (int botId, long address, int     value);
    public native boolean writeMemoryLong   (int botId, long address, long    value);
    public native boolean writeMemoryFloat  (int botId, long address, float   value);
    public native boolean writeMemoryDouble (int botId, long address, double  value);
    public native boolean writeMemoryBoolean(int botId, long address, boolean value);

}
