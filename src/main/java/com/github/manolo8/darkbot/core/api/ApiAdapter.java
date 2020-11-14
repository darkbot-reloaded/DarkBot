package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.awt.*;
import java.util.Arrays;

public abstract class ApiAdapter implements IDarkBotAPI {

    protected final LoginData loginData;

    protected static final User32 USER_32 = User32.INSTANCE;

    protected volatile WinDef.HWND window;
    protected volatile WinDef.HWND flash;

    private int initialWidth, initialHeight; // Prefered sizes set by setSize

    private static final String FALLBACK_STRING = "ERROR";
    private final ByteUtils.StringReader stringReader = new ByteUtils.StringReader(this);

    protected ApiAdapter(LoginData loginData) {
        this.loginData = loginData;
    }

    public void relogin() {
        if (loginData == null) {
            System.err.println("Re-logging in is unsupported for this browser/API");
            return;
        }
        System.out.println("Re-logging in: Logging in (1/2)");
        LoginUtils.usernameLogin(loginData);
        System.out.println("Re-logging in: Loading spacemap (2/2)");
        LoginUtils.findPreloader(loginData);
    }

    @Override
    public String readMemoryString(long address) {
        return readMemoryStringFallback(address, FALLBACK_STRING);
    }

    @Override
    public String readMemoryStringFallback(long address, String fallback) {
        String str = stringReader.readString(address);
        return str == null ? fallback : str;
    }


    private int x, y, w, h;
    public void setVisible(boolean visible) {
        if (!visible) {
            WinDef.RECT rect = new WinDef.RECT();
            USER_32.GetWindowRect(window, rect);
            x = rect.left;
            y = rect.top;
            w = Math.abs(rect.right - rect.left);
            h = Math.abs(rect.bottom - rect.top);
        }
        int minX = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
                .mapToInt(g -> g.getDefaultConfiguration().getBounds().x).min().orElse(0);

        USER_32.MoveWindow(window, visible ? x : minX - w - 100, y, w, h, true);
        if (visible) USER_32.SetForegroundWindow(window);
    }

    public void setMinimized(boolean visible) {
        this.setVisible(visible);
    }

    public void rawKeyboardClick(char ch) {
        keyboardClick(ch);
    }
    public void sendText(String str) {}
    public void replaceInt(long addr, int oldValue, int newValue) {
        writeMemoryInt(addr, newValue);
    }
    public long getMemoryUsage() {
        return 0L;
    }
    public int getVersion() {
        return 0;
    }

    public void setSize(int width, int height) {
        if (width == -1 && height == -1) {
            width = this.initialWidth;
            height = this.initialHeight;
        }
        if (window == null) {
            this.initialWidth = width;
            this.initialHeight = height;
            return;
        }

        WinDef.RECT rect = new WinDef.RECT();
        USER_32.GetWindowRect(window, rect);
        x = rect.left;
        y = rect.top;
        w = width;
        h = height;
        int minX = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
                .mapToInt(g -> g.getDefaultConfiguration().getBounds().x).min().orElse(0);


        USER_32.MoveWindow(window, x > minX ? x : minX - w - 100, y, w, h, true);
    }

}
