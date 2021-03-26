package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.utils.StartupParams;

import java.util.function.BooleanSupplier;

/**
 * No-operation API adapter. Will do nothing. Useful for testing purposes, and as fallback if no API is loaded.
 */
public class NoopApiAdapter extends ApiAdapter {

    public NoopApiAdapter(StartupParams params, BooleanSupplier fullyHide) {
        super(params, fullyHide);
    }

    @Override
    public void createWindow() {
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public String getVersion() {
        return "no-op";
    }

    @Override
    public void sendText(String string) {
    }

    @Override
    public void setVisible(boolean visible) {
    }

    @Override
    public void mouseMove(int x, int y) {
    }

    @Override
    public void mouseDown(int x, int y) {
    }

    @Override
    public void mouseUp(int x, int y) {
    }

    @Override
    public void mouseClick(int x, int y) {
    }

    @Override
    public void rawKeyboardClick(char btn) {
    }

    @Override
    public double readMemoryDouble(long address) {
        return 0;
    }

    @Override
    public long readMemoryLong(long address) {
        return 0;
    }

    @Override
    public int readMemoryInt(long address) {
        return 0;
    }

    @Override
    public boolean readMemoryBoolean(long address) {
        return false;
    }

    @Override
    public byte[] readMemory(long address, int length) {
        return new byte[0];
    }

    @Override
    public void readMemory(long address, byte[] buffer, int length) {
    }

    @Override
    public void writeMemoryDouble(long address, double value) {
    }

    @Override
    public void writeMemoryLong(long address, long value) {
    }

    @Override
    public void writeMemoryInt(long address, int value) {
    }

    @Override
    public long[] queryMemoryInt(int value, int maxQuantity) {
        return new long[0];
    }

    @Override
    public long[] queryMemoryLong(long value, int maxQuantity) {
        return new long[0];
    }

    @Override
    public long[] queryMemory(byte[] query, int maxQuantity) {
        return new long[0];
    }

    @Override
    public void handleRefresh() {
        resetCache();
    }
}
