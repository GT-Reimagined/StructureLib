package com.gtnewhorizon.structurelib.structure;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;



/**
 * Use StructureUtility to instantiate
 */
interface IStructureNavigate<T> extends IStructureElement<T> {

    @Override
    default boolean check(T t, Level world, int x, int y, int z) {
        return true;
    }

    @Override
    default boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
        return true;
    }

    @Override
    default boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
        return true;
    }

    @Override
    default PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger, IItemSource s,
                                           ServerPlayer actor, Consumer<Component> chatter) {
        return PlaceResult.SKIP;
    }

    @Override
    default PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment env) {
        return PlaceResult.SKIP;
    }

    default boolean isNavigating() {
        return true;
    }
}
