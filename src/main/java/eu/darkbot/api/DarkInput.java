package eu.darkbot.api;

import java.nio.file.Paths;

public class DarkInput {

    static {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            System.load(Paths.get("lib", "DarkInputAPI.dll").toAbsolutePath().toString());
        else
            System.load(Paths.get("lib", "DarkInputAPI.so").toAbsolutePath().toString());
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