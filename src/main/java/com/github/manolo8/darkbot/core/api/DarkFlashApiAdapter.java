package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.DarkFlash;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.Time;
import com.github.manolo8.darkbot.utils.login.LoginData;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;

import javax.swing.*;

public class DarkFlashApiAdapter extends ApiAdapter {

    private final LoginData loginData;
    private final DarkFlash API = new DarkFlash();
    private long willBeValid = System.currentTimeMillis() + 5_000;

    public DarkFlashApiAdapter(StartupParams params) {
        this.loginData = LoginUtils.performUserLogin(params);
    }

    @Override
    public void createWindow() {
        IntByReference parentProcessId = new IntByReference();

        String url = "https://" + loginData.getUrl() + "/",
                sid = "dosid=" + loginData.getSid();

        API.setCookie(url, sid);
        showForm();
        Thread apiThread = new Thread(() -> API.loadSWF(loginData.getPreloaderUrl(), loginData.getParams(), url));
        apiThread.setDaemon(true);
        apiThread.start();

        Thread windowFinder = new Thread(() -> {
            while ((window = USER_32.FindWindow(null, "DarkPlayer")) == null ||
                    !USER_32.IsWindow(window) ||
                    USER_32.GetWindowThreadProcessId(window, parentProcessId) == 0 ||
                    parentProcessId.getValue() != Kernel32.INSTANCE.GetCurrentProcessId()) Time.sleep(100);

            API.setVisible(true);
            setSize(-1, -1);
            flash = USER_32.FindWindowEx(USER_32.FindWindowEx(window, null, null, null), null, null, null);
            API.setVisible(true);
            API.setRender(true);
        });
        windowFinder.setDaemon(true);
        windowFinder.start();
    }

    private void showForm() {
        // Credits for the dll:
        JFrame frame = new JFrame("Darkbot - By Manolo8");
        frame.setVisible(true);
        frame.setVisible(false);
    }

    public void setMinimized(boolean visible) {
        API.setVisible(visible);
    }

    @Override
    public boolean isValid() {
        return willBeValid < System.currentTimeMillis();
    }

    // Forces proguard to include the no-param constructor, needed for mouseMove
    static { new WinDef.LRESULT();}
    @Override
    public void mouseMove(int x, int y) {
        USER_32.SendMessage(flash, 0x0200, null, new WinDef.LPARAM(x & 0xFFFF | ((y & 0xFFFF) << 16)));
    }

    @Override
    public void mouseClick(int x, int y) {
        API.mousePress(x, y);
    }

    @Override
    public void keyboardClick(char btn) {
        API.keyPress(btn);
    }

    @Override
    public double readMemoryDouble(long address) {
        return API.readMemoryDouble(address);
    }

    @Override
    public long readMemoryLong(long address) {
        return API.readMemoryLong(address);
    }

    @Override
    public int readMemoryInt(long address) {
        return API.readMemoryInt(address);
    }

    @Override
    public boolean readMemoryBoolean(long address) {
        return this.readMemoryInt(address) == 1;
    }

    @Override
    public byte[] readMemory(long address, int length) {
        return API.readMemory(address, length);
    }

    @Override
    public void writeMemoryDouble(long address, double value) {
        API.writeMemoryDouble(address, value);
    }

    @Override
    public void writeMemoryLong(long address, long value) {
        API.writeMemoryLong(address, value);
    }

    @Override
    public void writeMemoryInt(long address, int value) {
        API.writeMemoryInt(address, value);
    }

    @Override
    public long[] queryMemoryInt(int value, int maxQuantity) {
        return API.queryMemoryInt(value, maxQuantity);
    }

    @Override
    public long[] queryMemoryLong(long value, int maxQuantity) {
        return API.queryMemoryLong(value, maxQuantity);
    }

    @Override
    public long[] queryMemory(byte[] query, int maxQuantity) {
        return API.queryMemory(query, maxQuantity);
    }

    @Override
    public void handleRefresh() {
        willBeValid = System.currentTimeMillis() + 5_000;
        API.reloadSWF();
    }

}
