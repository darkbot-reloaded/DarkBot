package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import eu.darkbot.api.game.items.ItemCategory;
import eu.darkbot.api.game.items.SelectableItem;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.manolo8.darkbot.Main.API;

public class CategoryBar extends MenuBar {
    public final FlashList<Category> categories = FlashList.ofVector(Category::new);

    private final Map<SelectableItem, Item> itemsMap = new HashMap<>(
            (int) SelectableItem.ALL_ITEMS.values().stream()
                    .mapToLong(List::size).sum());

    @Override
    public void update() {
        super.update();
        long categoriesAddress = readLong(56);
        if (categories.getAddress() != categoriesAddress) {
            itemsMap.clear();
        }
        this.categories.update(categoriesAddress);
    }

    // assume the parameter is enum from SelectableItems itf
    public Item getItem(SelectableItem selectableItem) {
        Item item = itemsMap.get(selectableItem);
        if (item != null) return item;

        return findItem(selectableItem).orElse(null);
    }

    @Deprecated
    public Category get(CategoryType type) {
        String id = type.getId();
        for (Category category : categories) {
            if (id.equals(category.categoryId)) return category;
        }
        return null;
    }

    public Category get(ItemCategory itemCategory) {
        for (Category category : categories) {
            if (itemCategory == category.itemCategory)
                return category;
        }
        return null;
    }

    public List<Item> getItems(ItemCategory itemCategory)  {
        Category cat = get(itemCategory);
        return cat == null ? Collections.emptyList() : cat.items;
    }

    public Stream<Item> getItemStream(ItemCategory itemCategory) {
        return getItems(itemCategory).stream();
    }

    public boolean hasCategory(ItemCategory type) {
        for (Category category : categories) {
            if (type == category.itemCategory)
                return true;
        }
        return false;
    }

    public Optional<Item> findItemById(String itemId) {
        for (Category cat : categories) {
            for (Item item : cat.items) {
                if (itemId.equals(item.id)) return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public Optional<Item> findItem(SelectableItem item) {
        if (item.getCategory() == null)
            return categories.stream()
                    .map(c -> c.findItem(item))
                    .filter(Objects::nonNull)
                    .findFirst();

        Category category = get(item.getCategory());
        if (category == null)
            return Optional.empty();

        return Optional.ofNullable(category.findItem(item));
    }

    public class Category extends Auto {
        private ItemCategory itemCategory;

        public String categoryId;
        public FlashList<Item> items = FlashList.ofVector(() -> new Item(itemCategory, itemsMap));

        public @Nullable Item findItem(SelectableItem item) {
            return items.stream()
                    .filter(i -> i.equals(item))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void update() {
            this.items.update(API.readLong(address + 40));
        }

        @Override
        public void update(long address) {
            if (this.address != address || categoryId == null || categoryId.isEmpty()) {
                this.categoryId = API.readString(address, 32);
                this.itemCategory = ItemCategory.of(categoryId);
            }
            super.update(address);
        }
    }

    @Deprecated
    public enum CategoryType {
        LASERS,
        ROCKETS,
        ROCKET_LAUNCHERS,
        SPECIAL_ITEMS,
        MINES,
        CPUS,
        BUY_NOW,
        TECH_ITEMS,
        SHIP_ABILITIES,
        DRONE_FORMATIONS,
        PET;

        public String getId() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}