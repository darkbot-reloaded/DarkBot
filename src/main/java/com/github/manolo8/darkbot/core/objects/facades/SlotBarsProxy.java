package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;
import org.jetbrains.annotations.Nullable;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBarsProxy extends Updatable {
    public CategoryBar categoryBar = new CategoryBar();
    public SlotBar standardBar     = new SlotBar();
    public SlotBar premiumBar    = new SlotBar();
    //public SlotBar proActionBar  = new SlotBar(); //112

    @Nullable
    public SlotBar.Slot getSlot(SettingsProxy.KeyBind keybind) {
        if (keybind == null) return null;
        String keyStr = keybind.toString();

        SlotBar sb = keyStr.startsWith("SLOTBAR") ? standardBar :
                keyStr.startsWith("PREMIUM") ? premiumBar : null;

        return sb == null ? null : sb.slots.get((Integer.parseInt(keyStr.split("_")[1]) + 9) % 10);
    }

    @Override
    public void update() {
        this.categoryBar.update(API.readMemoryLong(address + 88));
        this.standardBar.update(API.readMemoryLong(address + 96));
        this.premiumBar.update(API.readMemoryLong(address + 104));
    }
}