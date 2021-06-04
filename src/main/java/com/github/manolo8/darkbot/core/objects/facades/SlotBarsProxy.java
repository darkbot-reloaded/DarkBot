package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.FacadeManager;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;
import eu.darkbot.api.entities.other.SelectableItem;
import eu.darkbot.api.future.ItemFutureResult;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.impl.future.ItemSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBarsProxy extends Updatable implements HeroItemsAPI {

    public final CategoryBar categoryBar = new CategoryBar();
    public final SlotBar standardBar = new SlotBar(categoryBar, Type.DEFAULT_BAR);
    public final SlotBar premiumBar = new SlotBar(categoryBar, Type.PREMIUM_BAR);
    public final SlotBar proActionBar = new SlotBar(categoryBar, Type.PRO_ACTION_BAR);

    private final FacadeManager facade;
    private final Queue<ItemSelector> itemFutureResultQueue = new ArrayDeque<>();

    public SlotBarsProxy(FacadeManager facade) {
        this.facade = facade;
    }

    @Override
    public void update() {
        this.categoryBar.update(API.readMemoryLong(address + 88));

        this.proActionBar.update(API.readMemoryLong(address + 112));
        this.premiumBar.update(API.readMemoryLong(address + 104));
        this.standardBar.update(API.readMemoryLong(address + 96));

        ItemSelector is;
        while ((is = itemFutureResultQueue.poll()) != null) {
            is.run();
        }
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

    @SuppressWarnings("unchecked")
    @Override
    public Optional<eu.darkbot.api.objects.Item> checkSelectable(@NotNull SelectableItem item) {
        return (Optional<eu.darkbot.api.objects.Item>) findItem(item)
                .filter(this::isSelectable);
    }

    @Override
    public ItemFutureResult selectItem(@NotNull SelectableItem item) {
        ItemSelector itemSelector = new ItemSelector((Item) findItem(item).get(), facade);

        itemFutureResultQueue.add(itemSelector);
        return itemSelector;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<eu.darkbot.api.objects.Item> getItemOf(SelectableItem item) {
        return (Optional<eu.darkbot.api.objects.Item>) findItem(item);
    }

    @Override
    public Map<Category, List<? extends eu.darkbot.api.objects.Item>> getItems() {
        return categoryBar.items;
    }

    private boolean isSelectable(eu.darkbot.api.objects.Item item) {
        Item i = (Item) item;
        return i.hasShortcut();
    }

    private Optional<? extends eu.darkbot.api.objects.Item> findItem(SelectableItem item) {
        if (item == null) return Optional.empty();
        if (item instanceof Item) return Optional.of((Item) item);

        Category category = item.getCategory();

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