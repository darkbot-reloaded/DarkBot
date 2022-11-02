package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.api.KekkaPlayer;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.OreAPI;
import eu.darkbot.api.utils.ItemUseCaller;

public class KekkaPlayerAdapter extends GameAPIImpl<
        KekkaPlayer,
        KekkaPlayer,
        KekkaPlayer,
        ByteUtils.ExtraMemoryReader,
        KekkaPlayer,
        KekkaPlayerAdapter.KekkaPlayerDirectInteraction> {

    private final ItemUseCaller itemUseCaller;

    public KekkaPlayerAdapter(StartupParams params, KekkaPlayerDirectInteraction di, KekkaPlayer kekkaPlayer,
                              BotInstaller botInstaller, ItemUseCaller itemUseCaller) {
        super(params,
                kekkaPlayer,
                kekkaPlayer,
                kekkaPlayer,
                new ByteUtils.ExtraMemoryReader(kekkaPlayer, botInstaller),
                kekkaPlayer,
                di,
                GameAPI.Capability.LOGIN,
                GameAPI.Capability.INITIALLY_SHOWN,
                GameAPI.Capability.CREATE_WINDOW_THREAD,
                GameAPI.Capability.DIRECT_LIMIT_FPS,
                GameAPI.Capability.HANDLER_GAME_QUALITY,
                GameAPI.Capability.PROXY,
                GameAPI.Capability.WINDOW_POSITION,
                GameAPI.Capability.HANDLER_CLEAR_CACHE,
                GameAPI.Capability.HANDLER_CLEAR_RAM,
                GameAPI.Capability.HANDLER_GAME_QUALITY,
                GameAPI.Capability.HANDLER_VOLUME,
                GameAPI.Capability.HANDLER_TRANSPARENCY,
                GameAPI.Capability.HANDLER_CLIENT_SIZE,
                GameAPI.Capability.HANDLER_MIN_CLIENT_SIZE,
                GameAPI.Capability.ALL_KEYBINDS_SUPPORT,
                GameAPI.Capability.HANDLER_FLASH_PATH,
                GameAPI.Capability.DIRECT_ENTITY_SELECT,
                GameAPI.Capability.DIRECT_MOVE_SHIP,
                GameAPI.Capability.DIRECT_COLLECT_BOX,
                GameAPI.Capability.DIRECT_POST_ACTIONS,
                GameAPI.Capability.DIRECT_CALL_METHOD,
                GameAPI.Capability.DIRECT_REFINE,
                GameAPI.Capability.DIRECT_USE_ITEM);
        this.itemUseCaller = itemUseCaller;
    }

    @Override
    public void tick() {
        super.tick();
        itemUseCaller.tick();
    }

    @Override
    public boolean useItem(Item item) {
        return itemUseCaller.useItem(item);
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

    public static class KekkaPlayerDirectInteraction extends GameAPI.NoOpDirectInteraction {
        private final KekkaPlayer kekkaPlayer;
        private final BotInstaller botInstaller;

        public KekkaPlayerDirectInteraction(KekkaPlayer KekkaPlayer, BotInstaller botInstaller) {
            this.kekkaPlayer = KekkaPlayer;
            this.botInstaller = botInstaller;
        }

        @Override
        public void setMaxFps(int maxFps) {
            kekkaPlayer.setMaxFps(maxFps);
        }

        @Override
        public void refine(long refineUtilAddress, OreAPI.Ore oreType, int amount) {
            kekkaPlayer.refine(refineUtilAddress, oreType.getId(), amount);
        }

        @Override
        public boolean callMethodAsync(int index, long... arguments) {
            return kekkaPlayer.callMethod(botInstaller.screenManagerAddress.get(), index, arguments);
        }

        @Override
        public void selectEntity(long clickableAddress, long scriptObjectVtable) {
            kekkaPlayer.selectEntity(clickableAddress, scriptObjectVtable, false);
        }

        @Override
        public void moveShip(Locatable destination) {
            kekkaPlayer.moveShip(botInstaller.screenManagerAddress.get(), (long) destination.getX(), (long) destination.getY(), 0);
        }

        @Override
        public void collectBox(Box box) {
            kekkaPlayer.moveShip(botInstaller.screenManagerAddress.get(), (long) box.getX(), (long) box.getY(), box.address);
        }

        @Override
        public long callMethod(int index, long... arguments) {
            return kekkaPlayer.callMethodSync(index, arguments);
        }

    }

}