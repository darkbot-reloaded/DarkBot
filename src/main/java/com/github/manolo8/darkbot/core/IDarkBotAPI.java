package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.OreAPI;
import eu.darkbot.api.managers.WindowAPI;
import org.intellij.lang.annotations.Language;

public interface IDarkBotAPI extends WindowAPI, MemoryAPI {

    //<editor-fold desc="WindowAPI">
    void tick();
    int getRefreshCount();

    void createWindow();

    boolean isValid();
    boolean isInitiallyShown();
    long getMemoryUsage();
    double getCpuUsage();

    // Left for legacy purposes who may still use it, prefer keyboardClick(Character)
    @Deprecated
    default void keyboardClick(char btn) {
        rawKeyboardClick(Character.toUpperCase(btn), true);
    }

    // Left for legacy purposes who may still use it, prefer keyboardClick(Character)
    @Deprecated
    default void rawKeyboardClick(char btn) {
        rawKeyboardClick(btn, true);
    }

    // Internal, prefer using keyboardClick instead
    void rawKeyboardClick(char btn, boolean deduplicate);

    default void keyboardClick(Character ch) {
        keyboardClick(ch, true);
    }

    default void keyboardClick(Character ch, boolean deduplicate) {
        if (ch != null) rawKeyboardClick(ch, deduplicate);
    }

    @Override
    default void keyClick(int keyCode) {
        rawKeyboardClick((char) keyCode, true);
    }

    default void setVisible(boolean visible, boolean fullyHideEnabled) {
        if (fullyHideEnabled) setMinimized(!visible);
        else setVisible(visible);
    }

    default void handleRefresh() {
        handleRefresh(false);
    }

    void handleRefresh(boolean useFakeDailyLogin);

    default void handleRelogin() {
        handleRelogin(false);
    }

    void handleRelogin(boolean forceRelogin);

    void resetCache();
    //</editor-fold>

    boolean hasCapability(Capability capability);

    default boolean hasCapability(Capability... capabilities) {
        for (Capability capability : capabilities) {
            if (!hasCapability(capability)) {
                return false;
            }
        }
        return true;
    }

    //<editor-fold desc="Direct game access">
    void setMaxFps(int maxCps);

    void lockEntity(int id);

    void selectEntity(Entity entity);

    void moveShip(Locatable destination);

    void collectBox(Box box);

    void refine(long refineUtilAddress, OreAPI.Ore ore, int amount);

    long callMethod(int index, long... arguments);
    boolean callMethodChecked(boolean checkName, String signature, int index, long... arguments);
    boolean callMethodAsync(int index, long... arguments);
    boolean useItem(Item item);

    // checks if useItem() is supported and ready to use
    boolean isUseItemSupported();

    // post actions (mouse clicks, key clicks or any other window message) see NativeAction
    void postActions(long... actions);
    void pasteText(String text, long... actions);
    //</editor-fold>

    //<editor-fold desc="Handler API">
    void clearCache(@Language("RegExp") String pattern);
    void emptyWorkingSet();
    void setLocalProxy(int port);
    void setPosition(int x, int y);
    void setFlashOcxPath(String path);
    void setUserInput(boolean enable);
    void setClientSize(int width, int height);
    void setMinClientSize(int width, int height);
    void setTransparency(int transparency);
    void setVolume(int volume); // 0 - 100

    // LOW = 0, MEDIUM = 1, HIGH = 2, BEST = 3, AUTO_LOW = 4, AUTO_HIGH = 5
    void setQuality(GameAPI.Handler.GameQuality quality);

    long lastInternetReadTime();
    //</editor-fold>
}
