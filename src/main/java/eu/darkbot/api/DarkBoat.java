package eu.darkbot.api;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.utils.LibUtils;

import java.lang.annotation.Native;
import java.nio.ByteBuffer;

public class DarkBoat implements GameAPI.Window, GameAPI.Handler, GameAPI.Memory, GameAPI.Interaction, API.Singleton {

    static {
        LibUtils.loadLibrary("DarkBoatAPI");
    }

    public native void    setData(String url, String sid, String preloader, String vars);
    public native void    createWindow();
    public native void    setSize(int width, int height);
    public native void    setVisible(boolean visible);
    // When enabled browser is further hidden, lowering CPU usage, but doesn't work on all systems
    public native void    setMinimized(boolean minimized);
    public native void    reload();
    public native boolean isValid();
    public native long    getMemoryUsage();
    public native void    setMaxFps(int maxFps);
    public native int     getVersion();

    public native void keyClick  (int keyCode);
    public native void sendText  (String text);

    public native void mouseMove (int x, int y);
    public native void mouseDown (int x, int y);
    public native void mouseUp   (int x, int y);
    public native void mouseClick(int x, int y);

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

    // DarkBoat v9+
    public final @Native ByteBuffer[] buffers = new ByteBuffer[10];

    // writes data at given address to direct buffer
    public native boolean readToBuffer(int bufferIdx, long address, int length);

    // writes string data to direct buffer
    public native int readStringBuffer(int bufferIdx, long address);

    // same as above but reads the pointer before reading the string
    public native int readStringBufferPtr(int bufferIdx, long address);
}