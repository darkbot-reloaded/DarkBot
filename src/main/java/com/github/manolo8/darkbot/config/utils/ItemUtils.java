package com.github.manolo8.darkbot.config.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import eu.darkbot.api.game.items.ItemCategory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ItemUtils {

    public static Optional<Item> findAssociatedItem(@Nullable ItemCategory category, Character c) {
        Main main = Main.INSTANCE;

        SettingsProxy.KeyBind k = main.facadeManager.settings.getAtChar(c);
        if (k == null || k.getType() == null) return Optional.empty();

        int slotNumber;
        try {
            slotNumber = Integer.parseInt(k.name().replaceAll("[^-?0-9]+", ""));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        CategoryBar cb = main.facadeManager.slotBars.categoryBar;

        return (category != null ? cb.getItemStream(category) :
                cb.categories.stream().flatMap(cat -> cat.items.stream()))
                .filter(Item::hasShortcut)
                .filter(item -> item.getSlotBarType() == k.getType())
                .filter(item -> item.containsSlotNumber(slotNumber))
                .findAny();
    }

}
