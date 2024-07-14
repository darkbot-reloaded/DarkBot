package eu.darkbot.api.utils;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.KekkaPlayer;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.util.Timer;

import static com.github.manolo8.darkbot.Main.API;

public class ItemUseCaller {
    private final KekkaPlayer kekkaPlayer;
    private final HeroItemsAPI heroItems;
    private final HeroAPI hero;

    private final Timer nextKeyClick = Timer.get(5_000), nextCommandCheck = Timer.get(500);

    private long connectionManager, screenManager, useItemCommand;

    public ItemUseCaller(KekkaPlayer kekkaPlayer, BotInstaller botInstaller, HeroItemsAPI heroItems, HeroAPI hero) {
        this.kekkaPlayer = kekkaPlayer;
        this.heroItems = heroItems;
        this.hero = hero;

        botInstaller.connectionManagerAddress.add(this::updateConnectionManager);
        botInstaller.screenManagerAddress.add(this::updateScreenManager);
    }

    private void updateConnectionManager(long address) {
        connectionManager = address;
        useItemCommand = 0;
    }

    private void updateScreenManager(long address) {
        screenManager = address;
    }

    public boolean useItem(Item item) {
        if (isItemUsable()) {
            kekkaPlayer.useItem(screenManager, item.getId(), 19, connectionManager, useItemCommand);
            return true;
        }
        return false;
    }

    public boolean isItemUsable() {
        return ByteUtils.isValidPtr(connectionManager) && ByteUtils.isValidPtr(screenManager) && ensureItemCommandAvailable();
    }

    private boolean ensureItemCommandAvailable() {
        if (ByteUtils.isValidPtr(useItemCommand)) {
            return true;
        }

        long useItemCommandClosure = findUseItemCommandClosure();
        if (useItemCommandClosure != 0) {
            useItemCommand = API.callMethod(5, useItemCommandClosure);
        } else if (nextKeyClick.tryActivate()) {
            API.keyClick(heroItems.getKeyBind(hero.getFormation()));
        }

        nextCommandCheck.activate();
        return ByteUtils.isValidPtr(useItemCommand);
    }

    private long findUseItemCommandClosure() {
        return API.searchClassClosure(v ->
                API.readInt(v + 48) == 0 &&
                        API.readInt(v + 52) == 1 &&
                        API.readInt(v + 56) == 2 &&
                        API.readInt(v + 60) == 0 &&
                        API.readInt(v + 64) == 1 &&
                        API.readInt(v + 68) == 2 &&
                        (API.readLong(v + 72) == 0 || API.readInt(v + 72) != 3));
    }
}
