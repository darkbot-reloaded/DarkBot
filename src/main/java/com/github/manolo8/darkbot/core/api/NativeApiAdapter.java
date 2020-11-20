package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.login.LoginUtils;
import eu.darkbot.api.NativeApi;

public class NativeApiAdapter extends ApiAdapter {

    private final NativeApi API = new NativeApi();

    private static int nextBotId = 0;
    private int botId = -1;

    public NativeApiAdapter(StartupParams params) {
        super(LoginUtils.performUserLogin(params));
    }

    public void createWindow() {
        botId = nextBotId++;

        if (!API.createBot(botId))
            throw new IllegalStateException("The bot could not successfully setup the browser window");

        setData();
    }

    protected void setData() {
        API.sendMessage(botId, Headers.LOGIN, loginData.getUrl().split("\\.")[0], loginData.getSid());
    }

    public boolean isValid() {
        return API.isValid(botId);
    }

    public void mousePress(int x, int y) {
        API.sendMessage(botId, Headers.MOUSE, MouseEvent.DOWN, x, y);
    }

    public void mouseMove(int x, int y) {
        API.sendMessage(botId, Headers.MOUSE, MouseEvent.MOVE, x, y);
    }

    public void mouseRelease(int x, int y) {
        API.sendMessage(botId, Headers.MOUSE, MouseEvent.UP, x, y);
    }

    public void mouseClick(int x, int y) {
        API.mouseClick(botId, 50, x, y);
    }

    public void keyboardClick(char btn) {
        API.sendMessage(botId, Headers.KEYBOARD, KeyEvent.CLICK, btn);
    }

    public void setVisible(boolean visible) {
        API.sendMessage(botId, visible ? Headers.SHOW : Headers.HIDE);
    }

    public void handleRefresh() {
        relogin();
        setData();
        API.sendMessage(botId, Headers.RELOAD);
    }

    public void blockUserInput(boolean block) {
        API.sendMessage(botId, Headers.BLOCK_INPUT, block);
    }

    public long[] queryMemoryInt(int value, int max) {
        return API.queryMemoryInt(botId, value, max);
    }

    public long[] queryMemoryLong(long value, int max) {
        return API.queryMemoryLong(botId, value, max);
    }

    public long[] queryMemory(byte[] query, int max) {
        return API.queryMemory(botId, query, max);
    }

    public byte[] readMemory(long address, int length) {
        return API.readMemory(botId, address, length);
    }

    public int readMemoryInt(long address) {
        return API.readMemoryInt(botId, address);
    }

    public long readMemoryLong(long address) {
        return API.readMemoryLong(botId, address);
    }

    public double readMemoryDouble(long address) {
        return API.readMemoryDouble(botId, address);
    }

    public boolean readMemoryBoolean(long address) {
        return API.readMemoryBoolean(botId, address);
    }

    public void writeMemoryInt(long address, int value) {
        API.writeMemoryInt(botId, address, value);
    }

    public void writeMemoryLong(long address, long value) {
        API.writeMemoryLong(botId, address, value);
    }

    public void writeMemoryDouble(long address, double value) {
        API.writeMemoryDouble(botId, address, value);
    }

    public enum Headers {
        LOGIN, RELOAD, MOUSE, KEYBOARD, SHOW, HIDE, BLOCK_INPUT;

        @Override
        public String toString() {
            return ordinal() + "";
        }
    }

    public enum KeyEvent {
        DOWN, UP, CLICK;

        @Override
        public String toString() {
            return (ordinal() + 1) + "";
        }
    }

    public enum MouseEvent {
        MOVE, DOWN, UP, CLICK;

        @Override
        public String toString() {
            return (ordinal() + 1) + "";
        }
    }

}
