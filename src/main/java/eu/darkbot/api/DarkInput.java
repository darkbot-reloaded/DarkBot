package eu.darkbot.api;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.utils.LibUtils;

public class DarkInput implements GameAPI.Interaction {

    static {
        LibUtils.loadLibrary("DarkInputAPI");
    }

    public native int  getVersion();
    public native boolean openWindow(long pid, int width, int height);

    public native void keyClick  (int keyCode);
    public native void sendText  (String text);

    public native void mouseMove (int x, int y);
    public native void mouseDown (int x, int y);
    public native void mouseUp   (int x, int y);
    public native void mouseClick(int x, int y);

}