package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkMem;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class DarkMemAdapter extends NoopApiAdapter {
    private final DarkMem MEM = new DarkMem();

    private int pid;

    public DarkMemAdapter(StartupParams params, BooleanSupplier fullyHide) {
        super(params, fullyHide);
    }

    @Override
    public void createWindow() {
        Map<Integer, String> procs = Arrays.stream(MEM.getProcesses())
                .collect(Collectors.toMap(DarkMem.Proc::getPid, DarkMem.Proc::getName));

        JComboBox<Integer> pidSelector = new JComboBox<>(procs.keySet().toArray(new Integer[0]));
        pidSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String str = value == null ? null : procs.get((Integer) value);
                Object val = str == null ? value : value + " - " + str;
                return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
            }
        });
        pidSelector.setEditable(true);

        int result = JOptionPane.showOptionDialog(HeroManager.instance.main.getGui(), pidSelector,
                "Select flash process", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);

        if (result != JOptionPane.OK_OPTION) return;

        Object pid = pidSelector.getSelectedItem();
        if (pid == null) return;
        try {
            if (pid instanceof String)
                pid = Integer.parseInt((String) pid);
        } catch (NumberFormatException e) {
            Popups.showMessageAsync("Error, invalid PID",
                    "Invalid PID " + pid + ", expected a number", JOptionPane.ERROR_MESSAGE);
        }

        MEM.openProcess(this.pid = (Integer) pid);
    }

    @Override
    public boolean isValid() {
        return pid != 0;
    }

    @Override
    public int getVersion() {
        return MEM.getVersion();
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