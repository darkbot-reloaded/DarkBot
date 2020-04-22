package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.utils.LoginData;
import com.github.manolo8.darkbot.utils.Time;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;

public class DarkFlash extends AbstractDarkBotApi {
    private LoginData loginData;

    public DarkFlash(LoginData loginData) {
        this.loginData = loginData;
    }

    @Override
    public void createWindow() {
        IntByReference parentProcessId = new IntByReference();

        String url = "https://" + loginData.getUrl() + "/",
                sid = "dosid=" + loginData.getSid();

        setCookie(url, sid);
        Thread apiThread = new Thread(() -> this.loadSWF(loginData.getPreloaderUrl(), loginData.getParams(), url));
        apiThread.setDaemon(true);
        apiThread.start();

        Thread windowFinder = new Thread(() -> {
            while ((window = USER_32.FindWindow(null, "DarkPlayer")) == null ||
                    !USER_32.IsWindow(window) ||
                    USER_32.GetWindowThreadProcessId(window, parentProcessId) == 0 ||
                    parentProcessId.getValue() != Kernel32.INSTANCE.GetCurrentProcessId()) Time.sleep(100);

            setVisible(true);
            WinDef.RECT rect = new WinDef.RECT();
            USER_32.GetWindowRect(window, rect);
            USER_32.MoveWindow(window, rect.left, rect.top, 1270, 800, true);

            flash = USER_32.FindWindowEx(USER_32.FindWindowEx(window, null, null, null), null, null, null);
            setVisible(true);
            setRender(true);
        });
        windowFinder.setDaemon(true);
        windowFinder.start();
    }

    @Override
    public void refresh() {
        this.reloadSWF();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void mouseMove(int x, int y) {
        USER_32.SendMessage(flash, 0x0200, null, new WinDef.LPARAM(x & 0xFFFF | ((y & 0xFFFF) << 16)));
    }

    @Override
    public void mouseClick(int x, int y) {
        this.mousePress(x, y);
    }

    @Override
    public void keyboardClick(char btn) {
        this.keyPress(btn);
    }

    public native void setCookie(String url, String cookie);

    private native void loadSWF(String preloader, String params, String url);

    private native void reloadSWF();

    public native void mousePress(int x, int y);

    public native void keyPress(char btn);

    public native double readMemoryDouble(long address);

    public native long readMemoryLong(long address);

    public native int readMemoryInt(long address);

    public boolean readMemoryBoolean(long address) {
        return this.readMemoryInt(address) == 1;
    }

    public native byte[] readMemory(long address, int length);

    public native void writeMemoryDouble(long address, double value);

    public native void writeMemoryLong(long address, long value);

    public native void writeMemoryInt(long address, int value);

    public native long[] queryMemoryInt(int value, int maxQuantity);

    public native long[] queryMemoryLong(long value, int maxQuantity);

    public native long[] queryMemory(byte[] query, int maxQuantity);

    public native void setVisible(boolean flag);

    public native void setRender(boolean flag);

    static {
        System.loadLibrary("lib/DarkFlash");
    }

}
