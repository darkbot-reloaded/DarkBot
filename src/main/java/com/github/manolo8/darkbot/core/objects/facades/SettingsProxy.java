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

    public KeyBind getAtChar(Character c) {
        if (c == null) return null;

        for (int i = 0; i < keycodes.length; i++)
            if (c == keycodes[i])
                return KeyBind.of(i);

        return null;
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
        SLOTBAR_1(SlotBarsProxy.Type.DEFAULT_BAR),
        SLOTBAR_2(SlotBarsProxy.Type.DEFAULT_BAR),
        SLOTBAR_3(SlotBarsProxy.Type.DEFAULT_BAR),
        SLOTBAR_4(SlotBarsProxy.Type.DEFAULT_BAR),
        SLOTBAR_5(SlotBarsProxy.Type.DEFAULT_BAR),
        SLOTBAR_6(SlotBarsProxy.Type.DEFAULT_BAR),
        SLOTBAR_7(SlotBarsProxy.Type.DEFAULT_BAR),
        SLOTBAR_8(SlotBarsProxy.Type.DEFAULT_BAR),
        SLOTBAR_9(SlotBarsProxy.Type.DEFAULT_BAR),
        SLOTBAR_0(SlotBarsProxy.Type.DEFAULT_BAR),
        PREMIUM_1(SlotBarsProxy.Type.PREMIUM_BAR),
        PREMIUM_2(SlotBarsProxy.Type.PREMIUM_BAR),
        PREMIUM_3(SlotBarsProxy.Type.PREMIUM_BAR),
        PREMIUM_4(SlotBarsProxy.Type.PREMIUM_BAR),
        PREMIUM_5(SlotBarsProxy.Type.PREMIUM_BAR),
        PREMIUM_6(SlotBarsProxy.Type.PREMIUM_BAR),
        PREMIUM_7(SlotBarsProxy.Type.PREMIUM_BAR),
        PREMIUM_8(SlotBarsProxy.Type.PREMIUM_BAR),
        PREMIUM_9(SlotBarsProxy.Type.PREMIUM_BAR),
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
        PREMIUM_0(SlotBarsProxy.Type.PREMIUM_BAR),
        TOGGLE_PRO_ACTION;

        private final SlotBarsProxy.Type type;

        KeyBind() {
            this(null);
        }

        KeyBind(SlotBarsProxy.Type type) {
            this.type = type;
        }

        public SlotBarsProxy.Type getType() {
            return type;
        }

        public static KeyBind of(int index) {
            if (index < 0 || index >= values().length) return null;
            return values()[index];
        }
    }
}
