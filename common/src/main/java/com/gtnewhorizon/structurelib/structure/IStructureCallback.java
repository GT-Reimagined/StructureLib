package com.gtnewhorizon.structurelib.structure;

import net.minecraft.world.level.Level;

@FunctionalInterface
public interface IStructureCallback<T, B extends IStructureElement<T>> {
    void accept(B e, T c, Level w, int x, int y, int z);
}
