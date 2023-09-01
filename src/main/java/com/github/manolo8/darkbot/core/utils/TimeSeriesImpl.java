package com.github.manolo8.darkbot.core.utils;

import eu.darkbot.api.managers.StatsAPI;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

public class TimeSeriesImpl implements StatsAPI.TimeSeries {
    private static final int MAX_SIZE = 1024;
    private static final int MIN_SIZE = 16;
    private long[] t = new long[MIN_SIZE];
    private double[] v = new double[MIN_SIZE];
    private int size = 0;

    private final ValueList vl = new ValueList();
    private final TimeList tl = new TimeList();

    // What's the maximum error we've already removed? It is acceptable to remove stuff under this again
    private double maxErr = Double.MIN_VALUE;

    @Override
    public List<Long> time() {
        return tl;
    }

    @Override
    public List<Double> value() {
        return vl;
    }

    public void track(double value) {
        long now = System.currentTimeMillis();
        // Can we replace previous point with the newly added one?
        if (size > 1 && err(v[size - 2], v[size - 1], value, t[size - 2], t[size - 1], now) <= maxErr) {
            v[size - 1] = value;
            t[size - 1] = now;
            return;
        }
        ensureSlot();
        t[size] = System.currentTimeMillis();
        v[size] = value;
        size++;
    }

    private void ensureSlot() {
        // No action required
        if (size < t.length) return;

        // 16 -> 64 -> 256 -> 1024
        if (t.length < MAX_SIZE) {
            int newSize = t.length << 2;
            t = Arrays.copyOf(t, newSize);
            v = Arrays.copyOf(v, newSize);
            return;
        }

        // Time to compact the time series!
        double smallestErr = Double.POSITIVE_INFINITY;
        int smallestErrIdx = 0; // Worst case scenario, remove oldest datapoint
        for (int i = 1; i < t.length - 1; i++) {
            double err = err(v[i - 1], v[i], v[i + 1], t[i - 1], t[i], t[i + 1]);
            if (err < smallestErr) {
                smallestErr = err;
                smallestErrIdx = i;
            }
        }
        remove(smallestErrIdx);
        // Accept up to error +20%, avoids re-locations
        if (smallestErrIdx != 0) maxErr = smallestErr * 1.2;
    }

    private void remove(int i) {
        size--;
        System.arraycopy(t, i + 1, t, i, size - i);
        System.arraycopy(v, i + 1, v, i, size - i);
    }

    /**
     * Calculate the error that would be introduced, if point b was removed from the graph.
     * This is analogous to, what's the area of the triangle defined between points a, b and c
     */
    private double err(double ax, double bx, double cx, long ay, long by, long cy) {
        return Math.abs((ax - cx) * (by - ay) - (ax - bx) * (cy - ay));
    }

    private class ValueList extends AbstractList<Double> {
        @Override
        public Double get(int index) {
            return v[index];
        }

        @Override
        public int size() {
            return size;
        }
    }

    private class TimeList extends AbstractList<Long> {
        @Override
        public Long get(int index) {
            return t[index];
        }

        @Override
        public int size() {
            return size;
        }
    }

}
