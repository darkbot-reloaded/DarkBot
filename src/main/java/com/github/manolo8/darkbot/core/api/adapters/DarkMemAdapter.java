package com.github.manolo8.darkbot.core.api.adapters;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.api.GameAPIImpl;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.DarkInput;
import eu.darkbot.api.DarkMem;

public class DarkMemAdapter extends GameAPIImpl<DarkMem, DarkMemAdapter.DarkMemHandler, DarkMem,
        ByteUtils.ExtraMemoryReader, DarkMemAdapter.DarkMemInput, NoopAPIAdapter.NoOpDirectInteraction> {

    private final DarkMem MEM = new DarkMem();
    private final DarkInput INPUT = new DarkInput();

    public DarkMemAdapter(StartupParams params, DarkMem mem, BotInstaller botInstaller) {
        super(params,
                mem,
                new DarkMemHandler(),
                mem,
                new ByteUtils.ExtraMemoryReader(mem, botInstaller),
                new DarkMemInput(),
                new NoopAPIAdapter.NoOpDirectInteraction(), Capability.ATTACH);
    }

    @Override
    public void createWindow() {
        super.createWindow();

        handler.setPid(pid);
        interaction.setPid(pid);
    }

    @Override
    public String getVersion() {
        return MEM.getVersion() + "m " + INPUT.getVersion() + "i";
    }

    @Override
    public void handleRefresh() {
        resetCache();
    }

    protected static class DarkMemHandler extends NoopAPIAdapter.NoOpHandler {
        private int pid;

        public void setPid(int pid) {
            this.pid = pid;
        }

        public boolean isValid() {
            return pid != 0;
        }
    }

    protected static class DarkMemInput implements GameAPI.Interaction {
        private final DarkInput input = new DarkInput();
        private int pid;
        private boolean windowOpen;

        public void setPid(int pid) {
            this.pid = pid;
            MapManager.clientWidth = 0;
            MapManager.clientHeight = 0;
            this.windowOpen = false;
        }

        private boolean inputReady() {
            if (windowOpen) return true;
            if (MapManager.clientWidth == 0 && MapManager.clientHeight == 0) return false;

            return windowOpen = input.openWindow(pid, MapManager.clientWidth, MapManager.clientHeight);
        }

        @Override
        public int getVersion() {
            return input.getVersion();
        }

        @Override
        public void keyClick(int keyCode) {
            if (inputReady()) input.keyClick(keyCode);
        }

        @Override
        public void sendText(String text) {
            if (inputReady()) input.sendText(text);
        }

        @Override
        public void mouseMove(int x, int y) {
            if (inputReady()) input.mouseMove(x, y);
        }

        @Override
        public void mouseDown(int x, int y) {
            if (inputReady()) input.mouseDown(x, y);
        }

        @Override
        public void mouseUp(int x, int y) {
            if (inputReady()) input.mouseUp(x, y);
        }

        @Override
        public void mouseClick(int x, int y) {
            if (inputReady()) {
                input.mouseClick(x, y);
                // FIXME: clicks should be sync instead of sleeping, however, chrome handles events on its own.
                Time.sleep(75);
            }
        }

    }

}