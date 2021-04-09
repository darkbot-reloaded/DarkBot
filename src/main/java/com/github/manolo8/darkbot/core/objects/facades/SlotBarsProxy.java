package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;
import eu.darkbot.api.managers.HeroItemsAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBarsProxy extends Updatable implements HeroItemsAPI {

    public final CategoryBar categoryBar = new CategoryBar();
    public final SlotBar standardBar = new SlotBar(categoryBar, Type.DEFAULT_BAR);
    public final SlotBar premiumBar = new SlotBar(categoryBar, Type.PREMIUM_BAR);
    public final SlotBar proActionBar = new SlotBar(categoryBar, Type.PRO_ACTION_BAR);

    private final SettingsProxy settings;

    public SlotBarsProxy(SettingsProxy settingsProxy) {
        this.settings = settingsProxy;
    }

    @Override
    public void update() {
        this.categoryBar.update(API.readMemoryLong(address + 88));

        this.proActionBar.update(API.readMemoryLong(address + 112));
        this.premiumBar.update(API.readMemoryLong(address + 104));
        this.standardBar.update(API.readMemoryLong(address + 96));
    }

    public boolean isCategoryBarVisible() {
        return API.readBoolean(address + 72);
    }

    public boolean isProActionBarVisible() {
        return API.readBoolean(address + 76);
    }

    @Nullable
    public SlotBar.Slot getSlot(SettingsProxy.KeyBind keybind) {
        if (keybind == null) return null;
        String keyStr = keybind.toString();

        SlotBar sb = keyStr.startsWith("SLOTBAR") ? standardBar :
                keyStr.startsWith("PREMIUM") ? premiumBar : null;

        return sb == null ? null : sb.slots.get((Integer.parseInt(keyStr.split("_")[1]) + 9) % 10);
    }

    @Override
    public boolean isSelectable(@NotNull eu.darkbot.api.objects.Item item) {
        return ((Item) item).hasShortcut();
    }

    @Override
    public boolean selectItem(@NotNull eu.darkbot.api.objects.Item item) {
        SlotBarsProxy.Type slotBarType = ((Item) item).getSlotBarType();
        int slotNumber = ((Item) item).getFirstSlotNumber();

        if (slotBarType == null || slotNumber == -1 ||
                (slotBarType == SlotBarsProxy.Type.PRO_ACTION_BAR &&
                        !isProActionBarVisible() &&
                        !settings.toggleKeyBind(SettingsProxy.KeyBind.TOGGLE_PRO_ACTION)))
            return false; //return false if slot type is pro action bar & isnt visible & keybind wasn't toggled.

        return settings.toggleKeyBind(SettingsProxy.KeyBind.of(slotBarType, slotNumber));
    }

    @Override
    public Collection<? extends eu.darkbot.api.objects.Item> getItems() {
        return categoryBar.categories.stream()
                .flatMap(category -> category.items.stream())
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasCategory(@NotNull Category category) {
        return categoryBar.hasCategory(category);
    }

    @Override
    public Collection<? extends eu.darkbot.api.objects.Item> getItems(@NotNull Category category) {
        return categoryBar.get(category).items;
    }

    public enum Type {
        DEFAULT_BAR,
        PREMIUM_BAR,
        PRO_ACTION_BAR;
    }
}