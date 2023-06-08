package com.gtnewhorizon.structurelib.structure;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICustomBlockSetting {

    /**
     * Default block setting calls {@link Level#setBlock(BlockPos pos, BlockState block, int updateType)}
     * like: {@code world.setBlock(x,y,z,this/block,meta,2)} where updateType 2 means to update lighting and stuff
     *
     * @param world world that should be affected
     * @param x     x position to set
     * @param y     y position to set
     * @param z     z position to set
     */
    void setBlock(Level world, int x, int y, int z);
}
