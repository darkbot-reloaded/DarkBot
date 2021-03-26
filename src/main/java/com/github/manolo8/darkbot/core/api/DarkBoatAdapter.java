package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkBoat;

import java.util.function.BooleanSupplier;

public class DarkBoatAdapter extends ApiAdapter {
    private final DarkBoat API = new DarkBoat();

    public DarkBoatAdapter(StartupParams params, BooleanSupplier fullyHide) {
        super(params, fullyHide);
    }

    @Override
    public void createWindow() {
        setData();
        Thread apiThread = new Thread(API::createWindow);
        apiThread.setDaemon(true);
        apiThread.start();
    }

    protected void setData() {
        String url = "https://" + loginData.getUrl() + "/",
                sid = "dosid=" + loginData.getSid();

        API.setData(url, sid, loginData.getPreloaderUrl(), loginData.getParams());
    }

    @Override
    public void setSize(int width, int height) {
        API.setSize(width, height);
    }

    @Override
    public void setVisible(boolean visible) {
        API.setVisible(visible);
    }

    @Override
    public void setMinimized(boolean visible) {
        API.setMinimized(visible);
    }

    @Override
    public boolean isValid() {
        return super.tryHideIfValid(API.isValid());
    }

    @Override
    public long getMemoryUsage() {
        return API.getMemoryUsage();
    }

    @Override
    public String getVersion() {
        return String.valueOf(API.getVersion());
    }

    @Override
    public void mouseMove(int x, int y) {
        API.mouseMove(x, y);
    }

    @Override
    public void mouseDown(int x, int y) {
        API.mouseDown(x, y);
    }

    @Override
    public void mouseUp(int x, int y) {
        API.mouseUp(x, y);
    }

    @Override
    public void mouseClick(int x, int y) {
        API.mouseClick(x, y);
    }

    @Override
    public void rawKeyboardClick(char btn) {
        API.keyClick(btn);
    }

    @Override
    public void sendText(String str) {
        API.sendText(str);
    }

    @Override
    public double readMemoryDouble(long address) {
        return API.readDouble(address);
    }

    @Override
    public long readMemoryLong(long address) {
        return API.readLong(address);
    }

    @Override
    public int readMemoryInt(long address) {
        return API.readInt(address);
    }

    @Override
    public boolean readMemoryBoolean(long address) {
        return API.readBoolean(address);
    }

    @Override
    public byte[] readMemory(long address, int length) {
        return API.readBytes(address, length);
    }

    @Override
    public void readMemory(long address, byte[] buffer, int length) {
        API.readBytes(address, buffer, length);
    }

    @Override
    public void writeMemoryDouble(long address, double value) {
        API.writeDouble(address, value);
    }

    @Override
    public void writeMemoryLong(long address, long value) {
        API.writeLong(address, value);
    }

    @Override
    public void writeMemoryInt(long address, int value) {
        API.writeInt(address, value);
    }

    @Override
    public void replaceInt(long addr, int oldValue, int newValue) {
        API.replaceInt(addr, oldValue, newValue);
    }

    @Override
    public long[] queryMemoryInt(int value, int maxQuantity) {
        return API.queryInt(value, maxQuantity);
    }

    @Override
    public long[] queryMemoryLong(long value, int maxQuantity) {
        return API.queryLong(value, maxQuantity);
    }

    @Override
    public long[] queryMemory(byte[] query, int maxQuantity) {
        return API.queryBytes(query, maxQuantity);
    }

    @Override
    public void handleRefresh() {
        relogin();
        setData();
        API.reload();
        resetCache();
    }
}