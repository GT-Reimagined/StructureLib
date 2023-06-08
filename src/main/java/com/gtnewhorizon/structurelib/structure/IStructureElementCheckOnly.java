package com.gtnewhorizon.structurelib.structure;

import java.util.function.Consumer;

import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.Level;

public interface IStructureElementCheckOnly<T> extends IStructureElement<T> {

    @Override
    default boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
        return false;
    }

    @Override
    default boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
        return false;
    }

    @Override
    default PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger, IItemSource s,
            ServerPlayer actor, Consumer<IChatComponent> chatter) {
        return PlaceResult.SKIP;
    }

    @Override
    default PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment env) {
        return PlaceResult.SKIP;
    }
}
