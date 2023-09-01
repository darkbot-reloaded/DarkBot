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
        botInstaller.connectionManagerAddress.add(l -> {
            connectionManager = l;
            useItemCommand = 0;
        });

        botInstaller.screenManagerAddress.add(l -> screenManager = l);
    }

    public boolean useItem(Item item) {
        if (checkUsable()) {
            kekkaPlayer.useItem(screenManager, item.getId(), 19, connectionManager, useItemCommand);
            return true;
        }

        return false;
    }

    public boolean checkUsable() {
        return ByteUtils.isValidPtr(connectionManager) && ByteUtils.isValidPtr(screenManager) && checkItemCommand();
    }

    private boolean checkItemCommand() {
        if (ByteUtils.isValidPtr(useItemCommand)) return true;

        long useItemCommandClosure = API.searchClassClosure(v ->
                API.readInt(v + 48) == 0
                        && API.readInt(v + 52) == 1
                        && API.readInt(v + 56) == 2
                        && API.readInt(v + 60) == 0
                        && API.readInt(v + 64) == 1
                        && API.readInt(v + 68) == 2
                        && (API.readLong(v + 72) == 0 || API.readInt(v + 72) != 3));

        if (useItemCommandClosure != 0) {
            useItemCommand = API.callMethod(5, useItemCommandClosure); // getInstance();

        } else if (nextKeyClick.tryActivate()) {
            // item use command not found, try to click current formation to create instance
            API.keyClick(heroItems.getKeyBind(hero.getFormation()));
        }

        nextCommandCheck.activate();
        return ByteUtils.isValidPtr(useItemCommand);
    }

    public void tick() {
        if (!ByteUtils.isValidPtr(useItemCommand) && nextCommandCheck.isInactive())
            checkItemCommand();
    }
}
