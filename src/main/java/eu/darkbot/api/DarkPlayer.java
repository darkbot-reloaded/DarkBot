package eu.darkbot.api;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.utils.LibUtils;

public class DarkPlayer implements GameAPI.Memory, GameAPI.Window, GameAPI.Handler, GameAPI.Interaction, API.Singleton {

    static {
        LibUtils.loadLibrary("DarkPlayerAPI");
    }

    public native void    setData(String url, String sid, String preloader, String vars);
    public native void    createWindow();
    public native void    setSize(int width, int height);
    public native void    setVisible(boolean visible);
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

    /*
        Custom natives
    */

    public native boolean createProcess(String path, String name);
    public native boolean isAlive();

    public native void    setHook(long address, int index);
    public native boolean isHookValid(long address, int index);

    public native void    lockEntity(long screenManager, double localPlayerX, double localPlayerY, double targetX, double targetY, int targetId);
    public native void    moveShip(long eventAddress, double x, double y);
    public native void    collectBox(long eventAddress, double x, double y, long boxAddress);
}