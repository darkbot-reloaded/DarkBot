package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBarsProxy extends Updatable {
    public CategoryBar categoryBar = new CategoryBar();
    public SlotBar standardBar     = new SlotBar();
    public SlotBar premiumBar    = new SlotBar();
    //public SlotBar proActionBar  = new SlotBar(); //112

    private final SettingsProxy settings;

    public SlotBarsProxy(SettingsProxy settings) {
        this.settings = settings;
    }

    @Nullable
    public SlotBar.Slot getSlot(SettingsProxy.KeyBind keybind) {
        if (keybind == null) return null;
        String keyStr = keybind.toString();

        SlotBar sb = keyStr.startsWith("SLOTBAR") ? standardBar :
                keyStr.startsWith("PREMIUM") ? premiumBar : null;

        return sb == null ? null : sb.slots.get((Integer.parseInt(keyStr.split("_")[1]) + 9) % 10);
    }

    public Optional<Item> findItemByCharacter(Character character) {
        if (character == null) return Optional.empty();
        return Optional.ofNullable(settings.getKeyBind(character))
                .map(this::getSlot)
                .filter(slot -> slot.item != null)
                .flatMap(slot -> categoryBar.findItemById(slot.item.id));
    }

    @Override
    public void update() {
        this.categoryBar.update(API.readMemoryLong(address + 88));
        this.standardBar.update(API.readMemoryLong(address + 96));
        this.premiumBar.update(API.readMemoryLong(address + 104));
    }
}