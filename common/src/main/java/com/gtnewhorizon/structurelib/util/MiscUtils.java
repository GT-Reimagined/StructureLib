package com.gtnewhorizon.structurelib.util;

import java.util.Set;

import net.minecraft.nbt.CompoundTag;

/**
 * not related to a certain mod!
 */
public class MiscUtils {

    private MiscUtils() {}

    @SuppressWarnings("unchecked")
    public static Set<String> getTagKeys(CompoundTag tag) {
        return tag.getAllKeys();
    }
}
