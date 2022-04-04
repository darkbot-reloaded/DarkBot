package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.DarkPlayer;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.managers.HeroAPI;

import java.util.UUID;

public class DarkPlayerAdapter extends GameAPIImpl<
        DarkPlayerAdapter.DarkPlayerWindow,
        DarkPlayerAdapter.DarkPlayerWindow,
        DarkPlayer,
        ByteUtils.StringReader,
        DarkPlayer,
        DarkPlayerAdapter.DarkPlayerDirectInteraction> {

    public DarkPlayerAdapter(StartupParams params, DarkPlayerWindow dpw, DarkPlayerDirectInteraction di, DarkPlayer darkPlayer, Main main) {
        super(params,
                dpw,
                dpw,
                darkPlayer,
                new ByteUtils.StringReader(darkPlayer),
                darkPlayer,
                di,
                GameAPI.Capability.LOGIN,
                GameAPI.Capability.INITIALLY_SHOWN,
                //--
                GameAPI.Capability.DIRECT_LIMIT_FPS,
                GameAPI.Capability.DIRECT_ENTITY_LOCK,
                GameAPI.Capability.DIRECT_MOVE_SHIP,
                GameAPI.Capability.DIRECT_COLLECT_BOX);

        dpw.setDarkPlayerAdapter(this);
        dpw.setMain(main);
    }

    @Override
    public String getVersion() {
        return "darkplayer-" + window.getVersion();
    }

    public static class DarkPlayerWindow implements GameAPI.Window, GameAPI.Handler {
        private final DarkPlayer darkPlayer;
        private DarkPlayerAdapter darkPlayerAdapter;
        private Main main;

        private boolean isProcessOpen = false;

        public DarkPlayerWindow(DarkPlayer darkPlayer) {
            this.darkPlayer = darkPlayer;
        }

        public void setDarkPlayerAdapter(DarkPlayerAdapter darkPlayerAdapter) {
            this.darkPlayerAdapter = darkPlayerAdapter;
        }

        public void setMain(Main main) {
            this.main = main;
        }

        @Override
        public int getVersion() {
            return darkPlayer.getVersion();
        }

        @Override
        public void setData(String url, String sid, String preloader, String vars) {
            darkPlayer.setData(url, sid, preloader, vars);
        }

        @Override
        public void createWindow() {
            if (!isProcessOpen) {
                isProcessOpen = darkPlayer.createProcess("./lib/DarkPlayer.exe", UUID.randomUUID().toString());
                if (isProcessOpen) {
                    darkPlayerAdapter.setData();
                    darkPlayer.createWindow();
                    darkPlayer.setMaxFps(main.config.BOT_SETTINGS.API_CONFIG.MAX_FPS);
                }
            }
        }

        @Override
        public boolean isValid() {
            if (!darkPlayer.isAlive()) {
                isProcessOpen = false;
                this.createWindow();
                return false;
            }
            return darkPlayer.isValid();
        }

        @Override
        public long getMemoryUsage() {
            return darkPlayer.getMemoryUsage();
        }

        @Override
        public void reload() {
            darkPlayer.reload();
        }

        @Override
        public void setSize(int width, int height) {
            darkPlayer.setSize(width, height);
        }

        @Override
        public void setVisible(boolean visible) {
            darkPlayer.setVisible(visible);
        }

        @Override
        public void setMinimized(boolean minimized) {
            darkPlayer.setMinimized(minimized);
        }
    }

    public static class DarkPlayerDirectInteraction extends GameAPI.NoOpDirectInteraction {
        private final Main main;
        private final DarkPlayer darkPlayer;
        private final HeroAPI heroAPI;
        private final BotInstaller botInstaller;
        private final HeroManager heroManager;

        private long guiManagerAddress;
        private long screenManagerAddress;
        private boolean hookInitialized;

        public DarkPlayerDirectInteraction(Main main, DarkPlayer darkPlayer, HeroAPI heroAPI, BotInstaller botInstaller, HeroManager heroManager) {
            this.main = main;
            this.darkPlayer = darkPlayer;
            this.heroAPI = heroAPI;
            this.botInstaller = botInstaller;
            this.heroManager = heroManager;

            botInstaller.invalid.add(state -> {
                hookInitialized = false;
            });

            botInstaller.guiManagerAddress.add(value -> guiManagerAddress = value);
            botInstaller.screenManagerAddress.add(value -> screenManagerAddress = value);
        }

        @Override
        public void tick() {
            super.tick();
            if (!hookInitialized) {
                if (heroManager.address > 0xFFFF && !darkPlayer.isHookValid(guiManagerAddress, 3)) {
                    darkPlayer.setHook(guiManagerAddress, 3);
                    hookInitialized = true;
                }
            }
        }

        @Override
        public void setMaxFps(int maxFps) {
            darkPlayer.setMaxFps(maxFps);
        }

        @Override
        public void lockEntity(Lockable lockable) {
            darkPlayer.lockEntity(screenManagerAddress, heroAPI.getX(), heroAPI.getY(), lockable.getX(), lockable.getY(), lockable.getId());
        }

        @Override
        public void moveShip(Locatable dest) {
            darkPlayer.moveShip(darkPlayer.readLong(main.mapManager.eventAddress), (long) dest.getX(), (long) dest.getY());
        }

        @Override
        public void collectBox(Locatable dest, long addr) {
            darkPlayer.collectBox(darkPlayer.readLong(main.mapManager.eventAddress), (long) dest.getX(), (long) dest.getY(), addr);
        }
    }
}