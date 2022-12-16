package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.HeroManager;
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

    enum Capability {
        LOGIN,
        ATTACH,
        INITIALLY_SHOWN,
        CREATE_WINDOW_THREAD,
        BACKGROUND_ONLY,

        ALL_KEYBINDS_SUPPORT, // Support for key clicks like "ctrl"

        PROXY,
        WINDOW_POSITION,

        HANDLER_CLEAR_CACHE,
        HANDLER_CLEAR_RAM,
        HANDLER_GAME_QUALITY,
        HANDLER_VOLUME,
        HANDLER_TRANSPARENCY,
        HANDLER_CLIENT_SIZE,
        HANDLER_MIN_CLIENT_SIZE,
        HANDLER_FLASH_PATH,
        HANDLER_INTERNET_READ_TIME,

        DIRECT_USE_ITEM,
        DIRECT_LIMIT_FPS,
        DIRECT_ENTITY_LOCK,
        DIRECT_ENTITY_SELECT,
        DIRECT_MOVE_SHIP,
        DIRECT_COLLECT_BOX,
        DIRECT_REFINE,
        DIRECT_CALL_METHOD,
        DIRECT_POST_ACTIONS
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

    class NoOpWindow implements Window {
        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public void createWindow() {
        }
    }

    class NoOpHandler implements Handler {
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
        public double getCpuUsage() {
            return 0;
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

    class NoOpMemory implements Memory {
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

    class NoOpExtraMemoryReader implements ExtraMemoryReader {
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

    class NoOpInteraction implements Interaction {
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
    
    class NoOpDirectInteraction implements DirectInteraction {
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
        public void refine(long refineUtilAddress, OreAPI.Ore oreType, int amount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void collectBox(Box box) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long callMethod(int index, long... arguments) {
            throw new UnsupportedOperationException();
        }
    }

}
