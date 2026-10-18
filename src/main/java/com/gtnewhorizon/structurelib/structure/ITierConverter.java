package com.gtnewhorizon.structurelib.structure;


import net.minecraft.world.level.block.Block;

/**
 * a function to extract tier info from a block.
 *
 * @param <TIER>
 */
@FunctionalInterface
public interface ITierConverter<TIER> {

    TIER convert(Block block);
}
