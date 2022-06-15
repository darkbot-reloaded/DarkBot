package eu.darkbot.api;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.utils.LibUtils;

public class KekkaPlayer implements GameAPI.Window, GameAPI.Handler, GameAPI.Memory, GameAPI.Interaction, API.Singleton {

    static {
        LibUtils.loadLibrary("KekkaPlayer");
    }

    public native boolean useItem(long screenManager, String check, int methodIdx, long... args);

    public native long callMethodSync(int methodIdx, long... args);
    //async only
    public native boolean callMethod(long screenManager, int methodIdx, long... args);

    public native void selectEntity(long clickableAddress, long confirmAddress, boolean doubleClick);
    public native void moveShip(long screenManager, long x, long y, long collectableAdr);
    public native void pasteText(String text, long... actions);
    public native void postActions(long... actions);

    public native double getCpuUsage();

    // Returns true when game(main.swf) is fully loaded (green start button)
    public native boolean isValid();
    public native void    clearCache();
    public native void    emptyWorkingSet();

    // 0 = disabled - game reload is needed. Blocks "deltadna.net" & "eventstream" requests by default
    public native void    setLocalProxy(int port);
    public native void    setPosition(int x, int y);

    // API will try to use default registered Flash if given one failed somehow
    public native void    setFlashOcxPath(String path);
    public native void    setUserInput(boolean enableInput);

    // Game client size(not window)
    public native void    setClientSize(int width, int height);
    public native void    setMinClientSize(int width, int height);
    public native void    setTransparency(int transparency); // 0 - 100
    public native void    setVolume(int volume); // 0 - 100

    // LOW = 0, MEDIUM = 1, HIGH = 2, BEST = 3, AUTO_LOW = 4, AUTO_HIGH = 5
    public native void    setQuality(int quality);
    // ~25% faster finding one value than queryBytes(byte[] pattern, int maxSize)
    public native long    queryBytes(byte[] pattern);

    // Everything else same as DarkBoat

    public native void    setData(String url, String sid, String preloader, String vars);
    public native void    createWindow();
    public native void    setSize(int width, int height);
    public native void    setVisible(boolean visible);

    // When enabled browser is further hidden, lowering CPU usage, but doesn't work on all systems
    public native void    setMinimized(boolean minimized); // Hidden actually
    public native void    reload();
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
}
