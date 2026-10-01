package com.github.manolo8.darkbot.core.api.adapters;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPIImpl;
import com.github.manolo8.darkbot.core.api.Utils;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.FlashBridge;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.utils.FlashBridgeItemUseCaller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class FlashBridgeAdapter extends GameAPIImpl<
        FlashBridge,
        FlashBridge,
        FlashBridge,
        ByteUtils.ExtraMemoryReader,
        FlashBridge,
        FlashBridgeAdapter.FlashBridgeDirectInteraction> {

    private final FlashBridgeItemUseCaller itemUseCaller;
    private final Consumer<Map<String, Config.BotSettings.APIConfig.PatternInfo>> blockPatternListener =
            this::setBlockingPatterns;

    public FlashBridgeAdapter(StartupParams params,
                              FlashBridgeDirectInteraction di,
                              FlashBridge flashBridge,
                              BotInstaller botInstaller,
                              FlashBridgeItemUseCaller itemUseCaller,
                              ConfigAPI config) {
        super(params,
                flashBridge,
                flashBridge,
                flashBridge,
                new ByteUtils.ExtraMemoryReader(flashBridge, botInstaller),
                flashBridge,
                di,
                Capability.LOGIN,
                Capability.INITIALLY_SHOWN,
                Capability.CREATE_WINDOW_THREAD,
                Capability.WINDOW_POSITION,
                Capability.HANDLER_CLIENT_SIZE,
                Capability.PROXY,
                Capability.ALL_KEYBINDS_SUPPORT,
                Capability.DIRECT_ENTITY_SELECT,
                Capability.DIRECT_MOVE_SHIP,
                Capability.DIRECT_COLLECT_BOX,
                Capability.DIRECT_CALL_METHOD,
                Capability.DIRECT_USE_ITEM,
                Capability.DIRECT_LIMIT_FPS,
                Capability.DIRECT_POST_ACTIONS,
                Capability.HANDLER_CLEAR_CACHE,
                Capability.HANDLER_CLEAR_RAM,
                Capability.HANDLER_CPU_USAGE,
                Capability.HANDLER_RAM_USAGE,
                Capability.HANDLER_FLASH_PATH,
                Capability.HANDLER_INTERNET_READ_TIME);

        this.itemUseCaller = itemUseCaller;

        botInstaller.invalid.add(v -> clearRamTimer.activate(10_000));

        // Same wiring as KekkaPlayerAdapter, so the existing BLOCK_PATTERNS config drives both backends identically.
        ConfigSetting<Map<String, Config.BotSettings.APIConfig.PatternInfo>> c = config.requireConfig("bot_settings.api_config.block_patterns");
        blockPatternListener.accept(c.getValue());
        c.addListener(blockPatternListener);
    }

    @Override
    public void reload(boolean useFakeDailyLogin) {
        if (useFakeDailyLogin) window.reload();
        else window.normalReload();
    }

    /**
     * Flatten the config into the [regex, filePath, regex, filePath, ...] array
     * the native side expects. An empty filePath means block; a non-empty one
     * means serve that file's bytes in place of the response body.
     */
    private void setBlockingPatterns(Map<String, Config.BotSettings.APIConfig.PatternInfo> map) {
        List<String> result = new ArrayList<>(map.size() * 2);
        map.forEach((key, value) -> {
            if (value.enable && value.regex != null && !value.regex.isEmpty()) {
                result.add(value.regex);
                result.add(value.filePath == null ? "" : value.filePath);
            }
        });
        window.setBlockingPatterns(result.toArray(new String[0]));
    }

    @Override
    public String getVersion() {
        return "FlashBridge v" + window.getVersion();
    }

    @Override
    public boolean useItem(Item item) {
        if (direct.checkSignature(true, "23(sendRequest)(2626)1016221500",
                19, direct.botInstaller.connectionManagerAddress.get()))
            return itemUseCaller.useItem(item);
        return false;
    }

    @Override
    public boolean isUseItemSupported() {
        return itemUseCaller.checkUsable();
    }

    @Override
    public void postActions(long... actions) {
        FlashBridge.postActions(actions);
    }

    @Override
    public void pasteText(String text, long... actions) {
        FlashBridge.pasteText(text, actions);
    }

    public static class FlashBridgeDirectInteraction extends NoopAPIAdapter.NoOpDirectInteraction
            implements Utils.SignatureChecker {

        private final FlashBridge flashBridge;
        private final BotInstaller botInstaller;
        private final Set<String> methodSignatureCache = new HashSet<>();

        public FlashBridgeDirectInteraction(FlashBridge flashBridge, BotInstaller botInstaller) {
            this.flashBridge = flashBridge;
            this.botInstaller = botInstaller;

            botInstaller.invalid.add(v -> methodSignatureCache.clear());
        }

        @Override
        public Set<String> signatureCache() {
            return methodSignatureCache;
        }

        @Override
        public boolean callMethodChecked(boolean checkName, String signature, int index, long... arguments) {
            if (checkSignature(checkName, signature, index, arguments[0]))
                return callMethodAsync(index, arguments);
            return false;
        }

        @Override
        public boolean callMethodAsync(int index, long... arguments) {
            long sm = botInstaller.screenManagerAddress.get();
            if (sm != 0) FlashBridge.setScreenManager(sm);
            return FlashBridge.callMethodAsync(index, arguments);
        }

        @Override
        public void selectEntity(Entity entity) {
            if (entity.clickable.isInvalid()) return;
            if (botInstaller.screenManagerAddress.get() == 0) return;

            long[] args = Utils.createSelectEntityArgs(entity);
            FlashBridge.sendNotification(botInstaller.screenManagerAddress.get(), Utils.SELECT_MAP_ASSET, args);
        }

        @Override
        public void moveShip(Locatable destination) {
            long sm = botInstaller.screenManagerAddress.get();
            if (checkGotoMethod(flashBridge, botInstaller)) {
                FlashBridge.moveShip(sm, (long) destination.getX(), (long) destination.getY(), 0);
            }
        }

        @Override
        public void collectBox(Box box) {
            long sm = botInstaller.screenManagerAddress.get();
            if (checkGotoMethod(flashBridge, botInstaller)) {
                FlashBridge.collectBox(sm, (long) box.getX(), (long) box.getY(), box.address);
            }
        }

        @Override
        public long callMethod(int index, long... arguments) {
            long sm = botInstaller.screenManagerAddress.get();
            if (sm != 0) FlashBridge.setScreenManager(sm);
            return FlashBridge.callMethodSync(index, arguments);
        }

        @Override
        public int checkMethodSignature(long obj, int methodIdx, boolean includeMethodName, String signature) {
            long sm = botInstaller.screenManagerAddress.get();
            if (sm != 0) FlashBridge.setScreenManager(sm);
            return FlashBridge.checkMethodSignature(obj, methodIdx, includeMethodName, signature);
        }

        @Override
        public void setMaxFps(int maxFps) {
            FlashBridge.setMaxFps(maxFps);
        }

    }
}
