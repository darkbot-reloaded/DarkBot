package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.utils.WeakValueHashMap;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class ApiAdapter implements IDarkBotAPI {

    protected static final User32 USER_32 = User32.INSTANCE;

    protected volatile WinDef.HWND window;
    protected volatile WinDef.HWND flash;

    private int initialWidth, initialHeight; // Prefered sizes set by setSize

    private static final String FALLBACK_STRING = "ERROR";
    private final WeakValueHashMap<Long, String> stringCache = new WeakValueHashMap<>();

    @Override
    public String readMemoryString(long address) {
        return readMemoryStringFallback(address, FALLBACK_STRING);
    }

    @Override
    public String readMemoryStringFallback(long address, String fallback) {
        String str = readMemoryStringInternal(address);
        return str == null ? fallback : str;
    }

    public String readMemoryStringInternal(long address) {
        int size = readMemoryInt(address + 32);
        if (size == 0) return "";

        //get string from cache and return if exists
        String temp = stringCache.get(address);
        if (temp != null) return temp;

        int flags = readMemoryInt(address + 36);
        int width = (flags & 0b001);
        int type = (flags & 0b110) >> 1;

        //we assume that string size over 1024 or below 0 is invalid
        if ((size <<= width) > 1024 || size < 0) return null;

        //read string buffer
        byte[] bytes;
        if (type == 2)
            bytes = readMemory(readMemoryLong(address, 24, 16) + readMemoryInt(address + 16), size);
        else
            bytes = readMemory(readMemoryLong(address + 16), size);

        temp = new String(bytes, width == 0 ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_16LE);
        stringCache.put(address, temp);

        return temp;
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

