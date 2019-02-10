package com.github.manolo8.darkbot.config;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ZoneInfo implements Serializable {
    private static final byte[] MASKS = new byte[8];
    static {
        for (int i = 0; i < MASKS.length; i++) MASKS[i] = (byte) (1 << i);
    }

    public int resolution;
    private byte[] data;
    private transient List<Zone> zones;
    private transient boolean changed = true;

    public ZoneInfo() {}

    public ZoneInfo(int resolution) {
        this.resolution = resolution;
        data = new byte[((resolution * resolution) + 7) / 8];
    }

    public void setResolution(int resolution) {
        if (resolution == this.resolution) return;
        ZoneInfo newZone = new ZoneInfo(resolution);

        for (int x = 0; x < resolution; x++)
            for (int y = 0; y < resolution; y++)
                if (get(x, y)) newZone.set(x, y);

        this.resolution = resolution;
        this.data = newZone.data;
    }

    public boolean get(int x, int y) {
        if (outside(x, y)) return false;
        int pos = x + (y * resolution);
        return (data[pos / 8] & MASKS[pos % 8]) != 0;
    }

    private void set(int x, int y) {
        if (outside(x, y)) return;
        int pos = x + (y * resolution);
        data[pos / 8] |= MASKS[pos % 8];
        changed = true;
    }

    private void remove(int x, int y) {
        if (outside(x, y)) return;
        int pos = x + (y * resolution);
        data[pos / 8] &= ~MASKS[pos % 8];
        changed = true;
    }

    private void toggle(int x, int y) {
        if (outside(x, y)) return;
        int pos = x + (y * resolution);
        data[pos / 8] ^= MASKS[pos % 8];
        changed = true;
    }

    private void set(int x, int y, int x2, int y2) {
        for (; x < x2; x++) for (int currY = y; currY < y2; currY++) set(x, currY);
    }

    private void remove(int x, int y, int x2, int y2) {
        for (; x < x2; x++) for (int currY = y; currY < y2; currY++) remove(x, currY);
    }

    public void toggle(int x, int y, int x2, int y2) {
        for (; x < x2; x++) for (int currY = y; currY < y2; currY++) toggle(x, currY);
    }

    public void set(int x, int y, int x2, int y2, boolean state) {
        if (state) set(x, y, x2, y2);
        else remove(x, y, x2, y2);
    }

    private boolean outside(int x, int y) {
        return x < 0 || y < 0 || x >= resolution || y >= resolution;
    }

    public List<Zone> getZones() {
        if (changed) {
            zones = new ArrayList<>();
            for (int x = 0; x < resolution; x++) {
                for (int y = 0; y < resolution; y++)
                    if (get(x, y)) zones.add(new Zone(x, y));
            }
            changed = false;
        }
        return zones;
    }

    public class Zone {
        public int x, y;
        public Zone(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}
