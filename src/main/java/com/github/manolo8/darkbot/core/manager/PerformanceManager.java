package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.API;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.ConfigAPI;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PerformanceManager implements Manager, Listener, API.Singleton {

    private final MapManager mapManager;
    private final BotAPI bot;

    private final ConfigSetting<Boolean> alwaysEnabled;
    private final ConfigSetting<Boolean> disableRender;
    private final ConfigSetting<Integer> maxFps;
    private final ConfigSetting<Integer> minTick;

    private final List<Consumer<?>> listeners = new ArrayList<>();

    @Getter private int minTickTime;
    private long settings3DAddress;
    private boolean renderDisabled;

    public PerformanceManager(ConfigAPI config, MapManager mapManager, BotAPI bot) {
        this.mapManager = mapManager;
        this.bot = bot;

        this.alwaysEnabled = config.requireConfig("bot_settings.performance.always_enabled");
        this.disableRender = config.requireConfig("bot_settings.performance.disable_render");
        this.maxFps = config.requireConfig("bot_settings.performance.max_fps");
        this.minTick = config.requireConfig("bot_settings.performance.min_tick");

        this.alwaysEnabled.addListener(storeListener(this::toggleAll));
        this.maxFps.addListener(storeListener(this::setMaxFps));
        this.minTick.addListener(storeListener(this::setMinTickTime));

        setMaxFps(maxFps.getValue());
        setMinTickTime(minTick.getValue());
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.invalid.add(invalid -> {
            settings3DAddress = 0;
            renderDisabled = false;
        });
    }

    public void tick() {
        updateRender(shouldEnable() && disableRender.getValue());
    }

    private boolean shouldEnable() {
        return alwaysEnabled.getValue() || bot.isRunning();
    }

    private <T> Consumer<T> storeListener(Consumer<T> listener) {
        this.listeners.add(listener);
        return listener;
    }

    private void toggleAll(boolean ignored) {
        setMaxFps(maxFps.getValue());
        setMinTickTime(minTick.getValue());
    }

    private void setMaxFps(int maxFps) {
        if (Main.API.hasCapability(Capability.DIRECT_LIMIT_FPS))
            Main.API.setMaxFps(shouldEnable() ? maxFps : 0);
    }

    private void setMinTickTime(int minTickTime) {
        this.minTickTime = shouldEnable() ? minTickTime : 15;
    }

    private void updateRender(boolean disable) {
        if (!mapManager.is3D() || renderDisabled == disable) return;

        if (!ByteUtils.isValidPtr(settings3DAddress)) {
            settings3DAddress = Main.API.searchClassClosure(l -> ByteUtils.readObjectName(l).equals("Settings3D$"));
            return;
        }

        if (ByteUtils.isScriptableObjectValid(settings3DAddress)) {
            int render = disable ? 0 : 1;
            int oldValue = render == 1 ? 0 : 1;
            Main.API.replaceInt(Main.API.readLong(settings3DAddress, 0xf8) + 0x20, oldValue, render);
        }

        renderDisabled = disable;
    }

    @EventHandler
    public void onRunningToggle(BotAPI.RunningToggleEvent event) {
        toggleAll(event.isRunning());
    }
}
