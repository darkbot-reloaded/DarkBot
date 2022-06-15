package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;
import eu.darkbot.api.game.items.ItemCategory;
import eu.darkbot.api.game.items.ItemFlag;
import eu.darkbot.api.game.items.ItemUseResult;
import eu.darkbot.api.game.items.SelectableItem;
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

    public boolean useItem(Character key) {
        if (key == null) return false;
        API.keyboardClick(key);
        return true;
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
        if (keybind == null || keybind.getType() == null || keybind.getSlotIdx() == -1) return null;

        SlotBar sb = keybind.getType() == Type.DEFAULT_BAR ? standardBar :
                keybind.getType() == Type.PREMIUM_BAR ? premiumBar : null;

        return sb == null || sb.slots.size() < 10 ? null : sb.slots.get(keybind.getSlotIdx());
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.items.Item> getItems(@NotNull ItemCategory itemCategory) {
        return categoryBar.get(itemCategory).items;
    }

    @Override
    public Optional<eu.darkbot.api.game.items.Item> getItem(@NotNull SelectableItem selectableItem, ItemFlag... itemFlags) {
        Optional<eu.darkbot.api.game.items.Item> item = Optional.ofNullable(getItem(selectableItem));

        return item.filter(i -> checkItemFlags((Item) i, itemFlags) == null);
    }

    private static final ItemFlag[] DEFAULT_ITEM_FLAGS = {ItemFlag.AVAILABLE, ItemFlag.READY, ItemFlag.USABLE};

    public @NotNull ItemUseResult useItemInternal(@NotNull SelectableItem selectableItem, ItemFlag... itemFlags) {
        Item item = getItem(selectableItem);

        if (item == null) return ItemUseResult.NOT_AVAILABLE;

        ItemUseResult itemUseResult = checkItemFlags(item, itemFlags);
        if (itemUseResult != null || (itemUseResult = checkItemFlags(item, DEFAULT_ITEM_FLAGS)) != null)
            return itemUseResult;

        if (API.useItem(item)) {
            return ItemUseResult.SUCCESS
                    .ifSuccessful(r -> item.setLastUsed(System.currentTimeMillis()));
        }

        SlotBarsProxy.Type slotBarType = item.getSlotBarType();
        int slotNumber = item.getFirstSlotNumber();
        if (slotNumber < 1 || slotNumber > 10) return ItemUseResult.FAILED;

        boolean toggleProAction = (slotBarType == SlotBarsProxy.Type.PRO_ACTION_BAR) != isProActionBarVisible();

        return ((!toggleProAction || settings.pressKeybind(SettingsProxy.KeyBind.TOGGLE_PRO_ACTION))
                && settings.pressKeybind(SettingsProxy.KeyBind.of(slotBarType, slotNumber))
                ? ItemUseResult.SUCCESS : ItemUseResult.FAILED)
                .ifSuccessful(r -> item.setLastUsed(System.currentTimeMillis()));
    }

    @Override
    public ItemUseResult useItem(@NotNull SelectableItem selectableItem, ItemFlag... itemFlags) {
        return useItem(selectableItem, 500, itemFlags);
    }

    @Override
    public ItemUseResult useItem(@NotNull SelectableItem selectableItem, double minWait, ItemFlag... itemFlags) {
        Item item = getItem(selectableItem);
        if (item == null) return ItemUseResult.NOT_AVAILABLE;
        if (item.lastUseTime() + minWait > System.currentTimeMillis()) return ItemUseResult.RECENTLY_USED;

        return useItemInternal(item, itemFlags);
    }

    @Override
    public @Nullable Item getItem(Character character) {
        SlotBar.Slot slot = getSlot(settings.getAtChar(character));
        if (slot == null || slot.item == null) return null;

        return categoryBar.findItem(slot.item).orElse(slot.item);
    }

    @Override
    public @Nullable Item getItem(Character keyBind, @NotNull ItemCategory itemCategory) {
        SlotBar.Slot slot = getSlot(settings.getAtChar(keyBind));
        if (slot == null || slot.item == null) return null;

        CategoryBar.Category category = categoryBar.get(itemCategory);
        if (category == null) return null;

        Item result = category.findItem(slot.item);
        return result == null ? slot.item : result;
    }

    @Override
    public @Nullable Character getKeyBind(SelectableItem selectableItem) {
        Item item = getItem(selectableItem);
        if (item == null) return null;
        SlotBarsProxy.Type slotBarType = item.getSlotBarType();
        int slotNumber = item.getFirstSlotNumber();
        if (slotNumber < 1 || slotNumber > 10 || slotBarType == Type.PRO_ACTION_BAR) return null;

        return settings.getCharCode(SettingsProxy.KeyBind.of(slotBarType, slotNumber));
    }

    private ItemUseResult checkItemFlags(Item item, ItemFlag... flags) {
        for (ItemFlag flag : flags) {
            if (flag == ItemFlag.USABLE
                    && API.isUseItemSupported()) continue;

            if (!flag.test(item))
                return flag.getFailResult();
        }

        return null;
    }

    private Item getItem(SelectableItem item) {
        if (item == null) return null;
        if (item instanceof Item) return (Item) item;

        return categoryBar.findItem(item).orElse(null);
    }

    public enum Type {
        DEFAULT_BAR,
        PREMIUM_BAR,
        PRO_ACTION_BAR
    }
}