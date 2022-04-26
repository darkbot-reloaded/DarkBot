package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.api.util.ByteBufferReader;
import com.github.manolo8.darkbot.core.api.util.DataReader;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkBoat;

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
            return new DarkBoatByteBufferReader(idx, memory, extraMemoryReader);

        return super.createReader(idx);
    }

    static class DarkBoatByteBufferReader extends ByteBufferReader implements DataReader {

        private final int idx;
        private final DarkBoat darkBoat;

        public DarkBoatByteBufferReader(int idx, DarkBoat darkBoat, GameAPI.ExtraMemoryReader reader) {
            super(darkBoat.getBuffer(idx), reader);
            this.idx = idx;
            this.darkBoat = darkBoat;
        }

        @Override
        public Result read(long address, int length) {
            if (!inUse.compareAndSet(false, true)) return DataReader.Result.BUSY;

            boolean res = darkBoat.readToBuffer(idx, address, length);
            if (!res) return Result.ERROR;

            reset(length);
            return Result.OK;
        }

        @Override
        public byte[] toArray() {
            return getArray(new byte[getAvailable()], 0, getAvailable());
        }
    }
}