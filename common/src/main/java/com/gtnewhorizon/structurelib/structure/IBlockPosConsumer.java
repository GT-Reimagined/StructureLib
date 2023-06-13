package com.gtnewhorizon.structurelib.structure;


import net.minecraft.world.level.Level;

public interface IBlockPosConsumer {

    void consume(Level world, int x, int y, int z);
}
