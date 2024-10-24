package com.gtnewhorizon.structurelib.structure.adders;


import net.minecraft.world.level.block.entity.BlockEntity;

public interface ITileAdder<T> {

    /**
     * Callback to add hatch, needs to check if tile is valid (and add it)
     *
     * @param tileEntity tile
     * @return managed to add hatch (structure still valid)
     */
    boolean apply(T t, BlockEntity tileEntity);
}
