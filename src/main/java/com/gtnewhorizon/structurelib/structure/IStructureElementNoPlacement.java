package com.gtnewhorizon.structurelib.structure;

import java.util.function.Consumer;

import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.Level;

public interface IStructureElementNoPlacement<T> extends IStructureElement<T> {

    @Override
    default boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
        return false;
    }

    @Override
    default PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger, IItemSource s,
            ServerPlayer actor, Consumer<IChatComponent> chatter) {
        return PlaceResult.REJECT;
    }

    @Override
    default PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment env) {
        return PlaceResult.REJECT;
    }

    @Override
    default IStructureElementNoPlacement<T> noPlacement() {
        return this;
    }
}
