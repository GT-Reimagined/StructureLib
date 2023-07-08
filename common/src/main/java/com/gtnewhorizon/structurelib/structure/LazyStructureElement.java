package com.gtnewhorizon.structurelib.structure;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;


class LazyStructureElement<T> implements IStructureElementDeferred<T> {

    private Function<T, IStructureElement<T>> to;
    private IStructureElement<T> elem;

    public LazyStructureElement(Function<T, IStructureElement<T>> to) {
        this.to = to;
    }

    private IStructureElement<T> get(T t) {
        if (to != null) {
            elem = to.apply(t);
            to = null;
        }
        return elem;
    }

    @Override
    public boolean check(T t, Level world, int x, int y, int z) {
        return get(t).check(t, world, x, y, z);
    }

    @Override
    public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
        return get(t).placeBlock(t, world, x, y, z, trigger);
    }

    @Override
    public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
        return get(t).spawnHint(t, world, x, y, z, trigger);
    }

    @Nullable
    @Override
    public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment env) {
        return get(t).getBlocksToPlace(t, world, x, y, z, trigger, env);
    }

    @Override
    public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger, IItemSource s,
                                          ServerPlayer actor, Consumer<Component> chatter) {
        return get(t).survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
    }

    @Override
    public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment env) {
        return get(t).survivalPlaceBlock(t, world, x, y, z, trigger, env);
    }

    @Override
    public void onStructureSuccess(T t, Level world, int x, int y, int z) {
        get(t).onStructureSuccess(t, world, x, y, z);
    }

    @Override
    public void onStructureFail(T t, Level world, int x, int y, int z) {
        get(t).onStructureFail(t, world, x, y, z);
    }
}
