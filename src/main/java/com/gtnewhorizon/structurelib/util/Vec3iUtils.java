package com.gtnewhorizon.structurelib.util;

import net.minecraft.core.Vec3i;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;



public class Vec3iUtils {

    private static final Map<Vec3i, Vec3i> pool = new HashMap<>();

    /**
     * THIS WILL CAUSE THIS VECTOR TO PERMANENTLY RESIDE IN STATIC MEMORY. DO NOT USE THIS UNLESS YOU KNOW WHAT YOU ARE
     * DOING!!!
     */
    public static Vec3i getFromPool(int in0, int in1, int in2) {
        return pool.computeIfAbsent(new Vec3i(in0, in1, in2), Function.identity());
    }
}
