package com.gtnewhorizon.structurelib.structure;

import com.google.common.collect.Iterables;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Use StructureUtility to instantiate
 */
public interface IStructureElementChain<T> extends IStructureElement<T> {

    IStructureElement<T>[] fallbacks();

    @Override
    default boolean check(T t, Level world, int x, int y, int z) {
        for (IStructureElement<T> fallback : fallbacks()) {
            if (fallback.check(t, world, x, y, z)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
        for (IStructureElement<T> fallback : fallbacks()) {
            if (fallback.spawnHint(t, world, x, y, z, trigger)) {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
        for (IStructureElement<T> fallback : fallbacks()) {
            if (fallback.placeBlock(t, world, x, y, z, trigger)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    default BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment env) {
        Predicate<ItemStack> predicate = null;
        List<Iterable<ItemStack>> is = new ArrayList<>();
        for (IStructureElement<T> fallback : fallbacks()) {
            BlocksToPlace e = fallback.getBlocksToPlace(t, world, x, y, z, trigger, env);
            if (e == null) continue;
            if (predicate == null) predicate = e.getPredicate();
            else predicate = predicate.or(e.getPredicate());
            is.add(e.getStacks());
        }
        if (predicate == null) return null;
        return new BlocksToPlace(predicate, Iterables.concat(is));
    }

    @Override
    default PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger, IItemSource s,
                                           ServerPlayer actor, Consumer<Component> chatter) {
        boolean haveSkip = false;
        List<Component> bufferedNoise = new ArrayList<>();
        for (IStructureElement<T> fallback : fallbacks()) {
            PlaceResult result = fallback.survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, bufferedNoise::add);
            switch (result) {
                case REJECT:
                    break;
                case SKIP:
                    haveSkip = true;
                    break;
                default:
                    return result;
            }
        }
        // dump all that noise back into upstream
        bufferedNoise.forEach(chatter);
        // TODO need reconsider to ensure this is the right course of action
        return haveSkip ? PlaceResult.SKIP : PlaceResult.REJECT;
    }

    @Override
    default PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
            AutoPlaceEnvironment env) {
        boolean haveSkip = false;
        List<Component> bufferedNoise = new ArrayList<>();
        for (IStructureElement<T> fallback : fallbacks()) {
            PlaceResult result = fallback
                    .survivalPlaceBlock(t, world, x, y, z, trigger, env.withChatter(bufferedNoise::add));
            switch (result) {
                case REJECT:
                    break;
                case SKIP:
                    haveSkip = true;
                    break;
                default:
                    return result;
            }
        }
        // dump all that noise back into upstream
        bufferedNoise.forEach(env.getChatter());
        // TODO need reconsider to ensure this is the right course of action
        return haveSkip ? PlaceResult.SKIP : PlaceResult.REJECT;
    }

    @Override
    default void onStructureSuccess(T t, Level world, int x, int y, int z) {
        for (IStructureElement<T> fallback : fallbacks()) {
            fallback.onStructureSuccess(t, world, x, y, z);
        }
    }

    @Override
    default void onStructureFail(T t, Level world, int x, int y, int z) {
        for (IStructureElement<T> fallback : fallbacks()) {
            fallback.onStructureFail(t, world, x, y, z);
        }
    }
}
