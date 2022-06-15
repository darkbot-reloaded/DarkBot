package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.PairArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class SettingsProxy extends Updatable implements eu.darkbot.api.API.Singleton {

    private final Character[] keycodes = new Character[KeyBind.values().length];
    private final PairArray keycodesDictionary = PairArray.ofDictionary().setAutoUpdatable(true);
    private final Main main;

    public SettingsProxy(Main main) {
        this.main = main;
    }

    /**
     * Get {@link Character} associated with given {@link KeyBind}.
     * Returns null if keybind is not assigned or doesn't exist.
     */
    @Nullable
    public Character getCharCode(KeyBind keyBind) {
        return keycodes[keyBind.ordinal()];
    }

    public boolean pressKeybind(KeyBind keyBind) {
        Character charCode = getCharCode(Objects.requireNonNull(keyBind, "KeyBind is null!"));
        if (charCode == null) {
            main.guiManager.settingsGui.revalidateKeyBinds();
            return false;
        }

        API.keyboardClick(charCode);
        return true;
    }

    public Optional<Character> getCharacterOf(KeyBind keyBind) {
        return Optional.ofNullable(getCharCode(keyBind));
    }

    public KeyBind getAtChar(Character c) {
        if (c == null) return null;

        for (int i = 0; i < keycodes.length; i++)
            if (c == keycodes[i])
                return KeyBind.of(i);

        return null;
    }

    @Nullable
    @Deprecated
    public KeyBind getKeyBind(Character ch) {
        return getAtChar(ch);
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
        SLOTBAR_1(SlotBarsProxy.Type.DEFAULT_BAR, 0, KeyEvent.VK_1),
        SLOTBAR_2(SlotBarsProxy.Type.DEFAULT_BAR, 1, KeyEvent.VK_2),
        SLOTBAR_3(SlotBarsProxy.Type.DEFAULT_BAR, 2, KeyEvent.VK_3),
        SLOTBAR_4(SlotBarsProxy.Type.DEFAULT_BAR, 3, KeyEvent.VK_4),
        SLOTBAR_5(SlotBarsProxy.Type.DEFAULT_BAR, 4, KeyEvent.VK_5),
        SLOTBAR_6(SlotBarsProxy.Type.DEFAULT_BAR, 5, KeyEvent.VK_6),
        SLOTBAR_7(SlotBarsProxy.Type.DEFAULT_BAR, 6, KeyEvent.VK_7),
        SLOTBAR_8(SlotBarsProxy.Type.DEFAULT_BAR, 7, KeyEvent.VK_8),
        SLOTBAR_9(SlotBarsProxy.Type.DEFAULT_BAR, 8, KeyEvent.VK_9),
        SLOTBAR_0(SlotBarsProxy.Type.DEFAULT_BAR, 9, KeyEvent.VK_0),
        PREMIUM_1(SlotBarsProxy.Type.PREMIUM_BAR, 0, KeyEvent.VK_F1),
        PREMIUM_2(SlotBarsProxy.Type.PREMIUM_BAR, 1, KeyEvent.VK_F2),
        PREMIUM_3(SlotBarsProxy.Type.PREMIUM_BAR, 2, KeyEvent.VK_F3),
        PREMIUM_4(SlotBarsProxy.Type.PREMIUM_BAR, 3, KeyEvent.VK_F4),
        PREMIUM_5(SlotBarsProxy.Type.PREMIUM_BAR, 4, KeyEvent.VK_F5),
        PREMIUM_6(SlotBarsProxy.Type.PREMIUM_BAR, 5, KeyEvent.VK_F6),
        PREMIUM_7(SlotBarsProxy.Type.PREMIUM_BAR, 6, KeyEvent.VK_F7),
        PREMIUM_8(SlotBarsProxy.Type.PREMIUM_BAR, 7, KeyEvent.VK_F8),
        PREMIUM_9(SlotBarsProxy.Type.PREMIUM_BAR, 8, KeyEvent.VK_F9),
        JUMP_GATE(20,  KeyEvent.VK_J),
        TOGGLE_CONFIG(21,  KeyEvent.VK_C),
        ATTACK_LASER(22,  KeyEvent.VK_CONTROL),
        ATTACK_ROCKET(23,  KeyEvent.VK_SPACE),
        ACTIVE_PET(24,  KeyEvent.VK_E),
        PET_GUARD_MODE(25,  KeyEvent.VK_R),
        PET_COMBO_REPAIR(26,  KeyEvent.VK_D),
        LOGOUT(27,  KeyEvent.VK_L),
        TOGGLE_WINDOWS(28,  KeyEvent.VK_H),
        TOGGLE_MONITORING(29,  KeyEvent.VK_F),
        ZOOM_IN(30,  KeyEvent.VK_ADD),
        ZOOM_OUT(31,  KeyEvent.VK_SUBTRACT),
        FOCUS_CHAT(32,  0xD),
        TOGGLE_CATEGORYBAR(38,  KeyEvent.VK_TAB),
        PREMIUM_0(SlotBarsProxy.Type.PREMIUM_BAR, 9, 19, KeyEvent.VK_F10),
        TOGGLE_PRO_ACTION(39,  KeyEvent.VK_SHIFT);

        private final SlotBarsProxy.Type type;
        private final int slotIdx, settingIdx, defaultKey;

        KeyBind(int settingIdx, int defaultKey) {
            this(null, -1, settingIdx, defaultKey);
        }

        KeyBind(SlotBarsProxy.Type type, int slotIdx, int defaultKey) {
            this(type, slotIdx, -1, defaultKey);
        }

        KeyBind(SlotBarsProxy.Type type, int slotIdx, int settingIdx, int defaultKey) {
            this.type = type;
            this.slotIdx = slotIdx;
            this.settingIdx = settingIdx == -1 ? ordinal() : settingIdx;
            this.defaultKey = defaultKey;
        }

        public static KeyBind of(int index) {
            if (index < 0 || index >= values().length) return null;
            return values()[index];
        }

        public static KeyBind of(SlotBarsProxy.Type slotType, int slotNumber) {
            return KeyBind.valueOf((slotType == SlotBarsProxy.Type.PREMIUM_BAR ? "PREMIUM_" : "SLOTBAR_") + slotNumber % 10);
        }

        public SlotBarsProxy.Type getType() {
            return type;
        }

        public int getSlotIdx() {
            return slotIdx;
        }

        public int getSettingIdx() {
            return settingIdx;
        }

        public int getDefaultKey() {
            return defaultKey;
        }
    }
}
