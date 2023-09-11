package com.github.manolo8.darkbot.gui.utils.highlight;

import lombok.RequiredArgsConstructor;

import javax.swing.text.Position;

public interface Locatable {
    Position getStart();
    Position getEnd();

    static Locatable of(int start, int end) {
        return new Impl(start, end);
    }

    @RequiredArgsConstructor
    class Impl implements Locatable {
        private final int start, end;

        @Override
        public Position getStart() {
            return () -> start;
        }

        @Override
        public Position getEnd() {
            return () -> end;
        }
    }
}
