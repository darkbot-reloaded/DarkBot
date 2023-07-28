package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.OreAPI;
import org.intellij.lang.annotations.Language;

import java.util.function.LongPredicate;

public interface GameAPI {

    interface Base {
        int getVersion();
        default void tick() {}
    }

    interface Window extends Base {

        default void createWindow() {
            throw new UnsupportedOperationException();
        }
        default void setData(String url, String sid, String preloader, String vars) {
            throw new UnsupportedOperationException();
        }

        default Proc[] getProcesses() {
            throw new UnsupportedOperationException();
        }
        default void openProcess(long pid) {
            throw new UnsupportedOperationException();
        }

        interface Proc {
            int getPid();
            String getName();
        }
    }

    interface Handler extends Base {
        boolean isValid();

        long getMemoryUsage();

        default double getCpuUsage() {
            return 0;
        }

        void reload();

        void setSize(int width, int height);

        void setVisible(boolean visible);

        void setMinimized(boolean minimized);

        default void clearCache(@Language("RegExp") String pattern) {}
        default void emptyWorkingSet() {}
        default void setLocalProxy(int port) {}
        default void setPosition(int x, int y) {}
        default void setFlashOcxPath(String path) {}
        default void setUserInput(boolean enableInput) {}
        default void setClientSize(int width, int height) {}
        default void setMinClientSize(int width, int height) {}
        default void setTransparency(int transparency) {}
        default void setVolume(int volume) {} // 0 - 100
        // LOW = 0, MEDIUM = 1, HIGH = 2, BEST = 3, AUTO_LOW = 4, AUTO_HIGH = 5
        default void setQuality(int quality) {}

        default long lastInternetReadTime() {
            return 0;
        }

        enum GameQuality {
            LOW, MEDIUM, HIGH, BEST, AUTO_LOW, AUTO_HIGH;
        }
    }

    interface Memory extends Base {
        int     readInt    (long address);
        long    readLong   (long address);
        double  readDouble (long address);
        boolean readBoolean(long address);
        byte[]  readBytes  (long address, int length);
        void    readBytes  (long address, byte[] buff, int length);

        default int readInt(long address, int... offsets) {
            for (int i = 0; i < offsets.length - 1; i++) address = readLong(address + offsets[i]);
            return readInt(address + offsets[offsets.length - 1]);
        }

        default long readLong(long address, int... offsets) {
            for (int offset : offsets) {
                if (!ByteUtils.isValidPtr(address)) return 0;
                address = readLong(address + offset);
            }
            return address;
        }

        default double readDouble(long address, int... offsets) {
            for (int i = 0; i < offsets.length - 1; i++) address = readLong(address + offsets[i]);
            return readDouble(address + offsets[offsets.length - 1]);
        }

        default boolean readBoolean(long address, int... offsets) {
            for (int i = 0; i < offsets.length - 1; i++) address = readLong(address + offsets[i]);
            return readBoolean(address + offsets[offsets.length - 1]);
        }

        void replaceInt    (long address, int     oldValue, int     newValue);
        void replaceLong   (long address, long    oldValue, long    newValue);
        void replaceDouble (long address, double  oldValue, double  newValue);
        void replaceBoolean(long address, boolean oldValue, boolean newValue);

        void writeInt(long address, int value);
        void writeLong(long address, long value);
        void writeDouble(long address, double value);
        void writeBoolean(long address, boolean value);
        void writeBytes(long address, byte... bytes);

        long[] queryInt(int value, int maxSize);
        long[] queryLong(long value, int maxSize);
        long[] queryBytes(byte[] pattern, int maxSize);

        default long queryBytes(byte... pattern) {
            long[] values = queryBytes(pattern, 1);
            return values.length == 1 ? values[0] : 0;
        }
    }

    interface ExtraMemoryReader extends Base {
        String readString(long address);
        void resetCache();

        long searchClassClosure(LongPredicate pattern);
    }

    interface Interaction extends Base {
        void keyClick  (int keyCode);
        void sendText  (String text);

        void mouseMove (int x, int y);
        void mouseDown (int x, int y);
        void mouseUp   (int x, int y);
        void mouseClick(int x, int y);
    }

    interface DirectInteraction extends Base {
        void setMaxFps(int maxFps);

        void lockEntity(int id);

        void selectEntity(Entity entity);

        void moveShip(Locatable destination);

        void collectBox(Box box);

        void refine(long refineUtilAddress, OreAPI.Ore oreType, int amount);

        long callMethod(int index, long... arguments);

        default boolean callMethodChecked(boolean checkName, String signature, int index, long... arguments) {
            throw new UnsupportedOperationException();
        }

        default boolean callMethodAsync(int index, long... arguments) {
            throw new UnsupportedOperationException();
        }
    }
}
