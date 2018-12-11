package com.github.manolo8.darkbot.core.def;

import com.github.manolo8.darkbot.gui.Mapping;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;
import java.util.List;

import static com.sun.jna.platform.win32.WinDef.DWORD;
import static com.sun.jna.platform.win32.WinDef.HWND;
import static com.sun.jna.platform.win32.WinNT.HANDLE;

public class WindowsAPI {

    private static Kernel32 KERNEL = Kernel32.INSTANCE;
    private static User32 USER = User32.INSTANCE;
    private static GDI32 GDI = GDI32.INSTANCE;


    private HWND browser;
    private HWND flash;
    private HWND button;
    private HANDLE process;

    private Memory safeArray;
    private Memory safe;

    private Memory allocator = new Memory(1024);
    private byte[] buffer = new byte[1024];
    private int[] pixels = new int[0];

    public WindowsAPI() {
        this.safe = new Memory(128);
        this.safeArray = new Memory(1024 * 8);
    }

    public boolean attachToWindow() {
        browser = USER.FindWindow(null, "BotBrowser");

        char[] buffer = new char[128];
        int chars;


        chars = USER.GetWindowText(browser, buffer, 128);

        if (chars == 0 || !String.valueOf(buffer, 0, chars).equals("BotBrowser")) {
            return false;
        }

        flash = USER.FindWindowEx(browser, null, null, "");
        button = USER.FindWindowEx(browser, null, null, "Map");
        flash = USER.FindWindowEx(flash, null, null, "");
        flash = USER.FindWindowEx(flash, null, null, "");
        flash = USER.FindWindowEx(flash, null, null, "");
        flash = USER.FindWindowEx(flash, null, null, "");

        chars = USER.GetClassName(flash, buffer, 128);

        if (chars == 0 || !String.valueOf(buffer, 0, chars).equals("MacromediaFlashPlayerActiveX")) {
            return false;
        }

        IntByReference reference = new IntByReference();
        USER.GetWindowThreadProcessId(flash, reference);

        process = KERNEL.OpenProcess(0x0010 | 0x0020 | 0x0400, false, reference.getValue());

        return true;
    }

    public void free() {
        allocator = new Memory(1024);
        buffer = new byte[1024];
    }

    public void mousePress(double x, double y) {
        USER.SendMessage(
                flash,
                0x0201,
                new WinDef.WPARAM(0x0001),
                new WinDef.LPARAM(((int) Math.round(x)) | (((int) Math.round(y)) << 16))
        );
    }

    public void mouseMove(double x, double y) {
        USER.SendMessage(
                flash,
                0x0200,
                null,
                new WinDef.LPARAM(((int) Math.round(x)) | (((int) Math.round(y)) << 16))
        );
    }

    public void mouseRelease() {
        USER.SendMessage(
                flash,
                0x0202,
                new WinDef.WPARAM(0x0001),
                null
        );
    }

    public void mouseClick(double x, double y) {
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        USER.SendMessage(flash, 0x0201, new WinDef.WPARAM(0x0001), new WinDef.LPARAM(rx | (ry << 16)));
        USER.SendMessage(flash, 0x0202, new WinDef.WPARAM(0x0001), new WinDef.LPARAM(rx | (ry << 16)));
    }

    public void button(char btn) {
        USER.SendMessage(flash, 0x0100, new WinDef.WPARAM(Character.toUpperCase(btn)), null);
        USER.SendMessage(flash, 0x0101, new WinDef.WPARAM(Character.toUpperCase(btn)), null);
    }

    public double readMemoryDouble(long address) {
        KERNEL.ReadProcessMemory(process, new Pointer(address), safe, 8, null);
        return safe.getDouble(0);
    }

    public long readMemoryLong(long address) {
        KERNEL.ReadProcessMemory(process, new Pointer(address), safe, 8, null);
        return safe.getLong(0);
    }

    public int readMemoryInt(long address) {
        KERNEL.ReadProcessMemory(process, new Pointer(address), safe, 4, null);
        return safe.getInt(0);
    }

    public boolean readMemoryBoolean(long address) {
        return readMemoryInt(address) == 1;
    }

