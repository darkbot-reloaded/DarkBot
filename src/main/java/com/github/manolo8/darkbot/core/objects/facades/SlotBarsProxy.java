package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.api.objects.slotbars.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBarsProxy extends Updatable implements HeroItemsAPI {
    public CategoryBar categoryBar;
    public SlotBar standardBar;
    public SlotBar premiumBar; //104
    public SlotBar proActionBar; //112

    public SlotBarsProxy(SettingsProxy settingsProxy) {
        this.categoryBar = new CategoryBar(settingsProxy);
        this.standardBar = new SlotBar(categoryBar, Type.DEFAULT_BAR);
        this.premiumBar = new SlotBar(categoryBar, Type.PREMIUM_BAR);
        this.proActionBar = new SlotBar(categoryBar, Type.PRO_ACTION_BAR);
    }

    @Override
    public void update() {
        this.categoryBar.update(API.readMemoryLong(address + 88));

        this.proActionBar.update(API.readMemoryLong(address + 112));
        this.premiumBar.update(API.readMemoryLong(address + 104));
        this.standardBar.update(API.readMemoryLong(address + 96));
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

    public enum Type {
        DEFAULT_BAR,
        PREMIUM_BAR,
        PRO_ACTION_BAR;
    }
}