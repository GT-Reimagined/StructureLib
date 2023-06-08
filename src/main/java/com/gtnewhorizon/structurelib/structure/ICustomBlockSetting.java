package com.gtnewhorizon.structurelib.structure;


public interface ICustomBlockSetting {

    /**
     * Default block setting calls {@link Level#setBlock(int x, int y, int z, Block block, int updateType)}
     * like: {@code world.setBlock(x,y,z,this/block,meta,2)} where updateType 2 means to update lighting and stuff
     *
     * @param world world that should be affected
     * @param x     x position to set
     * @param y     y position to set
     * @param z     z position to set
     */
    void setBlock(Level world, int x, int y, int z);
}
