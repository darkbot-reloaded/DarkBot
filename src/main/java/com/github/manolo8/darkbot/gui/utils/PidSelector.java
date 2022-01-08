package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.core.api.GameAPI;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PidSelector extends JComboBox<Integer> {

    public PidSelector(GameAPI.Window.Proc[] procs) {
        super(Arrays.stream(procs).map(GameAPI.Window.Proc::getPid).toArray(Integer[]::new));

        setRenderer(new PidRenderer(Arrays.stream(procs)
                .collect(Collectors.toMap(GameAPI.Window.Proc::getPid, GameAPI.Window.Proc::getName))));

        setEditable(true);
    }

    public Integer getPid() throws NumberFormatException {
        Object pid = getSelectedItem();
        if (pid == null) return null;
        if (pid instanceof String) return Integer.parseInt((String) pid);
        return (Integer) pid;
    }

    private static class PidRenderer extends DefaultListCellRenderer {

        private final Map<Integer, String> procs;

        public PidRenderer(Map<Integer, String> procs) {
            this.procs = procs;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String str = value == null ? null : procs.get((Integer) value);
            Object val = str == null ? value : value + " - " + str;
            return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
        }
    }
}
