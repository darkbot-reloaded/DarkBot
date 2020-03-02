package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.utils.Time;
import com.sun.jna.platform.win32.WinDef;

public class DarkFlash extends AbstractDarkBotApi {
    private LoginData loginData;

    public DarkFlash(LoginData loginData) {
        this.loginData = loginData;
    }

    @Override
    public void createWindow() {
        setCookie(loginData.url, loginData.sid);
        new Thread(() -> this.loadSWF(loginData.preloaderUrl, loginData.params, loginData.url)).start();
        new Thread(() -> {
            while ((window = USER_32.FindWindow(null, "DarkPlayer")) == null || !USER_32.IsWindow(window)) Time.sleep(100);
            WinDef.RECT rect = new WinDef.RECT();
            USER_32.GetWindowRect(window, rect);
            USER_32.MoveWindow(window, rect.left, rect.top, 1280, 800, true);
            setVisible(true);
            setRender(true);
        }).start();
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

    public static class LoginData {
        private final String sid, url, preloaderUrl, params;

        public LoginData(String sid, String url, String preloaderUrl, String params) {
            this.sid = sid;
            this.url = url;
            this.preloaderUrl = preloaderUrl;
            this.params = params;
        }

        @Override
        public String toString() {
            return "LoginData{" +
                    "sid='" + sid + '\'' +
                    ", url='" + url + '\'' +
                    ", preloaderUrl='" + preloaderUrl + '\'' +
                    ", params='" + params + '\'' +
                    '}';
        }
    }
}
