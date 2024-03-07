package com.github.manolo8.darkbot.utils;


import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class Offsets {
    public static int SPRITE_OFFSET = OSUtil.isLinux() ? 0x8 : 0;

    public static String getEntityAssetId(long address) {
        long trait = API.readLong(address, 0x30, 0x30, 0x10) & ByteUtils.ATOM_MASK;
        return getTraitAssetId(trait);
    }

    public static String getTraitAssetId(long trait) {
        long address = API.readLong(trait, 0x40, 0x20, 0x18);
        return API.readString(address, 0x8 + SPRITE_OFFSET, 0x10, 0x18).trim();
    }

}
