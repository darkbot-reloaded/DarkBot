package eu.darkbot.api;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.utils.LibUtils;

public class DarkMem implements GameAPI.Window, GameAPI.Memory {

    static {
        LibUtils.loadLibrary("DarkMemAPI");
    }

    public native int    getVersion();
    public native Proc[] getProcesses();
    public native void   openProcess(long pid);

    public native int     readInt    (long address);
    public native long    readLong   (long address);
    public native double  readDouble (long address);
    public native boolean readBoolean(long address);
    public native byte[]  readBytes  (long address, int length);
    public native void    readBytes  (long address, byte[] buff, int length);

    public native void replaceInt    (long address, int     oldValue, int     newValue);
    public native void replaceLong   (long address, long    oldValue, long    newValue);
    public native void replaceDouble (long address, double  oldValue, double  newValue);
    public native void replaceBoolean(long address, boolean oldValue, boolean newValue);

    public native void writeInt      (long address, int     value);
    public native void writeLong     (long address, long    value);
    public native void writeDouble   (long address, double  value);
    public native void writeBoolean  (long address, boolean value);
    public native void writeBytes    (long address, byte... bytes);

    public native long[] queryInt    (int    value  , int maxSize);
    public native long[] queryLong   (long   value  , int maxSize);
    public native long[] queryBytes  (byte[] pattern, int maxSize);

    public static class Proc implements GameAPI.Window.Proc {
        private final int pid;
        private final String name;

        public Proc(int pid, String name) {
            this.pid = pid;
            this.name = name;
        }

        public int getPid() {
            return pid;
        }

        public String getName() {
            return name;
        }
    }
}