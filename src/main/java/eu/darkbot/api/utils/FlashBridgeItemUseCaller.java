package eu.darkbot.api.utils;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.FlashBridge;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.util.Timer;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Item use caller for FlashBridge — uses AVM2 method calls through the enterFrame hook.
 * Finds the MenuActionRequest class closure in memory, constructs an instance,
 * sets the item ID string, and calls connectionManager.sendRequest().
 */
public class FlashBridgeItemUseCaller {
    private final HeroItemsAPI heroItems;
    private final HeroAPI hero;

    private final Timer nextKeyClick = Timer.get(5_000);

    private long connectionManager, screenManager, useItemCommand;

    public FlashBridgeItemUseCaller(BotInstaller botInstaller, HeroItemsAPI heroItems, HeroAPI hero) {
        this.heroItems = heroItems;
        this.hero = hero;
        botInstaller.connectionManagerAddress.add(l -> {
            connectionManager = l;
            useItemCommand = 0;
        });
        botInstaller.screenManagerAddress.add(l -> screenManager = l);
    }

    public boolean useItem(Item item) {
        if (!checkUsable()) return false;
        if (item.getId() == null || item.getId().isEmpty()) return false;

        return FlashBridge.useItem(connectionManager, item.getId(), 19, useItemCommand);
    }

    public boolean checkUsable() {
        return ByteUtils.isValidPtr(connectionManager)
            && ByteUtils.isValidPtr(screenManager)
            && checkItemCommand();
    }

    private boolean checkItemCommand() {
        if (ByteUtils.isValidPtr(useItemCommand)) return true;

        // Search for MenuActionRequest class closure by matching its static constants
        long closure = API.searchClassClosure(v ->
                API.readInt(v + 48) == 0
                        && API.readInt(v + 52) == 1
                        && API.readInt(v + 56) == 2
                        && API.readInt(v + 60) == 0
                        && API.readInt(v + 64) == 1
                        && API.readInt(v + 68) == 2
                        && (API.readLong(v + 72) == 0 || API.readInt(v + 72) != 3));

        if (closure != 0) {
            // Store the closure — C++ will call construct() each time useItem is called
            useItemCommand = closure;
        } else if (nextKeyClick.tryActivate()) {
            // Not found yet — click current formation to trigger class loading
            API.keyClick(heroItems.getKeyBind(hero.getFormation()));
        }

        return ByteUtils.isValidPtr(useItemCommand);
    }
}
