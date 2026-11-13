package eu.darkbot.api;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.utils.LibUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Native DLL bridge for Flash Player hosting, memory access, AVM2 method calling, and input.
 * All Win32/COM/ActiveX, Flash hosting, and hook management is handled in FlashBridge.dll.
 *
 * This is KekkaPlayer alternative - with added proxy support
 */
public class FlashBridge implements GameAPI.Window, GameAPI.Handler, GameAPI.Memory, GameAPI.Interaction, API.Singleton {

    static { LibUtils.loadLibrary("FlashBridge"); }

    // ═══════════════════════════════════════════════════════════
    // Window lifecycle
    // ═══════════════════════════════════════════════════════════

    public static native void setLoginData(String url, String sid, String preloader, String flashVars);
    public static native void setOcxPath(String path);
    public native void createWindow();
    /** Reload <b>with</b> a fake daily login. KekkaPlayer's {@code reload}. */
    public native void reload();

    /** Reload <b>without</b> the fake daily login. KekkaPlayer's {@code normalReload}. */
    public native void normalReload();
    public native void setVisible(boolean visible);
    public native void setSize(int width, int height);
    public native void setPosition(int x, int y);

    /** Size the area the game renders into */
    @Override public native void setClientSize(int width, int height);

    /** Floor for the client area, enforced against dragging too, not just resizes. */
    @Override public native void setMinClientSize(int width, int height);

    /**
     * Block or allow real user input to the game while the bot drives it.
     * Synthetic input from the bot is unaffected.
     */
    @Override public native void setUserInput(boolean enableInput);
    public native void setMinimized(boolean minimized);

    @Override
    public void setData(String url, String sid, String preloader, String vars) {
        try {
            Path ocxPath = LibUtils.getFlashOcxPath();
            if (ocxPath != null && Files.exists(ocxPath))
                setOcxPath(ocxPath.toAbsolutePath().toString());
        } catch (Exception e) {
            System.err.println("FlashBridge: Could not resolve OCX path: " + e.getMessage());
        }
        setLoginData(url, sid, preloader, vars);
    }

    /**
     * Match URLs by regex and replace or block them. Pairs of
     * {@code [regex, filePath, regex, filePath, ...]} — an empty filePath blocks
     * the request, a non-empty one serves that file's bytes as the response body.
     */
    public native void setBlockingPatterns(String... blockingEntryPatterns);


    @Override public native int getVersion();

    @Override
    public native long lastInternetReadTime(); // GetTickCount64 of last HTTP read
    public native void setQuality(int quality);
    public native void setTransparency(int transparency); // 0 - 100
    public native void setVolume(int volume);             // 0 - 100

    /**
     * The whole proxy API: route Flash's game sockets through the local relay
     * on this port, or 0 to disable.
     */
    @Override
    public native void setLocalProxy(int port);

    // ═══════════════════════════════════════════════════════════
    // State & diagnostics
    // ═══════════════════════════════════════════════════════════

    @Override public native boolean isValid();
    @Override public native long getMemoryUsage();
    @Override public native double getCpuUsage();
    @Override public native void emptyWorkingSet();
    @Override public native void clearCache(String pattern);
    public static native void setMaxFps(int fps);
    public static native void setScreenManager(long screenManager);

    // ═══════════════════════════════════════════════════════════
    // Memory read
    // ═══════════════════════════════════════════════════════════

    public native int     readInt(long address);
    public native long    readLong(long address);
    public native double  readDouble(long address);
    public native boolean readBoolean(long address);
    public native byte[]  readBytes(long address, int length);

    public void readBytes(long address, byte[] buff, int length) {
        byte[] data = readBytes(address, length);
        if (data != null && buff != null)
            System.arraycopy(data, 0, buff, 0, Math.min(data.length, buff.length));
    }

    // ═══════════════════════════════════════════════════════════
    // Memory write
    // ═══════════════════════════════════════════════════════════

    public native void writeInt(long address, int value);
    public native void writeLong(long address, long value);
    public native void writeDouble(long address, double value);
    public native void writeBoolean(long address, boolean value);
    public native void writeBytes(long address, byte... bytes);

    public void replaceInt(long a, int o, int n)        { if (readInt(a) == o) writeInt(a, n); }
    public void replaceLong(long a, long o, long n)     { if (readLong(a) == o) writeLong(a, n); }
    public void replaceDouble(long a, double o, double n) { if (Double.compare(readDouble(a), o) == 0) writeDouble(a, n); }
    public void replaceBoolean(long a, boolean o, boolean n) { if (readBoolean(a) == o) writeBoolean(a, n); }

    // ═══════════════════════════════════════════════════════════
    // Memory pattern scanning
    // ═══════════════════════════════════════════════════════════

    public native long[] queryInt(int value, int maxSize);
    public native long[] queryLong(long value, int maxSize);
    public native long[] queryBytes(byte[] pattern, int maxSize);

    // Single-shot MainApplication scan. Validates natively over every match, so
    // it does not return the stale copies that OCX reuse leaves behind.


    // ═══════════════════════════════════════════════════════════
    // AVM2 method calling (auto-hooks on first use via screenManager)
    // ═══════════════════════════════════════════════════════════

    public static native long    callMethodSync(int methodIdx, long... args);
    public static native boolean callMethodAsync(int methodIdx, long... args);
    public static native void    moveShip(long screenManager, long x, long y, long collectableAdr);
    public static native void    collectBox(long screenManager, long x, long y, long boxAddress);
    public static native void    refine(long refineUtilAddress, int oreId, int amount);
    public static native boolean sendNotification(long screenManager, String notification, long... args);
    public static native int     checkMethodSignature(long obj, int methodIdx, boolean includeMethodName, String signature);
    public static native void    printScriptObject(long obj, boolean verbose);
    public static void           printScriptObject(long obj) { printScriptObject(obj, false); }

    public static boolean useItem(long connectionManager, String itemId, int sendRequestIdx, long closureAddr) {
        if (closureAddr == 0 || connectionManager == 0 || itemId == null || itemId.isEmpty()) return false;
        return nativeUseItem(connectionManager, closureAddr, itemId);
    }
    private static native boolean nativeUseItem(long connectionManager, long closureAddr, String itemId);

    // ═══════════════════════════════════════════════════════════
    // AVM2 string creation
    // ═══════════════════════════════════════════════════════════

    public static native long createString(String value);

    // ═══════════════════════════════════════════════════════════
    // Input
    // ═══════════════════════════════════════════════════════════

    public static native void sendMouseClick(int x, int y);
    public static native void sendMouseMove(int x, int y);
    public static native void sendMouseDown(int x, int y);
    public static native void sendMouseUp(int x, int y);
    public static native void sendKeyClick(int keyCode);
    public native void sendText(String text);

    // ═══════════════════════════════════════════════════════════
    // Batched input (NativeAction encoded longs)
    // ═══════════════════════════════════════════════════════════

    public static native void postActions(long... actions);
    public static native void pasteText(String text, long... actions);

    @Override public void mouseClick(int x, int y) { sendMouseClick(x, y); }
    @Override public void mouseMove(int x, int y)  { sendMouseMove(x, y); }
    @Override public void mouseDown(int x, int y)  { sendMouseDown(x, y); }
    @Override public void mouseUp(int x, int y)    { sendMouseUp(x, y); }
    @Override public void keyClick(int keyCode)     { sendKeyClick(keyCode); }
}