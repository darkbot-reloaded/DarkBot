package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.managers.HeroItemsAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class CategoryBar extends MenuBar {
    public List<Category> categories = new ArrayList<>();

    private final ObjArray categoriesArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        super.update();
        this.categoriesArr.update(API.readMemoryLong(address + 56));
        this.categoriesArr.sync(this.categories, Category::new, null);
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

        private final ObjArray itemsArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            this.categoryId = API.readMemoryString(address, 32);
            this.itemsArr.update(API.readMemoryLong(address + 40));
            this.itemsArr.sync(this.items, Item::new, null);
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