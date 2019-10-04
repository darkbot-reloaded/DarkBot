package com.github.manolo8.darkbot.core;

public class DarkFlash extends AbstractDarkBotApi {
    private LoginData loginData;

    public DarkFlash(LoginData loginData) {
        this.loginData = loginData;
    }

    @Override
    public void createWindow() {
        System.out.println(loginData);
        setCookie(loginData.url, "dosid=" + loginData.sid);
        new Thread(() -> this.loadSWF(loginData.preloaderUrl, loginData.params, loginData.url)).start();
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
        System.loadLibrary("DarkFlash");
    }

    public static class LoginData {
        private final String sv, sid, preloaderUrl, params, url;

        public LoginData(String sv, String sid, String preloaderUrl, String params, String url) {
            this.sv = sv;
            this.sid = sid;
            this.preloaderUrl = preloaderUrl;
            this.params = params;
            this.url = url;
        }

        @Override
        public String toString() {
            return "LoginData{" +
                    "sv='" + sv + '\'' +
                    ", sid='" + sid + '\'' +
                    ", preloaderUrl='" + preloaderUrl + '\'' +
                    ", params='" + params + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}
