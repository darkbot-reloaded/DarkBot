package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.managers.HeroItemsAPI;

import java.util.*;

import static com.github.manolo8.darkbot.Main.API;

public class CategoryBar extends MenuBar {
    public final List<Category> categories = new ArrayList<>();
    public final Map<HeroItemsAPI.Category, List<? extends eu.darkbot.api.objects.Item>> items = new EnumMap<>(HeroItemsAPI.Category.class);

    private final ObjArray categoriesArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        super.update();
        this.categoriesArr.update(API.readMemoryLong(address + 56));
        this.categoriesArr.sync(this.categories, Category::new, null);

        categories.forEach(category -> items.put(category.category, category.items));
    }

    public Category get(CategoryType type) {
        String id = type.getId();
        for (Category category : categories) {
            if (id.equals(category.categoryId)) return category;
        }
        return null;
    }

    public Category get(HeroItemsAPI.Category type) {
        String id = type.getId();
        for (Category category : categories) {
            if (id.equals(category.categoryId)) return category;
        }
        return null;
    }

    public boolean hasCategory(HeroItemsAPI.Category type) {
        String id = type.getId();
        for (Category category : categories) {
            if (id.equals(category.categoryId)) return true;
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

    public static class Category extends UpdatableAuto {
        public String categoryId;
        public List<Item> items = new ArrayList<>();

        private HeroItemsAPI.Category category;
        private final ObjArray itemsArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            this.categoryId = API.readMemoryString(address, 32);
            this.itemsArr.update(API.readMemoryLong(address + 40));
            this.itemsArr.sync(this.items, Item::new, null);

            for (HeroItemsAPI.Category cat : HeroItemsAPI.Category.values()) {
                if (cat.getId().equals(categoryId)) {
                    this.category = cat;
                    break;
                }
            }
        }
    }

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
        DRONE_FORMATIONS;

        public String getId() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}