package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.NativeUpdatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.Offsets;
import it.unimi.dsi.fastutil.longs.AbstractLongList;

import java.util.RandomAccess;
import java.util.function.LongConsumer;

public abstract class FlashListLong extends AbstractLongList implements NativeUpdatable, RandomAccess {

    protected long[] elements = new long[0];

    private long address;
    private int size;
    private long lastPointer;

    private boolean autoUpdate = true;

    public static FlashListLong ofArray() {
        return new FlashArray();
    }

    public static FlashListLong ofVector() {
        return new FlashVector();
    }

    public static FlashListLong ofSprite() {
        return new SpriteList();
    }

    public FlashListLong noAuto() {
        this.autoUpdate = false;
        return this;
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public void update(long address) {
        if (this.address != address) setSize(0);
        this.address = address;
        if (autoUpdate) update();
    }

    @Override
    public abstract void update();

    @Override
    public long getLong(int index) {
        if (index < 0 || index >= size()) return 0L;
        return elements[index];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int indexOf(long k) {
        for (int i = 0; i < size(); i++) {
            if (k == getLong(i)) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(long k) {
        for (int i = size() - 1; i >= 0; i--)
            if (k == getLong(i)) return i;
        return -1;
    }

    public void forEachIncremental(LongConsumer consumer) {
        for (int i = lastIndexOf(lastPointer) + 1; i < size(); i++)
            consumer.accept(lastPointer = getLong(i));
    }

    public long getLastElement() {
        return getLong(size() -1);
    }

    protected void setSize(int size) {
        this.size = size;
    }

    protected boolean insufficientCapacity(int size) {
        if (size <= 0 || size > FlashList.MAX_SIZE) return true;
        // don't need a copy, just create new
        if (size > elements.length)
            elements = new long[Math.min((int) (size * 1.25), FlashList.MAX_SIZE)];
        return false;
    }

    protected void update(int sizeOffset, int tableOffset, int tableSkip, boolean skipHoles) {
        if (getAddress() == 0) return;

        int size = readInt(sizeOffset);
        if (insufficientCapacity(size)) {
            setSize(0);
        } else {
            int maxLength = size * 8;
            long table = readLong(tableOffset) + tableSkip;

            int realSize = 0;
            long[] elements = this.elements;
            for (int offset = 0; offset < maxLength; offset += 8) {
                long atom = Main.API.readLong(table, offset);
                // hole in the array -> skip
                if (skipHoles && atom == 0) continue;
                elements[realSize++] = atom & ByteUtils.ATOM_MASK;
            }
            setSize(realSize);
        }
    }

    private static class FlashArray extends FlashListLong {
        @Override
        public void update() {
            update(40, 32, 16, true);
        }
    }

    private static class FlashVector extends FlashListLong {
        @Override
        public void update() {
            update(56, 48, 16, false);
        }
    }

    private static class SpriteList extends FlashListLong {
        @Override
        public void update() {
            int osOffset = Offsets.SPRITE_OFFSET;
            update(24 + osOffset, 8 + osOffset, 8, false);
        }

        @Override
        public void update(long address) {
            super.update(Main.API.readLong(address, 72, 64));
        }
    }
}
