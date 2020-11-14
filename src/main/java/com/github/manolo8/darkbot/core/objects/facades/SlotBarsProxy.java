package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;
import eu.darkbot.api.managers.SlotBarAPI;
import eu.darkbot.api.objects.Point;
import eu.darkbot.api.objects.slotbars.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBarsProxy extends Updatable implements SlotBarAPI {
    public CategoryBar categoryBar = new CategoryBar();
    public SlotBar standardBar = new SlotBar(categoryBar, SlotBarAPI.Type.DEFAULT_BAR);
    public SlotBar premiumBar = new SlotBar(categoryBar, SlotBarAPI.Type.PREMIUM_BAR); //104
    public SlotBar proActionBar = new SlotBar(categoryBar, SlotBarAPI.Type.PRO_ACTION_BAR); //112

    @Override
    public void update() {
        this.categoryBar.update(API.readMemoryLong(address + 88));

        this.proActionBar.update(API.readMemoryLong(address + 112));
        this.premiumBar.update(API.readMemoryLong(address + 104));
        this.standardBar.update(API.readMemoryLong(address + 96));
    }

    @Override
    public boolean hasSlotBar(Type slotBarType) {
        return getSlotBarByType(slotBarType).address != 0;
    }

    @Override
    public boolean isSlotBarVisible(Type slotBarType) {
        return getSlotBarByType(slotBarType).isVisible;
    }

    @Override
    public Point getSlotBarPosition(Type slotBarType) {
        return getSlotBarByType(slotBarType).barLocation;
    }

    @Override
    public boolean hasCategory(@NotNull Category category) {
        return categoryBar.hasCategory(category);
    }

    @Override
    public Collection<? extends Item> getItems(@NotNull Category category) {
        return categoryBar.get(category).items;
    }

    private SlotBar getSlotBarByType(Type slotBarType) {
        switch (slotBarType){
            case PREMIUM_BAR: return premiumBar;
            case PRO_ACTION_BAR: return proActionBar;
            default: return standardBar;
        }
    }
}