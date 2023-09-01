package com.github.manolo8.darkbot.core.api.adapters;

import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.api.GameAPIImpl;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.OreAPI;

import java.util.function.LongPredicate;

/**
 * No-operation API adapter. Will do nothing. Useful for testing purposes, and as fallback if no API is loaded.
 */
public class NoopAPIAdapter extends GameAPIImpl<
        NoopAPIAdapter.NoOpWindow,
        NoopAPIAdapter.NoOpHandler,
        NoopAPIAdapter.NoOpMemory,
        NoopAPIAdapter.NoOpExtraMemoryReader,
        NoopAPIAdapter.NoOpInteraction,
        NoopAPIAdapter.NoOpDirectInteraction> {

    public NoopAPIAdapter(StartupParams params) {
        super(params,
                new NoOpWindow(),
                new NoOpHandler(),
                new NoOpMemory(),
                new NoOpExtraMemoryReader(),
                new NoOpInteraction(),
                new NoOpDirectInteraction(),
                Capability.BACKGROUND_ONLY);
    }

    @Override
    public String getVersion() {
        return "no-op";
    }

    public static class NoOpWindow implements GameAPI.Window {
        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public void createWindow() {
        }
    }

    public static class NoOpHandler implements GameAPI.Handler {
        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public long getMemoryUsage() {
            return HeroManager.instance.main.facadeManager.stats.getMemory();
        }

        @Override
        public void reload() {
        }

        @Override
        public void setSize(int width, int height) {
        }

        @Override
        public void setVisible(boolean visible) {
        }

        @Override
        public void setMinimized(boolean minimized) {
        }
    }

    public static class NoOpMemory implements GameAPI.Memory {
        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public int readInt(long address) {
            return 0;
        }

        @Override
        public long readLong(long address) {
            return 0;
        }

        @Override
        public double readDouble(long address) {
            return 0;
        }

        @Override
        public boolean readBoolean(long address) {
            return false;
        }

        @Override
        public byte[] readBytes(long address, int length) {
            return new byte[0];
        }

        @Override
        public void readBytes(long address, byte[] buff, int length) {}

        @Override
        public void replaceInt(long address, int oldValue, int newValue) {}

        @Override
        public void replaceLong(long address, long oldValue, long newValue) {}

        @Override
        public void replaceDouble(long address, double oldValue, double newValue) {}

        @Override
        public void replaceBoolean(long address, boolean oldValue, boolean newValue) {}

        @Override
        public void writeInt(long address, int value) {}

        @Override
        public void writeLong(long address, long value) {}

        @Override
        public void writeDouble(long address, double value) {}

        @Override
        public void writeBoolean(long address, boolean value) {}

        @Override
        public void writeBytes(long address, byte... bytes) {}

        @Override
        public long[] queryInt(int value, int maxSize) {
            return new long[0];
        }

        @Override
        public long[] queryLong(long value, int maxSize) {
            return new long[0];
        }

        @Override
        public long[] queryBytes(byte[] pattern, int maxSize) {
            return new long[0];
        }
    }

    public static class NoOpExtraMemoryReader implements GameAPI.ExtraMemoryReader {
        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public long searchClassClosure(LongPredicate pattern) {
            return 0;
        }

        @Override
        public String readString(long address) {
            return null;
        }

        @Override
        public void resetCache() {}
    }

    public static class NoOpInteraction implements GameAPI.Interaction {
        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public void keyClick(int keyCode) {}

        @Override
        public void sendText(String text) {}

        @Override
        public void mouseMove(int x, int y) {}

        @Override
        public void mouseDown(int x, int y) {}

        @Override
        public void mouseUp(int x, int y) {}

        @Override
        public void mouseClick(int x, int y) {}
    }

    public static class NoOpDirectInteraction implements GameAPI.DirectInteraction {
        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public void setMaxFps(int maxFps) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void lockEntity(int id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectEntity(Entity entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void moveShip(Locatable destination) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void collectBox(Box box) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void refine(long refineUtilAddress, OreAPI.Ore oreType, int amount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long callMethod(int index, long... arguments) {
            throw new UnsupportedOperationException();
        }
    }
}
