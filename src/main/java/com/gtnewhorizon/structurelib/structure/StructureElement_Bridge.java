package com.gtnewhorizon.structurelib.structure;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;



/**
 * Internal bridge class for easing implementation
 */
abstract class StructureElement_Bridge<T> implements IStructureElement<T> {

    // seal the deprecated method to prevent accidental overrides
    @Deprecated
    @Override
    public final PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger, IItemSource s,
                                                ServerPlayer actor, Consumer<Component> chatter) {
        return survivalPlaceBlock(t, world, x, y, z, trigger, AutoPlaceEnvironment.fromLegacy(s, actor, chatter));
    }

    // clear the default implementation. we don't need that.
    public abstract PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment env);
}
