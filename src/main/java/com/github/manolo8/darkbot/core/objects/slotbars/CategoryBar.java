package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class CategoryBar extends MenuBar {
    public List<Category> categories = new ArrayList<>();

    private ObjArray categoriesArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        super.update();
        this.categoriesArr.update(API.readMemoryLong(address + 56));
        this.categoriesArr.sync(this.categories, Category::new, null);
    }

    public static class Category extends UpdatableAuto {
        public String categoryId;
        public List<Item> items = new ArrayList<>();

        private ObjArray itemsArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            this.categoryId = API.readMemoryString(address, 32);
            this.itemsArr.update(API.readMemoryLong(address + 40));
            this.itemsArr.sync(this.items, Item::new, null);
        }
    }
}