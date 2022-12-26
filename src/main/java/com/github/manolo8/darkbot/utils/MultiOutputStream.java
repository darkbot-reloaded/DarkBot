package com.github.manolo8.darkbot.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class MultiOutputStream extends OutputStream {

    private final OutputStream first;
    private final OutputStream second;

    public MultiOutputStream(OutputStream first, OutputStream second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void write(int b) throws IOException {
        first.write(b);
        second.write(b);
    }

    @Override
    public void write(byte @NotNull [] b) throws IOException {
        first.write(b);
        second.write(b);
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        first.write(b, off, len);
        second.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        first.flush();
        second.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            first.close();
        } finally {
            second.close();
        }
    }
}
