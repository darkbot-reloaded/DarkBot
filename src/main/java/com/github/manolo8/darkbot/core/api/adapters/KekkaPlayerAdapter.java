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
import eu.darkbot.api.KekkaPlayer;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.OreAPI;
import eu.darkbot.api.utils.ItemUseCaller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class KekkaPlayerAdapter extends GameAPIImpl<
        KekkaPlayer,
        KekkaPlayer,
        KekkaPlayer,
        ByteUtils.ExtraMemoryReader,
        KekkaPlayer,
        KekkaPlayerAdapter.KekkaPlayerDirectInteraction> {

    private final ItemUseCaller itemUseCaller;
    private final Consumer<Map<String, Config.BotSettings.APIConfig.PatternInfo>> listener = this::setBlockingPatterns;

    public KekkaPlayerAdapter(StartupParams params, KekkaPlayerDirectInteraction di, KekkaPlayer kekkaPlayer,
                              BotInstaller botInstaller, ItemUseCaller itemUseCaller, ConfigAPI config) {
        super(params,
                kekkaPlayer,
                kekkaPlayer,
                kekkaPlayer,
                new ByteUtils.ExtraMemoryReader(kekkaPlayer, botInstaller),
                kekkaPlayer,
                di,
                Capability.LOGIN,
                Capability.INITIALLY_SHOWN,
                Capability.CREATE_WINDOW_THREAD,
                Capability.DIRECT_LIMIT_FPS,
                Capability.HANDLER_GAME_QUALITY,
                Capability.PROXY,
                Capability.WINDOW_POSITION,
                Capability.HANDLER_CLEAR_CACHE,
                Capability.HANDLER_CLEAR_RAM,
                Capability.HANDLER_GAME_QUALITY,
                Capability.HANDLER_VOLUME,
                Capability.HANDLER_TRANSPARENCY,
                Capability.HANDLER_CLIENT_SIZE,
                Capability.HANDLER_MIN_CLIENT_SIZE,
                Capability.HANDLER_CPU_USAGE,
                Capability.HANDLER_RAM_USAGE,
                Capability.ALL_KEYBINDS_SUPPORT,
                Capability.HANDLER_FLASH_PATH,
                Capability.HANDLER_INTERNET_READ_TIME,
                Capability.DIRECT_ENTITY_SELECT,
                Capability.DIRECT_MOVE_SHIP,
                Capability.DIRECT_COLLECT_BOX,
                Capability.DIRECT_POST_ACTIONS,
                Capability.DIRECT_CALL_METHOD,
                Capability.DIRECT_REFINE,
                Capability.DIRECT_USE_ITEM);
        this.itemUseCaller = itemUseCaller;

        // 10 seconds after each reload
        botInstaller.invalid.add(v -> clearRamTimer.activate(10_000));

        ConfigSetting<Map<String, Config.BotSettings.APIConfig.PatternInfo>> c = config.requireConfig("bot_settings.api_config.block_patterns");
        listener.accept(c.getValue());
        c.addListener(listener);
    }

    @Override
    public void tick() {
        super.tick();
        itemUseCaller.tick();
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
        window.postActions(actions);
    }

    @Override
    public void pasteText(String text, long... actions) {
        window.pasteText(text, actions);
    }

    @Override
    public long getMemoryUsage() {
        return window.getMemoryUsage() / 1024 / 1024;
    }

    @Override
    public String getVersion() {
        return "KekkaPlayer-" + window.getVersion();
    }

    public void setBlockingPatterns(Map<String, Config.BotSettings.APIConfig.PatternInfo> map) {
        List<String> result = new ArrayList<>(map.size());
        map.forEach((key, value) -> {
            if (value.enable && value.regex != null && !value.regex.isEmpty()) {
                result.add(value.regex);
                result.add(value.filePath == null ? "" : value.filePath);
            }
        });

        window.setBlockingPatterns(result.toArray(new String[0]));
    }

    @Override
    public void reload(boolean useFakeDailyLogin) {
        if (!useFakeDailyLogin && window.getVersion() >= 26)
            window.normalReload();
        else handler.reload();
    }

    public static class KekkaPlayerDirectInteraction extends NoopAPIAdapter.NoOpDirectInteraction
            implements Utils.SignatureChecker {
        private final KekkaPlayer kekkaPlayer;
        private final BotInstaller botInstaller;

        private final Set<String> methodSignatureCache = new HashSet<>();

        public KekkaPlayerDirectInteraction(KekkaPlayer KekkaPlayer, BotInstaller botInstaller) {
            this.kekkaPlayer = KekkaPlayer;
            this.botInstaller = botInstaller;

            botInstaller.invalid.add(v -> methodSignatureCache.clear());
        }

        @Override
        public Set<String> signatureCache() {
            return methodSignatureCache;
        }

        @Override
        public void setMaxFps(int maxFps) {
            kekkaPlayer.setMaxFps(maxFps);
        }

        @Override
        public boolean callMethodChecked(boolean checkName, String signature, int index, long... arguments) {
            if (checkSignature(checkName, signature, index, arguments[0]))
                return callMethodAsync(index, arguments);

            return false;
        }

        @Override
        public boolean callMethodAsync(int index, long... arguments) {
            return kekkaPlayer.callMethod(botInstaller.screenManagerAddress.get(), index, arguments);
        }

        @Override
        public void selectEntity(Entity entity) {
            if (entity.clickable.isInvalid()) return;
            if (botInstaller.screenManagerAddress.get() == 0) return;

            long[] args = Utils.createSelectEntityArgs(entity);
            kekkaPlayer.sendNotification(botInstaller.screenManagerAddress.get(), Utils.SELECT_MAP_ASSET, args);
        }

        @Override
        public void moveShip(Locatable destination) {
            if (checkGotoMethod(kekkaPlayer, botInstaller))
                kekkaPlayer.moveShip(botInstaller.screenManagerAddress.get(), (long) destination.getX(), (long) destination.getY(), 0);
        }

        @Override
        public void collectBox(Box box) {
            if (checkGotoMethod(kekkaPlayer, botInstaller))
                kekkaPlayer.moveShip(botInstaller.screenManagerAddress.get(), (long) box.getX(), (long) box.getY(), box.address);
        }

        @Override
        public void refine(long refineUtilAddress, OreAPI.Ore oreType, int amount) {
            kekkaPlayer.refine(refineUtilAddress, oreType.getId(), amount);
        }

        @Override
        public long callMethod(int index, long... arguments) {
            return kekkaPlayer.callMethodSync(index, arguments);
        }

        @Override
        public int checkMethodSignature(long obj, int methodIdx, boolean includeMethodName, String signature) {
            return kekkaPlayer.checkMethodSignature(obj, methodIdx, includeMethodName, signature);
        }

    }
}