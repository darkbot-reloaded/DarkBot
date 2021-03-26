package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkCef;
import eu.darkbot.api.DarkMem;

import java.util.function.BooleanSupplier;

public class DarkCefAdapter extends ApiAdapter {
    private final DarkCef CEF = DarkCef.getInstance();
    private final DarkMem MEM = new DarkMem();

    public DarkCefAdapter(StartupParams params, BooleanSupplier fullyHide) {
        super(params, fullyHide);
    }

    @Override
    public void createWindow() {
        setData();
        Thread apiThread = new Thread(CEF::createWindow);
        apiThread.setDaemon(true);
        apiThread.start();
    }

    protected void setData() {
        String url = "https://" + loginData.getUrl() + "/indexInternal.es?action=internalMapRevolution";

        CEF.setData(url, loginData.getSid(), loginData.getPreloaderUrl(), loginData.getParams());
    }

    @Override
    public void setSize(int width, int height) {
        CEF.setSize(width, height);
    }

    @Override
    public String getVersion() {
        return MEM.getVersion() + "m"; // At some point will have to get version of both cef & mem.
    }

    @Override
    public void setVisible(boolean visible) {
        CEF.setVisible(visible);
    }

    @Override
    public void setMinimized(boolean visible) {
        CEF.setMinimized(!visible);
    }

    @Override
    public boolean isValid() {
        boolean isValid = super.tryHideIfValid(CEF.isValid());
        // Java 9 api, will require either CEF or DarkMem to return the flash PID.
        /*if (isValid) {
            List<ProcessHandle> hand = ProcessHandle.current().children()
                    .sorted(Comparator.comparing(p -> p.info().startInstant().orElse(Instant.EPOCH)))
                    .collect(Collectors.toList());

            if (hand.size() != 3) return false;
            MEM.openProcess(hand.get(2).pid());
        }*/
        return isValid;
    }

    @Override
    public void mouseMove(int x, int y) {
        CEF.mouseMove(x, y);
    }

    @Override
    public void mouseDown(int x, int y) {
        CEF.mouseDown(x, y);
    }

    @Override
    public void mouseUp(int x, int y) {
        CEF.mouseUp(x, y);
    }

    @Override
    public void mouseClick(int x, int y) {
        CEF.mouseClick(x, y);
    }

    @Override
    public void rawKeyboardClick(char btn) {
        CEF.keyClick(btn);
    }

    @Override
    public void sendText(String str) {
        CEF.sendText(str);
    }

    @Override
    public double readMemoryDouble(long address) {
        return MEM.readDouble(address);
    }

    @Override
    public long readMemoryLong(long address) {
        return MEM.readLong(address);
    }

    @Override
    public int readMemoryInt(long address) {
        return MEM.readInt(address);
    }

    @Override
    public boolean readMemoryBoolean(long address) {
        return MEM.readBoolean(address);
    }

    @Override
    public byte[] readMemory(long address, int length) {
        return MEM.readBytes(address, length);
    }

    @Override
    public void readMemory(long address, byte[] buffer, int length) {
        MEM.readBytes(address, buffer, length);
    }

    @Override
    public void writeMemoryDouble(long address, double value) {
        MEM.writeDouble(address, value);
    }

    @Override
    public void writeMemoryLong(long address, long value) {
        MEM.writeLong(address, value);
    }

    @Override
    public void writeMemoryInt(long address, int value) {
        MEM.writeInt(address, value);
    }

    @Override
    public void replaceInt(long addr, int oldValue, int newValue) {
        MEM.replaceInt(addr, oldValue, newValue);
    }

    @Override
    public long[] queryMemoryInt(int value, int maxQuantity) {
        return MEM.queryInt(value, maxQuantity);
    }

    @Override
    public long[] queryMemoryLong(long value, int maxQuantity) {
        return MEM.queryLong(value, maxQuantity);
    }

    @Override
    public long[] queryMemory(byte[] query, int maxQuantity) {
        return MEM.queryBytes(query, maxQuantity);
    }

    @Override
    public void handleRefresh() {
        relogin();
        setData();
        CEF.reload();
        resetCache();
    }

}