package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.DarkBotAPI;
import com.github.manolo8.darkbot.utils.Time;

class DarkbotApiAdapter extends ApiAdapter {

    private DarkBotAPI API = new DarkBotAPI();

    @Override
    public void createWindow() {
        Thread apiThread = new Thread(() -> {
            API.createWindow0();
            System.out.println("Browser window exited, exiting");
            System.exit(0);
        }, "BotBrowser");
        apiThread.setDaemon(true);
        apiThread.start();

        Thread windowFinder = new Thread(() -> {
            while ((window = USER_32.FindWindow("DarkBrowser", "DarkBrowser")) == null || !USER_32.IsWindow(window)) Time.sleep(100);
        });
        windowFinder.setDaemon(true);
        windowFinder.start();
    }

    @Override
    public boolean isValid() {
        return API.isValid();
    }

    @Override
    public void mouseMove(int x, int y) {
        API.mouseMove(x, y);
    }

    @Override
    public void mouseClick(int x, int y) {
        API.mouseClick(x, y);
    }

    @Override
    public void keyboardClick(char btn) {
        API.keyboardClick(btn);
    }

    @Override
    public void sendText(String string) {}

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
        return API.readMemoryBoolean(address);
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
    public void setRender(boolean visible) {}

    @Override
    public void handleRefresh() {
        USER_32.SetForegroundWindow(window);
        API.refresh();
    }

}
