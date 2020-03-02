package com.github.manolo8.darkbot.core;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.scene.input.Clipboard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class AbstractDarkBotApi implements IDarkBotAPI {

    protected static final User32 USER_32 = User32.INSTANCE;

    private static final int WM_SETTEXT = 0x000C;
    private static final int WM_PASTE = 0x0302;
    private static final int WM_KEYDOWN = 0x0100;
    private static final int WM_KEYUP = 0x0101;

    protected volatile WinDef.HWND window;
    protected volatile WinDef.HWND flash;

    @Override
    public void keyboardClick(Character ch) {
        if (ch != null) keyboardClick((char) ch);
    }

    private void findFlashInWindow() {
        if (flash != null) return;;
        char[] buffer = new char[128];

        boolean found = false;
        for (int i = 0; i < 10; i++) {
            flash = USER_32.FindWindowEx(flash == null ? window : flash, null, null, "");
            if (String.valueOf(buffer, 0, USER_32.GetClassName(flash, buffer, 128))
                    .equals("MacromediaFlashPlayerActiveX")) {
                found = true;
                break;
            }
        }
        if (!found) flash = null;
    }

    public void sendText(String string) {
        if (true) return; // Exit, none of the 3 methods seem to be working.

        findFlashInWindow();

        // VM_SETTEXT, doesn't work
        int len = string.length() + 1;
        Memory pointer = new Memory(len);
        pointer.setString(0, string);

        USER_32.SendMessage(flash, WM_SETTEXT, new WinDef.WPARAM(), new WinDef.LPARAM(Pointer.nativeValue(pointer)));

        // VM_KEYDOWN + VM_KEYUP, doesn't work
        USER_32.SendMessage(flash, WM_KEYDOWN, new WinDef.WPARAM('J'), null);
        USER_32.SendMessage(flash, WM_KEYUP, new WinDef.WPARAM('J'), null);

        // VM_PASTE, doesn't work
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(string), null);
        USER_32.SendMessage(flash, WM_PASTE, new WinDef.WPARAM(0), new WinDef.LPARAM(0));
    }

    public String readMemoryString(long address) {
        int flags = readMemoryInt(address + 36);
        int width = (flags & 0x00000001);
        int size = readMemoryInt(address + 32) << width;
        int type = (flags & 0x00000006) >> 1;

        if (size > 1024 || size < 0) return "ERROR";

        byte[] bytes;

        if (type == 2)
            bytes = readMemory(readMemoryLong(readMemoryLong(address + 24) + 16) + readMemoryInt(address + 16), size);
        else
            bytes = readMemory(readMemoryLong(address + 16), size);

        return width == 0 ? new String(bytes, StandardCharsets.ISO_8859_1) : new String(bytes, StandardCharsets.UTF_16LE);
    }

    public void handleRefresh() {
        refresh();
    }
    public abstract void refresh();

}

class LoggingAPIHandler implements InvocationHandler {

    private DarkBotAPI API;

    LoggingAPIHandler(DarkBotAPI API) {
        this.API = API;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        boolean log = method.getName().startsWith("write") && !method.getName().equals("writeMemoryDouble");
        if (log) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (int i = 3; i < trace.length - 3; i++) System.out.println(trace[i]);
            System.out.println("API CALL: " + method.getName() + (args != null ? Arrays.toString(args) : ""));
        }
        Object res = method.invoke(API, args);
        if (res != null && log) System.out.println("  -> " + res);
        return res;
    }

}
