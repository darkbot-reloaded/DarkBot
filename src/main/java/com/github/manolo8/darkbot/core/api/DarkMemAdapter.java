package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.gui.utils.PidSelector;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.DarkInput;
import eu.darkbot.api.DarkMem;

import javax.swing.*;
import java.util.function.BooleanSupplier;

public class DarkMemAdapter extends NoopApiAdapter {
    private final DarkMem MEM = new DarkMem();
    private final DarkInput INPUT = new DarkInput();

    private int pid;
    private boolean windowOpen = false;

    public DarkMemAdapter(StartupParams params, BooleanSupplier fullyHide) {
        super(params, fullyHide);
    }

    @Override
    public void createWindow() {
        PidSelector pidSelector = new PidSelector(MEM.getProcesses());

        int result = JOptionPane.showOptionDialog(HeroManager.instance.main.getGui(), pidSelector,
                "Select flash process", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            this.pid = pidSelector.getPid();
        } catch (NumberFormatException e) {
            Popups.showMessageAsync("Error, invalid PID",
                    "Invalid PID, expected a number", JOptionPane.ERROR_MESSAGE);
        }

        MEM.openProcess(this.pid);
        windowOpen = false;
    }

    @Override
    public boolean isValid() {
        return pid != 0;
    }

    @Override
    public String getVersion() {
        return MEM.getVersion() + "m " + INPUT.getVersion() + "i";
    }

    private boolean inputReady() {
        if (windowOpen) return true;
        if (MapManager.clientWidth == 0 && MapManager.clientHeight == 0) return false;

        return windowOpen = INPUT.openWindow(pid, MapManager.clientWidth, MapManager.clientHeight);
    }

    @Override
    public void mouseMove(int x, int y) {
        if (inputReady()) INPUT.mouseMove(x, y);
    }


    @Override
    public void mouseDown(int x, int y) {
        if (inputReady()) INPUT.mouseDown(x, y);
    }

    @Override
    public void mouseUp(int x, int y) {
        if (inputReady()) INPUT.mouseUp(x, y);
    }

    @Override
    public void mouseClick(int x, int y) {
        if (inputReady()) {
            INPUT.mouseClick(x, y);
            // FIXME: clicks should be sync instead of sleeping, however, chrome handles events on its own.
            Time.sleep(75);
        }
    }

    @Override
    public void rawKeyboardClick(char btn) {
        if (inputReady()) INPUT.keyClick(btn);
    }

    @Override
    public void sendText(String str) {
        if (inputReady()) INPUT.sendText(str);
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
        resetCache();
    }

}