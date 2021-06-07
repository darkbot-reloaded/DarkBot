package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;
import eu.darkbot.api.items.ItemCategory;
import eu.darkbot.api.items.ItemUseFlag;
import eu.darkbot.api.items.ItemUseResult;
import eu.darkbot.api.items.SelectableItem;
import eu.darkbot.api.managers.HeroItemsAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBarsProxy extends Updatable implements HeroItemsAPI {

    public final CategoryBar categoryBar = new CategoryBar();
    public final SlotBar standardBar = new SlotBar(categoryBar, Type.DEFAULT_BAR);
    public final SlotBar premiumBar = new SlotBar(categoryBar, Type.PREMIUM_BAR);
    public final SlotBar proActionBar = new SlotBar(categoryBar, Type.PRO_ACTION_BAR);

    private final SettingsProxy settings;

    public SlotBarsProxy(SettingsProxy settings) {
        this.settings = settings;
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
        return proActionBar.address != 0 && API.readBoolean(address + 76);
    }

    @Nullable
    public SlotBar.Slot getSlot(SettingsProxy.KeyBind keybind) {
        if (keybind == null) return null;
        String keyStr = keybind.toString();

        SlotBar sb = keyStr.startsWith("SLOTBAR") ? standardBar :
                keyStr.startsWith("PREMIUM") ? premiumBar : null;

        return sb == null ? null : sb.slots.get((Integer.parseInt(keyStr.split("_")[1]) + 9) % 10);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<eu.darkbot.api.items.Item> getItem(@NotNull SelectableItem selectableItem) {
        return (Optional<eu.darkbot.api.items.Item>) findItem(selectableItem);
    }

    @Override
    public Collection<? extends eu.darkbot.api.items.Item> getItems(@NotNull ItemCategory itemCategory) {
        return categoryBar.get(itemCategory).items;
    }

    @Override
    public Optional<eu.darkbot.api.items.Item> getAvailable(@NotNull SelectableItem item) {
        return Optional.empty();
    }

    @Override
    public @NotNull ItemUseResult useItem(@NotNull SelectableItem selectableItem, ItemUseFlag... itemFlags) {
        Item item = (Item) findItem(selectableItem).orElse(null);

        if (item == null || !item.hasShortcut() || !item.isAvailable())
            return ItemUseResult.NOT_AVAILABLE;

        for (ItemUseFlag useCase : itemFlags)
            if (!useCase.test(item))
                return useCase.getFailResult();

        SlotBarsProxy.Type slotBarType = item.getSlotBarType();
        int slotNumber = item.getFirstSlotNumber();

        if (slotBarType == null || slotNumber == -1)
            return (ItemUseResult.FAILED);

        boolean toggleProAction = (slotBarType == SlotBarsProxy.Type.PRO_ACTION_BAR) != isProActionBarVisible();

        return (!toggleProAction || settings.pressKeybind(SettingsProxy.KeyBind.TOGGLE_PRO_ACTION))
                && settings.pressKeybind(SettingsProxy.KeyBind.of(slotBarType, slotNumber))
                ? ItemUseResult.SUCCESS : ItemUseResult.FAILED;

    }

    private Optional<? extends eu.darkbot.api.items.Item> findItem(SelectableItem item) {
        if (item == null) return Optional.empty();
        if (item instanceof Item) return Optional.of((Item) item);

        ItemCategory category = item.getCategory();

        return category == null ? categoryBar.findItemById(item.getId())
                : categoryBar.get(category).items.stream()
                .filter(i -> i.getId().equals(item.getId()))
                .findFirst();
    }

    public enum Type {
        DEFAULT_BAR,
        PREMIUM_BAR,
        PRO_ACTION_BAR;
    }
}