    public String readMemoryString(long address) {

        long byteAddress = readMemoryLong(address + 16);
        int size = readMemoryInt(address + 32);

        if (size < 0 || size > 128) return "ERROR";

        KERNEL.ReadProcessMemory(process, new Pointer(byteAddress), safe, size, null);

        byte[] bytes = new byte[size];
        safe.read(0, bytes, 0, size);

        return new String(bytes);
    }


    public Memory readMemory(long address, long length) {

        if (length > safeArray.size()) {
            safeArray.clear();
            System.out.println("Trying to read an memory too big: " + length);
            return safeArray;
        }

        KERNEL.ReadProcessMemory(process, new Pointer(address), safeArray, (int) length, null);

        return safeArray;
    }

    public void writeMemoryLong(long address, long value) {
        safe.setLong(0, value);
        KERNEL.WriteProcessMemory(process, new Pointer(address), safe, 8, null);
    }

    public void writeMemoryInt(long address, int value) {
        safe.setInt(0, value);
        KERNEL.WriteProcessMemory(process, new Pointer(address), safe, 4, null);
    }

    public List<Long> queryMemory(int query) {
        safe.setInt(0, query);
        return queryMemory(safe.getByteArray(0, 4));
    }

    public List<Long> queryMemory(long query) {
        safe.setLong(0, query);
        return queryMemory(safe.getByteArray(0, 8));
    }

    public List<Long> queryMemory(byte[] query) {
        List<Long> found = new ArrayList<>();

        IntByReference size = new IntByReference();
        long address = 0;
        int current = 0;

        Pointer pointer = new Pointer(0);
        WinNT.MEMORY_BASIC_INFORMATION info = new WinNT.MEMORY_BASIC_INFORMATION();

        while (true) {
            KERNEL.VirtualQueryEx(process, pointer, info, new BaseTSD.SIZE_T(info.size()));

            if (info.regionSize.longValue() == 0) break;

            long length = info.regionSize.longValue();

            if (info.state.equals(new DWORD(0x00001000))
                    && info.type.equals(new DWORD(0x00020000))
                    && info.protect.equals(new DWORD(0x04))) {

                if (length > allocator.size()) {
                    allocator = new Memory(length);
                }
                if (length > buffer.length) {
                    buffer = new byte[(int) length];
                }

                KERNEL.ReadProcessMemory(process, pointer, allocator, info.regionSize.intValue(), size);

                int total = size.getValue();

                allocator.read(0, buffer, 0, size.getValue());

                for (int c = 0; c < total; c++) {

                    if (buffer[c] == query[current]) {

                        current++;

                        if (current == query.length) {
                            found.add(address + c - current + 1);
                            current = 0;

                            if (found.size() > 1000) {
                                return found;
                            }
                        }

                    } else {
                        current = 0;
                    }
                }

            }

            address += length;
            pointer = pointer.share(length);

            info = new WinNT.MEMORY_BASIC_INFORMATION();
        }

        return found;
    }

    public int[] pixels(int x, int y, int width, int height) {

        final WinDef.HDC hdcWindow = USER.GetDC(flash);

        final WinDef.HDC hdcMemDC = GDI.CreateCompatibleDC(hdcWindow);

        WinDef.HBITMAP hBitmap = GDI.CreateCompatibleBitmap(hdcWindow, width, height);

        final HANDLE hOld = GDI.SelectObject(hdcMemDC, hBitmap);

        GDI.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, x, y, 0x00CC0020);

        GDI.SelectObject(hdcMemDC, hOld);
        GDI.DeleteDC(hdcMemDC);

        final WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        if (this.allocator.size() < width * height * 4) {
            this.allocator = new Memory(width * height * 4);
        }

        GDI.GetDIBits(hdcWindow, hBitmap, 0, height, allocator, bmi, WinGDI.DIB_RGB_COLORS);

        int size = width * height;

        if (pixels.length < size) {
            pixels = new int[size];
        }

        allocator.read(0, pixels, 0, size);

        GDI.DeleteObject(hBitmap);
        User32.INSTANCE.ReleaseDC(flash, hdcWindow);

        return pixels;
    }

    public boolean toggleBrowser() {
        if (USER.IsWindowVisible(browser)) {
            USER.ShowWindow(browser, 0);
            return true;
        } else {
            USER.ShowWindow(browser, 1);
            USER.SetForegroundWindow(browser);
            return false;
        }
    }

    public void refresh() {
        USER.SendMessage(button, 0x00F5, null, null);
    }
}
