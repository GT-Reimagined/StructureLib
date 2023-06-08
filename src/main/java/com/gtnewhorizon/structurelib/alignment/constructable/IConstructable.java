package com.gtnewhorizon.structurelib.alignment.constructable;


import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Created by Tec on 24.03.2017.
 */
public interface IConstructable {

    void construct(ItemStack stackSize, boolean hintsOnly);

    /**
     * Get the structure definition used for this constructable. Can be null if this constructable is not backed by one.
     */
    default IStructureDefinition<?> getStructureDefinition() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    String[] getStructureDescription(ItemStack stackSize);
}
