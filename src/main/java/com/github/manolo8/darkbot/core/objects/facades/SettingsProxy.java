package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.PairArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.API;
import org.jetbrains.annotations.Nullable;

import static com.github.manolo8.darkbot.Main.API;

public class SettingsProxy extends Updatable implements eu.darkbot.api.API.Singleton {

    private final Character[] keycodes = new Character[KeyBind.values().length];
    private final PairArray keycodesDictionary = PairArray.ofDictionary().setAutoUpdatable(true);

    /**
     * Get {@link Character} associated with given {@link KeyBind}.
     * Returns null if keybind is not assigned or doesnt exists.
     */
    @Nullable
    public Character getCharCode(KeyBind keyBind) {
        return keycodes[keyBind.ordinal()];
    }

    @Override
    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        PairArray keycodesDictionary = this.keycodesDictionary;
        keycodesDictionary.update(API.readMemoryLong(data + 240));

        Character[] keycodes = this.keycodes;
        int length = keycodes.length;
        for (int i = 0; i < length && i < keycodesDictionary.getSize(); i++) {
            // int vector size
            int arrSize = API.readMemoryInt(keycodesDictionary.getPtr(i) + 64);
            if (arrSize <= 0) keycodes[i] = null;

            //read first encounter in int vector
            int keycode = API.readMemoryInt(keycodesDictionary.getPtr(i), 48, 4);
            keycodes[i] = keycode <= 0 || keycode > 222 ? null : (char) keycode;
        }
    }

    public enum KeyBind {
        SLOTBAR_1,
        SLOTBAR_2,
        SLOTBAR_3,
        SLOTBAR_4,
        SLOTBAR_5,
        SLOTBAR_6,
        SLOTBAR_7,
        SLOTBAR_8,
        SLOTBAR_9,
        SLOTBAR_0,
        PREMIUM_1,
        PREMIUM_2,
        PREMIUM_3,
        PREMIUM_4,
        PREMIUM_5,
        PREMIUM_6,
        PREMIUM_7,
        PREMIUM_8,
        PREMIUM_9,
        JUMP_GATE,
        TOGGLE_CONFIG,
        ATTACK_LASER,
        ATTACK_ROCKET,
        ACTIVE_PET,
        PET_GUARD_MODE,
        PET_COMBO_REPAIR,
        LOGOUT,
        TOGGLE_WINDOWS,
        TOGGLE_MONITORING,
        ZOOM_IN,
        ZOOM_OUT,
        FOCUS_CHAT,
        TOGGLE_CATEGORYBAR,
        PREMIUM_0,
        TOGGLE_PRO_ACTION
    }
}
