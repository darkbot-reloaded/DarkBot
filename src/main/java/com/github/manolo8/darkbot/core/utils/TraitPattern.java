package com.github.manolo8.darkbot.core.utils;

import static com.github.manolo8.darkbot.Main.API;

public class TraitPattern {

    /**
     * Filters if given address is instance of IntHolder which holds LockType.
     */
    public static boolean ofLockType(long address) {
        long temp = API.readMemoryLong(address + 48);
        int lockType = API.readMemoryInt(temp + 40);

        return (lockType == 1 || lockType == 2 || lockType == 3 || lockType == 4) &&
                API.readMemoryInt(temp + 32) == Integer.MIN_VALUE &&
                API.readMemoryInt(temp + 36) == Integer.MAX_VALUE;
    }

    /**
     * Filters if given address is instance of Health in-game
     */
    public static boolean ofHealth(long address) {
        long classType = API.readMemoryLong(address, 48, 0x10);

        return  API.readMemoryLong(address, 48 + 8    , 0x10) == classType &&
                API.readMemoryLong(address, 48 + 8 * 2, 0x10) == classType &&
                API.readMemoryLong(address, 48 + 8 * 3, 0x10) == classType &&
                API.readMemoryLong(address, 48 + 8 * 4, 0x10) == classType &&
                API.readMemoryLong(address, 48 + 8 * 5, 0x10) == classType;
    }

    /**
     * Filters if given address is instance of Clickable in-game.
     */
    public static boolean ofClickable(long address) {
        int radius   = API.readMemoryInt(address + 40);
        int priority = API.readMemoryInt(address + 44);
        int enabled  = API.readMemoryInt(address + 48);

        return radius >= 0 && radius < 4000 &&
                priority > -4 && priority < 1000 &&
                (enabled == 1 || enabled == 0);
    }
}
