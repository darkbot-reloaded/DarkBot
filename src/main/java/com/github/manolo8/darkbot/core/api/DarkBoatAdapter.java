package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.api.util.AbstractDataReader;
import com.github.manolo8.darkbot.core.api.util.DataReader;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkBoat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DarkBoatAdapter extends GameAPIImpl<
        DarkBoat,
        DarkBoat,
        DarkBoat,
        ByteUtils.ExtraMemoryReader,
        DarkBoat,
        DarkBoatAdapter.DarkBoatDirectInteraction> {

    public DarkBoatAdapter(StartupParams params, DarkBoatDirectInteraction di, DarkBoat darkboat, BotInstaller botInstaller) {
        super(params,
                darkboat,
                darkboat,
                darkboat,
                new ByteUtils.ExtraMemoryReader(darkboat, botInstaller),
                darkboat,
                di,
                GameAPI.Capability.LOGIN,
                GameAPI.Capability.INITIALLY_SHOWN,
                GameAPI.Capability.CREATE_WINDOW_THREAD, GameAPI.Capability.DIRECT_LIMIT_FPS);
    }

    @Override
    public String getVersion() {
        return "darkboat-" + window.getVersion();
    }

    public static class DarkBoatDirectInteraction extends GameAPI.NoOpDirectInteraction {
        private final DarkBoat darkboat;

        public DarkBoatDirectInteraction(DarkBoat darkboat) {
            this.darkboat = darkboat;
        }

        @Override
        public void setMaxFps(int maxFps) {
            int version = darkboat.getVersion();
            if (version >= 8) darkboat.setMaxFps(maxFps);
            else System.out.println("FPS limiting in darkboat is only available in version 8+, you are using version " + version);
        }
    }

    @Override
    protected DataReader createReader(int idx) {
        if (window.getVersion() >= 9)
            return new DarkBoatDataReader(idx, memory, extraMemoryReader);

        return super.createReader(idx);
    }

    static class DarkBoatDataReader extends AbstractDataReader {

        private final int idx;
        private final ByteBuffer byteBuffer;
        private final DarkBoat darkBoat;

        public DarkBoatDataReader(int idx, DarkBoat darkBoat, GameAPI.ExtraMemoryReader reader) {
            super(reader);
            this.idx = idx;
            this.darkBoat = darkBoat;

            ByteBuffer[] buffers = darkBoat.buffers;
            ByteBuffer buffer = buffers[idx];

            if (buffer == null)
                buffers[idx] = buffer = ByteBuffer.allocateDirect(DataReader.MAX_CHUNK_SIZE)
                        .order(ByteOrder.nativeOrder());

            this.byteBuffer = buffer;
        }

        @Override
        public ByteBuffer getByteBuffer() {
            return byteBuffer;
        }

        @Override
        public Boolean read(long address, int length) {
            if (!inUse.compareAndSet(false, true)) return false;

            boolean res = darkBoat.readToBuffer(idx, address, length);
            if (!res) return null;

            reset(length);
            return true;
        }

        @Override
        public byte[] toArray() {
            byte[] bytes = new byte[getAvailable()];
            setArray(bytes, 0, getAvailable());

            return bytes;
        }
    }
}