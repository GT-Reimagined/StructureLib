package com.gtnewhorizon.structurelib.structure;

import java.util.*;
import java.util.function.*;

import javax.annotation.Nullable;


import com.gtnewhorizon.structurelib.Registry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;

import com.gtnewhorizon.structurelib.StructureEvent.StructureElementVisitedEvent;
import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureElement.PlaceResult;
import com.gtnewhorizon.structurelib.structure.adders.IBlockAdder;
import com.gtnewhorizon.structurelib.structure.adders.ITileAdder;
import com.gtnewhorizon.structurelib.util.ItemStackPredicate.NBTMode;

/**
 * A brief index of everything contained
 * <h2>Control blocks</h2> A lot of these might seem to make more sense as a default function on
 * {@link IStructureElement}. However, in that case javac generic will often fail to infer the type for you, so we have
 * to define them as static methods
 * <h2>If block</h2> Provide if block, allowing downstream call only if user specified conditions are meet (i.e.
 * predicate returns true)
 * <ul>
 * <li>{@link #onlyIf(Predicate, IStructureElement)} and its overloads</li>
 * </ul>
 * <h2>Switch block</h2> Provide switch block, allowing to select a downstream element by using a key or index computed
 * from trigger item and/or context object.
 * <ul>
 * <li>{@link #partitionBy(Function, IStructureElement[])} and its overloads</li>
 * </ul>
 * <h2>Or Chain</h2> Provide a short-circuiting OR chain.
 * <ul>
 * <li>{@link #ofChain(IStructureElement[])} and its overloads</li>
 * </ul>
 * <h3>Side effect</h3> Provide callbacks on various occasion
 * <ul>
 * <li>{@link #onElementPass(Consumer, IStructureElement)}: Call an callback upon element check success</li>
 * <li>{@link #onElementFail(Consumer, IStructureElement)}: Call an callback upon element check failure</li>
 * </ul>
 *
 * <h2>Other Primitives</h2>
 * <h3>Error statement</h3> Unconditionally return false.
 * <ul>
 * <li>{@link #error()}</li>
 * </ul>
 * <h3>Context Object Change</h3> Switch the context object.
 * <ul>
 * <li>{@link #withContext(IStructureElement)}: switch context to the extended context</li>
 * </ul>
 * <h3>Channel Change</h3> Change the channel. See <a href="{@docRoot}/overview-summary.html#channels">Channels section
 * on overview</a> for more information.
 * <ul>
 * <li>{@link #withChannel(String, IStructureElement)}: switch channel using
 * {@link ChannelDataAccessor#withChannel(ItemStack, String)} when a trigger item is present.</li>
 * </ul>
 * <h3>Spawn hint particle</h3> Just spawn a hint. Do nothing otherwise. Useful when you want to override the hint
 * provided by another structure element, or as a fallback to a structure element that is check only.
 * <ul>
 * <li>{@link #ofHint(int)}, {@link #ofHintDeferred(Supplier)} and its overloads</li>
 * </ul>
 *
 * <h2>Actual Element</h2> These elements are elements that can actually do structure check, spawn hint and do auto
 * place. Elements listed in above 2 sections are not going to be helpful without elements like these. They also serve
 * as a reference implementation for your own custom {@link IStructureElement} implementations.
 * <h3>Simple Block Element</h3> These accept one or more different block types in one location.
 * <ul>
 * <li>{@link #ofBlock(Block, Block)} and their overloads: the most basic form</li>
 * <li>{@link #ofBlockHint(Block, Block)}: these are the same as above, but will not do autoplace</li>
 * <li>{@link #isAir()}, {@link #notAir()}: They are supplied by default under the identifier {@code '-'} and
 * {@code '+'} respectively, but are provided here regardless in case you want to use them as a fallback.</li>
 * </ul>
 * <h3>Complex Block Element</h3> In case your logic on determining which block is accepted is complex, use these.
 * <ul>
 * <li>{@link #ofBlockAdder(IBlockAdder, Block)}, {@link #ofBlockAdderHint(IBlockAdder, Block)} and their
 * overloads: hands off actual adder logic to an {@link IBlockAdder} you supplied. Save you the boilerplate of querying
 * block type and block meta from {@link Level}</li>
 * <li>{@link #ofBlocksTiered(ITierConverter, Object, BiConsumer, Function)} and its overloads: accept a series of
 * blocks that each may have different tier, but only allow one single tier for anywhere this element is used (e.g.
 * coils).</li>
 * <li>{@link #ofTileAdder(ITileAdder, Block)}, {@link #ofSpecificTileAdder(BiPredicate, Class, Block)}:
 * Similar to block adder, but for tile entities.</li>
 * </ul>
 *
 * <h2>Helper Methods</h2> These don't construct a {@link IStructureElement}, but is helpful to the instantiation or
 * implementation of these.
 * <ul>
 * <li>{@link #survivalPlaceBlock(Block, Level, int, int, int, IItemSource, Player, Consumer)} and its
 * overloads: Helper method to cut common boilerplate for implementing
 * {@link IStructureElement#survivalPlaceBlock(Object, Level, int, int, int, ItemStack, IItemSource, ServerPlayer, Consumer)}</li>
 * <li>{@link #getPseudoJavaCode(Level, ExtendedFacing, int, int, int, int, int, int, Function, int, int, int, boolean)}:
 * Generate a structure code template from existing structure.</li>
 * <li>{@link #iterate(Level, ExtendedFacing, int, int, int, int, int, int, boolean, int, int, int, IBlockPosConsumer, Runnable, Runnable)}
 * and its overloads: help iterate over a coordinate space.</li>
 * </ul>
 */
public class StructureUtility {

    private static final String NICE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz=|!@#$%&()[]{};:<>/?_,.*^'`";

    @SuppressWarnings("rawtypes")
    private static final Map<Vec3i, IStructureNavigate> STEP = new HashMap<>();

    @SuppressWarnings("rawtypes")
    private static final IStructureElement AIR = new StructureElement_Bridge() {

        @Override
        public boolean check(Object t, Level world, int x, int y, int z) {
            return world.getBlockState(new BlockPos(x, y, z)).isAir();
        }

        @Override
        public boolean spawnHint(Object o, Level world, int x, int y, int z, ItemStack trigger) {
            StructureLibAPI.hintParticle(world, x, y, z, Registry.HINT_AIR);
            return true;
        }

        @Override
        public boolean placeBlock(Object o, Level world, int x, int y, int z, ItemStack trigger) {
            world.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
            return false;
        }

        @Override
        @Deprecated
        public PlaceResult survivalPlaceBlock(Object o, Level world, int x, int y, int z, ItemStack trigger,
                AutoPlaceEnvironment env) {
            if (check(o, world, x, y, z)) return PlaceResult.SKIP;
            if (!StructureLibAPI.isBlockTriviallyReplaceable(world, x, y, z, env.getActor())) return PlaceResult.REJECT;
            world.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
            return PlaceResult.ACCEPT;
        }
    };

    @SuppressWarnings("rawtypes")
    private static final IStructureElement NOT_AIR = new StructureElement_Bridge() {

        @Override
        public boolean check(Object t, Level world, int x, int y, int z) {
            return !world.getBlockState(new BlockPos(x, y, z)).isAir();
        }

        @Override
        public boolean spawnHint(Object o, Level world, int x, int y, int z, ItemStack trigger) {
            StructureLibAPI.hintParticle(world, x, y, z, Registry.HINT_NOAIR);
            return true;
        }

        @Override
        public boolean placeBlock(Object o, Level world, int x, int y, int z, ItemStack trigger) {
            world.setBlock(new BlockPos(x, y, z), Registry.HINT_NOAIR.defaultBlockState(), 2);
            return true;
        }

        @Override
        public BlocksToPlace getBlocksToPlace(Object o, Level world, int x, int y, int z, ItemStack trigger,
                AutoPlaceEnvironment env) {
            return BlocksToPlace.create(Objects::nonNull);
        }

        @Override
        public PlaceResult survivalPlaceBlock(Object o, Level world, int x, int y, int z, ItemStack trigger,
                AutoPlaceEnvironment env) {
            if (check(o, world, x, y, z)) return PlaceResult.SKIP;
            // user should place anything here.
            // maybe make this configurable, but for now we try to take some cobble from user
            if (env.getSource().takeOne(new ItemStack(Blocks.COBBLESTONE), false)) {
                world.setBlock(new BlockPos(x, y, z), Blocks.COBBLESTONE.defaultBlockState(), 2);
            }
            return PlaceResult.REJECT;
        }
    };

    @SuppressWarnings("rawtypes")
    private static final IStructureElement ERROR = new StructureElement_Bridge() {

        @Override
        public boolean check(Object t, Level world, int x, int y, int z) {
            return false;
        }

        @Override
        public boolean spawnHint(Object o, Level world, int x, int y, int z, ItemStack trigger) {
            StructureLibAPI.hintParticle(world, x, y, z, Registry.HINT_ERROR);
            return true;
        }

        @Override
        public boolean placeBlock(Object o, Level world, int x, int y, int z, ItemStack trigger) {
            return true;
        }

        @Override
        public PlaceResult survivalPlaceBlock(Object o, Level world, int x, int y, int z, ItemStack trigger,
                AutoPlaceEnvironment env) {
            return PlaceResult.REJECT;
        }
    };

    private StructureUtility() {}

    /**
     * This is a helper method for implementing
     * {@link IStructureElement#survivalPlaceBlock(Object, Level, int, int, int, ItemStack, IItemSource, ServerPlayer, Consumer)}
     * <p>
     * This method will try to look up an {@link BlockItem} for given block from {@code s} and place it at
     * given location. This method assume block at given coord is NOT acceptable, but may or may not be trivially
     * replaceable This method might yield error messages to the player only if there is likely a programming error or
     * exploit.
     *
     * @param s     drain resources from this place
     * @param actor source of action. cannot be null.
     */
    public static PlaceResult survivalPlaceBlock(Block block, Level world, int x, int y, int z, IItemSource s,
                                                 ServerPlayer actor) {
        return survivalPlaceBlock(block, world, x, y, z, s, actor);
    }

    /**
     * This is a helper method for implementing
     * {@link IStructureElement#survivalPlaceBlock(Object, Level, int, int, int, ItemStack, IItemSource, ServerPlayer, Consumer)}
     * <p>
     * This method will try to look up an {@link BlockItem} for given block from {@code s} and place it at
     * given location. This method assume block at given coord is NOT acceptable, but may or may not be trivially
     * replaceable This method might yield error messages to the player only if there is likely a programming error or
     * exploit.
     *
     * @param s     drain resources from this place
     * @param actor source of action. cannot be null.
     */
    public static PlaceResult survivalPlaceBlock(Block block, Level world, int x, int y, int z, IItemSource s,
            Player actor) {
        return survivalPlaceBlock(block, world, x, y, z, s, actor, null);
    }

    /**
     * This is a helper method for implementing
     * {@link IStructureElement#survivalPlaceBlock(Object, Level, int, int, int, ItemStack, IItemSource, ServerPlayer, Consumer)}
     * <p>
     * This method will try to look up an {@link BlockItem} for given block from {@code s} and place it at
     * given location. This method assume block at given coord is NOT acceptable, but may or may not be trivially
     * replaceable This method might yield error messages to the player only if there is likely a programming error or
     * exploit. This method might yield error messages to the chatter.
     *
     * @param s       drain resources from this place
     * @param actor   source of action. cannot be null.
     * @param chatter normal error destination. can be null to suppress them.
     */
    public static PlaceResult survivalPlaceBlock(Block block, Level world, int x, int y, int z, IItemSource s,
            ServerPlayer actor, Consumer<Component> chatter) {
        return survivalPlaceBlock(block, world, x, y, z, s, (Player) actor, chatter);
    }

    /**
     * This is a helper method for implementing
     * {@link IStructureElement#survivalPlaceBlock(Object, Level, int, int, int, ItemStack, IItemSource, ServerPlayer, Consumer)}
     * <p>
     * This method will try to look up an {@link BlockItem} for given block from {@code s} and place it at
     * given location. This method assume block at given coord is NOT acceptable, but may or may not be trivially
     * replaceable This method might yield error messages to the player only if there is likely a programming error or
     * exploit. This method might yield error messages to the chatter.
     *
     * @param s       drain resources from this place
     * @param actor   source of action. cannot be null.
     * @param chatter normal error destination. can be null to suppress them.
     */
    public static PlaceResult survivalPlaceBlock(Block block, Level world, int x, int y, int z, IItemSource s,
            Player actor, Consumer<Component> chatter) {
        if (block == null) throw new NullPointerException();
        if (!StructureLibAPI.isBlockTriviallyReplaceable(world, x, y, z, actor)) return PlaceResult.REJECT;
        Item BlockItem = block.asItem();
        if (!s.takeOne(new ItemStack(BlockItem, 1), false)) {
            if (chatter != null) chatter.accept(
                    new TranslatableComponent(
                            "structurelib.autoplace.error.no_simple_block",
                            new ItemStack(BlockItem, 1).getDisplayName()));
            return PlaceResult.REJECT;
        }
        if (block instanceof ICustomBlockSetting) {
            ICustomBlockSetting block2 = (ICustomBlockSetting) block;
            block2.setBlock(world, x, y, z);
        } else {
            world.setBlock(new BlockPos(x, y, z), block.defaultBlockState(), 2);
        }
        return PlaceResult.ACCEPT;
    }

    /**
     * This is a helper method for implementing
     * {@link IStructureElement#survivalPlaceBlock(Object, Level, int, int, int, ItemStack, IItemSource, ServerPlayer, Consumer)}
     * <p>
     * This method will try to look up an {@link BlockItem} for given block from {@code s} and place it at
     * given location. This method assume block at given coord is NOT acceptable, but may or may not be trivially
     * replaceable This method might yield error messages to the player only if there is likely a programming error or
     * exploit.
     *
     * @param stack a valid stack with stack size of exactly 1. Must be of an BlockItem!
     * @param s     drain resources from this place
     * @param actor source of action. cannot be null.
     */
    public static PlaceResult survivalPlaceBlock(ItemStack stack, NBTMode nbtMode, CompoundTag tag,
            boolean assumeStackPresent, Level world, int x, int y, int z, IItemSource s, ServerPlayer actor) {
        return survivalPlaceBlock(stack, nbtMode, tag, assumeStackPresent, world, x, y, z, s, (Player) actor);
    }

    /**
     * This is a helper method for implementing
     * {@link IStructureElement#survivalPlaceBlock(Object, Level, int, int, int, ItemStack, IItemSource, ServerPlayer, Consumer)}
     * <p>
     * This method will try to look up an {@link BlockItem} for given block from {@code s} and place it at
     * given location. This method assume block at given coord is NOT acceptable, but may or may not be trivially
     * replaceable This method might yield error messages to the player only if there is likely a programming error or
     * exploit.
     *
     * @param stack a valid stack with stack size of exactly 1. Must be of an BlockItem!
     * @param s     drain resources from this place
     * @param actor source of action. cannot be null.
     */
    public static PlaceResult survivalPlaceBlock(ItemStack stack, NBTMode nbtMode, CompoundTag tag,
            boolean assumeStackPresent, Level world, int x, int y, int z, IItemSource s, Player actor) {
        return survivalPlaceBlock(stack, nbtMode, tag, assumeStackPresent, world, x, y, z, s, actor, null);
    }

    /**
     * This is a helper method for implementing
     * {@link IStructureElement#survivalPlaceBlock(Object, Level, int, int, int, ItemStack, IItemSource, ServerPlayer, Consumer)}
     * <p>
     * This method will try to look up an {@link BlockItem} for given block from {@code s} and place it at
     * given location. This method assume block at given coord is NOT acceptable, but may or may not be trivially
     * replaceable This method might yield error messages to the player only if there is likely a programming error or
     * exploit. This method might yield error messages to the chatter.
     *
     * @param stack   a valid stack with stack size of exactly 1. Must be of an BlockItem!
     * @param s       drain resources from this place
     * @param actor   source of action. cannot be null.
     * @param chatter normal error destination. can be null to suppress them.
     */
    public static PlaceResult survivalPlaceBlock(ItemStack stack, NBTMode nbtMode, CompoundTag tag,
            boolean assumeStackPresent, Level world, int x, int y, int z, IItemSource s, ServerPlayer actor,
            @Nullable Consumer<Component> chatter) {
        return survivalPlaceBlock(
                stack,
                nbtMode,
                tag,
                assumeStackPresent,
                world,
                x,
                y,
                z,
                s,
                (Player) actor,
                chatter);
    }

    /**
     * This is a helper method for implementing
     * {@link IStructureElement#survivalPlaceBlock(Object, Level, int, int, int, ItemStack, IItemSource, ServerPlayer, Consumer)}
     * <p>
     * This method will try to look up an {@link BlockItem} for given block from {@code s} and place it at
     * given location. This method assume block at given coord is NOT acceptable, but may or may not be trivially
     * replaceable This method might yield error messages to the player only if there is likely a programming error or
     * exploit. This method might yield error messages to the chatter.
     *
     * @param stack   a valid stack with stack size of exactly 1. Must be of an BlockItem!
     * @param s       drain resources from this place
     * @param actor   source of action. cannot be null.
     * @param chatter normal error destination. can be null to suppress them.
     */
    public static PlaceResult survivalPlaceBlock(ItemStack stack, NBTMode nbtMode, CompoundTag tag,
            boolean assumeStackPresent, Level world, int x, int y, int z, IItemSource s, Player actor,
            @Nullable Consumer<Component> chatter) {
        if (stack.isEmpty()) throw new NullPointerException();
        if (stack.getCount() != 1) throw new IllegalArgumentException();
        if (!(stack.getItem() instanceof BlockItem blockItem)) throw new IllegalArgumentException();
        if (!StructureLibAPI.isBlockTriviallyReplaceable(world, x, y, z, actor)) return PlaceResult.REJECT;
        if (!assumeStackPresent && !s.takeOne(stack, true)) {
            if (chatter != null) chatter.accept(
                    new TranslatableComponent("structurelib.autoplace.error.no_item_stack", stack.getDisplayName()));
            return PlaceResult.REJECT;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!blockItem.place(new BlockPlaceContext(actor, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false))).consumesAction())
            return PlaceResult.REJECT;
        if (!s.takeOne(stack, false))
            // this is bad! probably an exploit somehow. Let's nullify the block we just placed instead
            world.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        return PlaceResult.ACCEPT;
    }

    /**
     * Return a structure element that only allow air (or air equivalents, like Railcraft hidden blocks). You usually
     * don't need to call this yourselves. Use {@code -} in shape to automatically use this. Provided nontheless in case
     * you want this as a fallback to something else.
     */
    @SuppressWarnings("unchecked")
    public static <T> IStructureElement<T> isAir() {
        return AIR;
    }

    /**
     * Return a structure element that allow anything but air (or air equivalents, like Railcraft hidden blocks). You
     * usually don't need to call this yourselves. Use {@code +} in shape to automatically use this. Provided nontheless
     * in case you want this as a fallback to something else.
     */
    @SuppressWarnings("unchecked")
    public static <T> IStructureElement<T> notAir() {
        return NOT_AIR;
    }

    /**
     * Check returns false. Placement is always handled by this and does nothing. Makes little to no use it in fallback
     * chain.
     */
    @SuppressWarnings("unchecked")
    public static <T> IStructureElement<T> error() {
        return ERROR;
    }

    // region hint only

    /**
     * Spawn a hint with given amount of dots. Check always returns: true. Only useful as a fallback, e.g.
     * {@link #ofBlockRegistryName(String, String, IStructureElement)}
     */
    public static <T> IStructureElementNoPlacement<T> ofHint(int dots) {
        return new IStructureElementNoPlacement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return true;
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                StructureLibAPI.hintParticle(world, x, y, z, Registry.getHint(dots));
                return false;
            }
        };
    }

    /**
     * Spawn a hint with given textures. Check always returns: true. Only useful as a fallback, e.g.
     * {@link #ofBlockRegistryName(String, String, IStructureElement)}
     */
    public static <T> IStructureElementNoPlacement<T> ofHintDeferred(Supplier<TextureAtlasSprite[]> icons) {
        return new IStructureElementNoPlacement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return true;
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                StructureLibAPI.hintParticle(world, x, y, z, icons.get());
                return false;
            }
        };
    }

    /**
     * Spawn a hint with given amount of textures and tint. Check always returns: true. Only useful as a fallback, e.g.
     * {@link #ofBlockRegistryName(String, String, IStructureElement)}
     */
    public static <T> IStructureElementNoPlacement<T> ofHintDeferred(Supplier<TextureAtlasSprite[]> icons, short[] RGBa) {
        return new IStructureElementNoPlacement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return true;
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                StructureLibAPI.hintParticleTinted(world, x, y, z, icons.get(), RGBa);
                return false;
            }
        };
    }

    // endregion

    // region block

    /**
     * A more primitive variant of {@link #ofBlocksTiered(ITierConverter, List, Object, BiConsumer, Function)} Main
     * difference is this one is check only.
     * <p>
     * Note that if tierExtractor returns a null, this block will be rejected immediately. This makes null a potent
     * candidate for notSet.
     * <p>
     * See documentation on the other overload for more information.
     *
     * @see #ofBlocksTiered(ITierConverter, Object, BiConsumer, Function)
     */
    public static <T, TIER> IStructureElementCheckOnly<T> ofBlocksTiered(ITierConverter<TIER> tierExtractor,
            @Nullable TIER notSet, BiConsumer<T, TIER> setter, Function<T, TIER> getter) {
        if (tierExtractor == null) throw new IllegalArgumentException();
        if (setter == null) throw new IllegalArgumentException();
        if (getter == null) throw new IllegalArgumentException();

        return (t, world, x, y, z) -> {
            Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
            TIER tier = tierExtractor.convert(block);
            if (tier == null) return false;
            TIER current = getter.apply(t);
            if (Objects.equals(notSet, current)) {
                if (Objects.equals(notSet, tier)) {
                    if (StructureLib.PANIC_MODE) {
                        throw new AssertionError("tierExtractor should never return notSet: " + notSet);
                    } else {
                        StructureLib.LOGGER.error("#########################################");
                        StructureLib.LOGGER.error("#########################################");
                        StructureLib.LOGGER
                                .error("tierExtractor should never return notSet: {}", notSet, new Throwable());
                        StructureLib.LOGGER.error("#########################################");
                        StructureLib.LOGGER.error("#########################################");
                    }
                }
                setter.accept(t, tier);
                return true;
            }
            return Objects.equals(current, tier);
        };
    }

    /**
     * Element representing a component with different tiers. Your multi will accept any of them (as long as player does
     * not mix them (*)), but would like to know which is used. Player can use more blueprint to get hints denoting more
     * advanced components.
     * <p>
     * (*): Implementation wise, this structure element will only accept a block if its tier is the same as existing
     * tier or if existing tier is unset (i.e. value of third argument)
     * <p>
     * Above all else, you need to decide what tier you are going to use. For simpler cases, Integer is a good choice.
     * You can also use an existing Enum. Tier can never be null though. It can also be a String or a
     * {@link ResourceLocation}, or basically anything. It doesn't even have to implement
     * {@link Comparable}.
     * <p>
     * This assumes you will reset the backing storage, and on the first occurrence getter would return notSet. You can
     * also make getter to return other value to forcefully select a certain tier, but you're probably better off using
     * block adders or {@link #ofBlock(Block, Block)} and its overloads
     * <p>
     * Implementation wise, this allows a block only when we don't yet have a tier, or the block at that location has
     * the same tier as we already have.
     * <p>
     * There is yet no BlockEntity counterpart of this utility. Feel free to submit a PR to add it.
     * <p>
     * For most typical use cases, you will NOT want to return notSet from your tierExtractor. If you do so, we will
     * have this kind of chain of events:
     * <ul>
     * <li>at check start, tier is reset to notSet
     * <li>at first occurrence, ofBlocksTiered finds your current tier is notSet, and the block's tier is notSet, so
     * it's accepted
     * <li>at 2nd, 3rd..., same thing in 2 happens
     * <li>at last occurrence, player placed a valid block with a tier not notSet. ofBlocksTiered now sets your current
     * tier to this tier, and accept the block
     * <li>check got notified structure is sound, and tier is not notSet. it marks the structure as complete and start
     * doing its business
     * <li>Player enjoy a (probably) hilariously shaped multi and (probably) reduced build cost.
     * </ul>
     * <p>
     * <b>Example:</b>
     * <p>
     * Assume you have 16 tier, each map to one block's 16 different meta. You will usually want something like this
     *
     * <pre>
     * public class Tile extends BlockEntity {
     *     private static final Block B = getTargetBlock();
     *     private static final IStructureDefinition&lt;Tile&gt; S = IStructureDefinition.&lt;Tile&gt;builder()
     *            // ... addShape, and other elements
     *            .addElement('c', ofBlocksTiered((b, m) -&gt; b == B ? m : null,
     *                                            IntStream.range(0, 16).mapToObj(i -&gt; Pair.of(B, i)).collect(Collectors.toList()),
     *                                            -1,
     *                                            (t, m) -&gt; t.tier = m,
     *                                            t -&gt; t.tier))
     *            .build();
     *     private int tier;
     *
     *     public boolean check() {
     *         tier = -1;
     *         if (!S.check(...))
     *             return false;
     *         return isTierAcceptable(tier);
     *     }
     * }
     * </pre>
     *
     * @param tierExtractor a function to extract tier info from a block. This function can return null and will never
     *                      be passed a null block or an invalid block meta.
     * @param allKnownTiers A list of all known tiers as of calling. Can be empty or null. No hint will be spawned if
     *                      empty or null. Cannot have null elements. First element denotes the most primitive tier.
     *                      Last element denotes the most advanced tier. If not all tiers are available at definition
     *                      construction time, use {@link #lazy(Supplier)} or its overloads to delay a bit. This list is
     *                      only for hint particle spawning and survival build. The hint/autoplace code will choose 1st
     *                      pair to spawn/place if trigger item has master channel data of 1, 2nd pair if 2, and so on.
     * @param notSet        The value returned from {@code getter} when there were no tier info found in T yet. Can be
     *                      null.
     * @param getter        a function to retrieve the current tier from context object
     * @param setter        a function to set the current tier into context object
     */
    public static <T, TIER> IStructureElement<T> ofBlocksTiered(ITierConverter<TIER> tierExtractor,
            @Nullable List<Block> allKnownTiers, @Nullable TIER notSet, BiConsumer<T, TIER> setter,
            Function<T, TIER> getter) {
        List<Block> hints = allKnownTiers == null ? Collections.emptyList() : allKnownTiers;
        if (hints.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException();
        IStructureElementCheckOnly<T> check = ofBlocksTiered(tierExtractor, notSet, setter, getter);
        return new StructureElement_Bridge<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return check.check(t, world, x, y, z);
            }

            private Block getHint(ItemStack trigger) {
                return hints.get(Math.min(Math.max(trigger.getCount(), 1), hints.size()) - 1);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                Block hint = getHint(trigger);
                if (hint == null) return false;
                StructureLibAPI.hintParticle(world, x, y, z, hint);
                return true;
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                Block hint = getHint(trigger);
                if (hint == null) return false;
                if (hint instanceof ICustomBlockSetting) {
                    ICustomBlockSetting block = (ICustomBlockSetting) hint;
                    block.setBlock(world, x, y, z);
                } else {
                    world.setBlock(new BlockPos(x, y, z), hint.defaultBlockState(), 2);
                }
                return true;
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                Block hint = getHint(trigger);
                return BlocksToPlace.create(hint);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                Block hint = getHint(trigger);
                if (hint == null) return PlaceResult.REJECT; // TODO or SKIP?
                Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                TIER tier = tierExtractor.convert(block);
                if (Objects.equals(tier, tierExtractor.convert(hint)))
                    return PlaceResult.SKIP;
                return StructureUtility.survivalPlaceBlock(
                        hint,
                        world,
                        x,
                        y,
                        z,
                        env.getSource(),
                        env.getActor(),
                        env.getChatter());
            }
        };
    }

    /**
     * Denote a block using registry names. This can be useful to get around mod loading order issues.
     * While no immediate error will be thrown, client code should ensure said mod is loaded and said mod is present,
     * otherwise the element will effectively become an {@link #error()}.
     * <p>
     * Will place block or hint using the given meta if wildcard is true.
     */
    public static <T> IStructureElement<T> ofBlockRegistryName(String modid, String registryName) {
        if (StringUtils.isBlank(registryName)) throw new IllegalArgumentException();
        if (StringUtils.isBlank(modid)) throw new IllegalArgumentException();
        return new StructureElement_Bridge<T>() {

            private Block block;

            private Block getBlock() {
                if (block == null) block = net.minecraft.core.Registry.BLOCK.get(new ResourceLocation(modid, registryName));
                return block;
            }

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return world.getBlockState(new BlockPos(x, y, z)).getBlock() == getBlock();
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                if (getBlock() == null) return error().spawnHint(t, world, x, y, z, trigger);
                StructureLibAPI.hintParticle(world, x, y, z, getBlock());
                return true;
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                if (getBlock() == null) return error().placeBlock(t, world, x, y, z, trigger);
                world.setBlock(new BlockPos(x, y, z), getBlock().defaultBlockState(), 2);
                return true;
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                if (getBlock() == null) return error().getBlocksToPlace(t, world, x, y, z, trigger, env);
                return BlocksToPlace.create(getBlock());
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                if (check(t, world, x, y, z)) return PlaceResult.SKIP;
                if (getBlock() == null) return PlaceResult.REJECT;
                return StructureUtility.survivalPlaceBlock(
                        getBlock(),
                        world,
                        x,
                        y,
                        z,
                        env.getSource(),
                        env.getActor(),
                        env.getChatter());
            }
        };
    }

    /**
     * Similiar to the other overload, but allows client code to specify a fallback in case said block was not found
     * later when the element got called.
     * This is slightly different to using the other overload and another element in a
     * {@link #ofChain(IStructureElement[])}. Here fallback will only ever be used if this fails, where ofChain would
     * form an OR relationship even if the mod is loaded and the block exists in registry.
     */
    public static <T> IStructureElement<T> ofBlockRegistryName(String modid, String registryName,
                                                               IStructureElement<T> fallback) {
        if (StringUtils.isBlank(registryName)) throw new IllegalArgumentException();
        if (StringUtils.isBlank(modid)) throw new IllegalArgumentException();
        if (fallback == null) throw new IllegalArgumentException();
        return new IStructureElement<T>() {

            private Block block;
            private boolean initialized;

            private boolean init() {
                if (!initialized) {
                    block = net.minecraft.core.Registry.BLOCK.get(new ResourceLocation(modid, registryName));
                    initialized = true;
                }
                return block != null;
            }

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                if (init()) return world.getBlockState(new BlockPos(x, y, z)).getBlock() != block;
                else return fallback.check(t, world, x, y, z);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                if (init()) {
                    StructureLibAPI.hintParticle(world, x, y, z, block);
                    return true;
                } else return fallback.spawnHint(t, world, x, y, z, trigger);
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                if (init()) {
                    world.setBlock(new BlockPos(x, y, z), block.defaultBlockState(), 2);
                    return true;
                } else return fallback.placeBlock(t, world, x, y, z, trigger);
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                if (init()) return BlocksToPlace.create(block);
                return fallback.getBlocksToPlace(t, world, x, y, z, trigger, env);
            }

            @Override
            @Deprecated
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                if (check(t, world, x, y, z)) return PlaceResult.SKIP;
                if (init()) return StructureUtility.survivalPlaceBlock(block, world, x, y, z, s, actor, chatter);
                return fallback.survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                if (check(t, world, x, y, z)) return PlaceResult.SKIP;
                if (init()) return StructureUtility.survivalPlaceBlock(
                        block,
                        world,
                        x,
                        y,
                        z,
                        env.getSource(),
                        env.getActor(),
                        env.getChatter());
                return fallback.survivalPlaceBlock(t, world, x, y, z, trigger, env);
            }
        };
    }

    /**
     * Accept one (block). Spawn hint particles using an alternative block. Not very useful...
     */
    public static <T> IStructureElementNoPlacement<T> ofBlockHint(Block block, Block hintBlock) {
        if (block == null || hintBlock == null) {
            throw new IllegalArgumentException();
        }
        return new IStructureElementNoPlacement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                Block worldBlock = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                return block == worldBlock;
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                StructureLibAPI.hintParticle(world, x, y, z, hintBlock);
                return true;
            }
        };
    }

    /**
     * Accept one (block). Same as {@link #ofBlock(Block)}, except it explicitly turns off creative/survival
     * build.
     */
    public static <T> IStructureElementNoPlacement<T> ofBlockHint(Block block) {
        return ofBlockHint(block, block);
    }

    /**
     * Add a block using block adder. Spawn hints using given hint block.
     * <p>
     * Useful when your logic is very complex. Does not support autoplace.
     */
    public static <T> IStructureElementNoPlacement<T> ofBlockAdderHint(IBlockAdder<T> iBlockAdder, Block hintBlock) {
        if (iBlockAdder == null || hintBlock == null) {
            throw new IllegalArgumentException();
        }
        return new IStructureElementNoPlacement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                Block worldBlock = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                return iBlockAdder.apply(t, worldBlock);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                StructureLibAPI.hintParticle(world, x, y, z, hintBlock);
                return true;
            }
        };
    }

    /**
     * Accept a block. Spawn hint/autoplace using another.
     *
     * @param block        accepted block
     * @param defaultBlock hint block
     */
    public static <T> IStructureElement<T> ofBlock(Block block, Block defaultBlock) {
        if (block == null || defaultBlock == null) {
            throw new IllegalArgumentException();
        }
        if (block instanceof ICustomBlockSetting) {
            return new IStructureElement<T>() {

                @Override
                public boolean check(T t, Level world, int x, int y, int z) {
                    Block worldBlock = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    return block == worldBlock;
                }

                @Override
                public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                    ((ICustomBlockSetting) defaultBlock).setBlock(world, x, y, z);
                    return true;
                }

                @Override
                public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                    StructureLibAPI.hintParticle(world, x, y, z, defaultBlock);
                    return true;
                }

                @Override
                public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                        AutoPlaceEnvironment env) {
                    return BlocksToPlace.create(block);
                }
            };
        } else {
            return new IStructureElement<T>() {

                @Override
                public boolean check(T t, Level world, int x, int y, int z) {
                    Block worldBlock = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    return block == worldBlock;
                }

                @Override
                public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                    world.setBlock(new BlockPos(x, y, z), defaultBlock.defaultBlockState(), 2);
                    return true;
                }

                @Override
                public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                    StructureLibAPI.hintParticle(world, x, y, z, defaultBlock);
                    return true;
                }

                @Override
                public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                        AutoPlaceEnvironment env) {
                    return BlocksToPlace.create(block);
                }
            };
        }
    }

    /**
     * Accept a single block. Most primitive form of structure.
     */
    public static <T> IStructureElement<T> ofBlock(Block block) {
        return ofBlock(block, block);
    }

    public static <T> IStructureElement<T> ofBlock(TagKey<Block> blockTag){
        return new IStructureElementNoPlacement<T>() {
            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return world.getBlockState(new BlockPos(x, y, z)).is(blockTag);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return false;
            }
        };
    }

    public static <T> IStructureElement<T> ofBlock(TagKey<Block> blockTag, Block hint){
        return new IStructureElementNoPlacement<T>() {
            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return world.getBlockState(new BlockPos(x, y, z)).is(blockTag);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                StructureLibAPI.hintParticle(world, x, y, z, hint);
                return true;
            }
        };
    }

    // endregion

    // region adders

    /**
     * Add a block using block adder. Spawn hints/autoplace using given hint block and meta.
     * <p>
     * Useful when your logic is very complex.
     */
    public static <T> IStructureElement<T> ofBlockAdder(IBlockAdder<T> iBlockAdder, Block defaultBlock) {
        if (iBlockAdder == null || defaultBlock == null) {
            throw new IllegalArgumentException();
        }
        if (defaultBlock instanceof ICustomBlockSetting) {
            return new StructureElement_Bridge<T>() {

                @Override
                public boolean check(T t, Level world, int x, int y, int z) {
                    Block worldBlock = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    return iBlockAdder.apply(t, worldBlock);
                }

                @Override
                public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                    ((ICustomBlockSetting) defaultBlock).setBlock(world, x, y, z);
                    return true;
                }

                @Override
                public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                    StructureLibAPI.hintParticle(world, x, y, z, defaultBlock);
                    return true;
                }

                @Override
                @Deprecated
                public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                        AutoPlaceEnvironment env) {
                    if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == defaultBlock)
                        return PlaceResult.SKIP;
                    return StructureUtility.survivalPlaceBlock(
                            defaultBlock,
                            world,
                            x,
                            y,
                            z,
                            env.getSource(),
                            env.getActor(),
                            env.getChatter());
                }
            };
        } else {
            return new StructureElement_Bridge<T>() {

                @Override
                public boolean check(T t, Level world, int x, int y, int z) {
                    Block worldBlock = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    return iBlockAdder.apply(t, worldBlock);
                }

                @Override
                public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                    world.setBlock(new BlockPos(x, y, z), defaultBlock.defaultBlockState(), 2);
                    return true;
                }

                @Override
                public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                    StructureLibAPI.hintParticle(world, x, y, z, defaultBlock);
                    return true;
                }

                @Override
                @Deprecated
                public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                        AutoPlaceEnvironment env) {
                    if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == defaultBlock)
                        return PlaceResult.SKIP;
                    return StructureUtility.survivalPlaceBlock(
                            defaultBlock,
                            world,
                            x,
                            y,
                            z,
                            env.getSource(),
                            env.getActor(),
                            env.getChatter());
                }
            };
        }
    }

    public static <T> IStructureElement<T> ofBlockAdder(IBlockAdder<T> iBlockAdder, int dots) {
        return ofBlockAdder(iBlockAdder, Registry.getHint(dots));
    }

    /**
     * Try to add a structure element with a tile entity. Note that tile adder will be called with a null argument at
     * locations without tile entity.
     */
    public static <T> IStructureElementNoPlacement<T> ofTileAdder(ITileAdder<T> iTileAdder, Block hintBlock) {
        if (iTileAdder == null || hintBlock == null) {
            throw new IllegalArgumentException();
        }
        return new IStructureElementNoPlacement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                BlockEntity tileEntity = world.getBlockEntity(new BlockPos(x, y, z));
                // This used to check if it's a GT tile. Since this is now an standalone mod we no longer do this
                return iTileAdder.apply(t, tileEntity);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                StructureLibAPI.hintParticle(world, x, y, z, hintBlock);
                return true;
            }
        };
    }

    /**
     * Try to add a structure element with a particular type of tile entity. Note that tile adder will not be called at
     * locations without a tile entity.
     */
    public static <T, E> IStructureElementNoPlacement<T> ofSpecificTileAdder(BiPredicate<T, E> iTileAdder,
            Class<E> tileClass, Block hintBlock) {
        if (iTileAdder == null || hintBlock == null || tileClass == null) {
            throw new IllegalArgumentException();
        }
        return new IStructureElementNoPlacement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                BlockEntity tileEntity = world.getBlockEntity(new BlockPos(x, y, z));
                // This used to check if it's a GT tile. Since this is now an standalone mod we no longer do this
                return tileClass.isInstance(tileEntity) && iTileAdder.test(t, tileClass.cast(tileEntity));
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                StructureLibAPI.hintParticle(world, x, y, z, hintBlock);
                return true;
            }
        };
    }
    // No more hatch adder. Implement it via tile adder. We could of course add a wrapper around it in gregtech, but not
    // any more in this standalone mod.

    // endregion

    // region side effects

    /**
     * Call a callback if downstream element returned true in check.
     *
     * @param onCheckPass side effect
     * @param element     downstream
     */
    public static <B extends IStructureElement<T>, T> IStructureElement<T> onElementPass(Consumer<T> onCheckPass,
            B element) {
        return new IStructureElement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                boolean check = element.check(t, world, x, y, z);
                if (check) {
                    onCheckPass.accept(t);
                }
                return check;
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return element.placeBlock(t, world, x, y, z, trigger);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return element.spawnHint(t, world, x, y, z, trigger);
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return element.getBlocksToPlace(t, world, x, y, z, trigger, env);
            }

            @Override
            @Deprecated
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                return element.survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return element.survivalPlaceBlock(t, world, x, y, z, trigger, env);
            }
        };
    }

    /**
     * Call a callback if downstream element returned false in check.
     *
     * @param onFail  side effect
     * @param element downstream
     */
    public static <B extends IStructureElement<T>, T> IStructureElement<T> onElementFail(Consumer<T> onFail,
            B element) {
        return new IStructureElement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                boolean check = element.check(t, world, x, y, z);
                if (!check) {
                    onFail.accept(t);
                }
                return check;
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return element.placeBlock(t, world, x, y, z, trigger);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return element.spawnHint(t, world, x, y, z, trigger);
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return element.getBlocksToPlace(t, world, x, y, z, trigger, env);
            }

            @Override
            @Deprecated
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                return element.survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return element.survivalPlaceBlock(t, world, x, y, z, trigger, env);
            }
        };
    }
    /**
     * Call different callbacks depending on whether a downstream element returned false or true in check.
     *
     * @param onFail  side effect if check fails
     * @param onPass side ffect if check passes
     * @param element downstream
     */
    public static <B extends IStructureElement<T>, T> IStructureElement<T> onElementFailAndPass(Consumer<T> onFail, Consumer<T> onPass,
                                                                                         B element) {
        return new IStructureElement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                boolean check = element.check(t, world, x, y, z);
                if (!check) {
                    onFail.accept(t);
                } else {
                    onPass.accept(t);
                }
                return check;
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return element.placeBlock(t, world, x, y, z, trigger);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return element.spawnHint(t, world, x, y, z, trigger);
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                                                  AutoPlaceEnvironment env) {
                return element.getBlocksToPlace(t, world, x, y, z, trigger, env);
            }

            @Override
            @Deprecated
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                                                  IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                return element.survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                                                  AutoPlaceEnvironment env) {
                return element.survivalPlaceBlock(t, world, x, y, z, trigger, env);
            }
        };
    }


    // endregion

    /**
     * Enable this structure element only if given predicate returns true.
     * <p>
     * Return SKIP when survival auto place if given predicate returns false.
     */
    public static <T> IStructureElement<T> onlyIf(Predicate<? super T> predicate,
            IStructureElement<? super T> downstream) {
        return onlyIf(predicate, downstream, PlaceResult.SKIP);
    }

    /**
     * Enable this structure element only if given predicate returns true.
     *
     * @param placeResultWhenDisabled value to return for survival auto place when predicate returns false
     */
    public static <T> IStructureElement<T> onlyIf(Predicate<? super T> predicate,
            IStructureElement<? super T> downstream, PlaceResult placeResultWhenDisabled) {
        return new IStructureElement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return predicate.test(t) && downstream.check(t, world, x, y, z);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return predicate.test(t) && downstream.spawnHint(t, world, x, y, z, trigger);
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return predicate.test(t) && downstream.placeBlock(t, world, x, y, z, trigger);
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                if (predicate.test(t)) return downstream.getBlocksToPlace(t, world, x, y, z, trigger, env);
                return BlocksToPlace.createEmpty();
            }

            @Override
            @Deprecated
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                if (predicate.test(t))
                    return downstream.survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
                return placeResultWhenDisabled;
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                if (predicate.test(t)) return downstream.survivalPlaceBlock(t, world, x, y, z, trigger, env);
                return placeResultWhenDisabled;
            }
        };
    }

    /**
     * This allows you to compose different {@link IStructureElement} to form a <b>OR</b> chain. As with any other OR
     * operator, this one exhibits short-circuiting behavior, i.e. it will not call next structure element if previous
     * one succeeded. (*)
     * <p>
     * This allows you e.g. accept both a glass block using {@link #ofBlock(Block, Block)} and a piece of air
     * using {@link #isAir()}. It will not attempt to capture any errors though, so next one will not be tried if
     * previous one would crash.
     * <p>
     * (*): For survival auto place, it will * REJECT, if all structure element REJECT * SKIP, if 1 or more structure
     * element SKIP and the rest structure element (0 or more) REJECT * any other result, **immediately** upon any
     * structure element returns these other results. This behavior is not 100% fixed and might change later on, but we
     * will send the notice on a best effort basis.
     * <p>
     * Take care while chaining, as it will try to call every structure element until it returns true. If none does it
     * will finally return false.
     */
    @SafeVarargs
    public static <T> IStructureElementChain<T> ofChain(IStructureElement<T>... elementChain) {
        if (elementChain == null || elementChain.length == 0) {
            throw new IllegalArgumentException();
        }
        for (IStructureElement<T> iStructureElement : elementChain) {
            if (iStructureElement == null) {
                throw new IllegalArgumentException();
            }
        }
        return () -> elementChain;
    }

    /**
     * This allows you to compose different {@link IStructureElement} to form a <b>OR</b> chain.
     * <p>
     * Practically no difference with the other overload.
     *
     * @see #ofChain(IStructureElement[])
     */
    @SuppressWarnings("unchecked")
    public static <T> IStructureElementChain<T> ofChain(List<IStructureElement<T>> elementChain) {
        return ofChain(elementChain.toArray(new IStructureElement[0]));
    }

    // region context

    /**
     * Switch to the extended context object in the downstream element
     *
     * @param elem  downstream element
     * @param <CTX> extended context type
     * @param <T>   existing context object type
     */
    public static <CTX, T extends IWithExtendedContext<CTX>> IStructureElement<T> withContext(
            IStructureElement<CTX> elem) {
        return new IStructureElement<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return elem.check(t.getCurrentContext(), world, x, y, z);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return elem.spawnHint(t.getCurrentContext(), world, x, y, z, trigger);
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return elem.placeBlock(t.getCurrentContext(), world, x, y, z, trigger);
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return elem.getBlocksToPlace(t.getCurrentContext(), world, x, y, z, trigger, env);
            }

            @Override
            @Deprecated
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                return elem.survivalPlaceBlock(t.getCurrentContext(), world, x, y, z, trigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return elem.survivalPlaceBlock(t.getCurrentContext(), world, x, y, z, trigger, env);
            }
        };
    }
    // endregion

    // region defer

    /**
     * Similar to defer, but caches the first returned element returned and won't call it again. Initialization is not
     * thread safe. These both allow the structure element **constructor** to access properties only present on the
     * context object (e.g. GT5 multiblock controller), e.g. hatch texture index to use. Use `lazy` if the data you
     * access will remain constant across different context object.
     */
    public static <T> IStructureElementDeferred<T> lazy(Supplier<IStructureElement<T>> to) {
        if (to == null) {
            throw new IllegalArgumentException();
        }
        return new LazyStructureElement<>(t -> to.get());
    }

    /**
     * This will defer the actual instantiation of structure element until the <b>first time</b> structure code is
     * actually called.
     * <p>
     * Similar to defer, but caches the first returned element returned and won't call it again. Initialization is not
     * thread safe. This will allow the structure element <b>constructor</b> to access properties only present on the
     * context object (e.g. GT5 multiblock controller), e.g. hatch texture index to use. Use this if the data you access
     * will remain constant.
     *
     * @param to create structure element from the first context object passed in
     */
    public static <T> IStructureElementDeferred<T> lazy(Function<T, IStructureElement<T>> to) {
        if (to == null) {
            throw new IllegalArgumentException();
        }
        return new LazyStructureElement<>(to);
    }

    /**
     * This will defer the actual instantiation of structure element until the structure code is actually called. This
     * will allow the structure element <b>constructor</b> to access properties only present on the context object (e.g.
     * GT5 multiblock controller), e.g. hatch texture index to use. Use this if it might change for the same structure
     * definition, e.g. if your structure might switch mode and change some parameter, while basically remain the same
     * shape. Using this while {@link #lazy(Supplier)} is enough will not break anything, but would incur unnecessary
     * performance overhead.
     *
     * @param to downstream element supplier
     */
    public static <T> IStructureElementDeferred<T> defer(Supplier<IStructureElement<T>> to) {
        if (to == null) {
            throw new IllegalArgumentException();
        }
        return new IStructureElementDeferred<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return to.get().check(t, world, x, y, z);
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return to.get().placeBlock(t, world, x, y, z, trigger);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return to.get().spawnHint(t, world, x, y, z, trigger);
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return to.get().getBlocksToPlace(t, world, x, y, z, trigger, env);
            }

            @Override
            @Deprecated
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                return to.get().survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return to.get().survivalPlaceBlock(t, world, x, y, z, trigger, env);
            }
        };
    }

    /**
     * This will defer the actual instantiation of structure element until the structure code is actually called. This
     * will allow the structure element <b>constructor</b> to access properties only present on the context object (e.g.
     * GT5 multiblock controller), e.g. hatch texture index to use. Use this if it might change for the same structure
     * definition, e.g. if your structure might switch mode and change some parameter, while basically remain the same
     * shape. Using this while {@link #lazy(Supplier)} is enough will not break anything, but would incur unnecessary
     * performance overhead.
     *
     * @param to downstream element supplier
     */
    public static <T> IStructureElementDeferred<T> defer(Function<T, IStructureElement<T>> to) {
        if (to == null) {
            throw new IllegalArgumentException();
        }
        return new IStructureElementDeferred<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return to.apply(t).check(t, world, x, y, z);
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return to.apply(t).placeBlock(t, world, x, y, z, trigger);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return to.apply(t).spawnHint(t, world, x, y, z, trigger);
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return to.apply(t).getBlocksToPlace(t, world, x, y, z, trigger, env);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                return to.apply(t).survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return to.apply(t).survivalPlaceBlock(t, world, x, y, z, trigger, env);
            }
        };
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use.
     *
     * @param keyExtractor extract a key from the context object
     * @param map          all possible structure element
     * @deprecated renamed to partitionBy
     */
    @Deprecated
    public static <T, K> IStructureElementDeferred<T> defer(Function<T, K> keyExtractor,
            Map<K, IStructureElement<T>> map) {
        return partitionBy(keyExtractor, map);
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns a key not
     * found in map.
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     *
     * @param keyExtractor extract a key from the context object
     * @param map          all possible structure element
     */
    public static <T, K> IStructureElementDeferred<T> partitionBy(Function<T, K> keyExtractor,
            Map<K, IStructureElement<T>> map) {
        if (keyExtractor == null || map == null) {
            throw new IllegalArgumentException();
        }
        return defer(keyExtractor.andThen(map::get));
    }

    /**
     * This is the switch block for structure code, with default case support.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. The fallback will only be used when keyExtractor returns a value not found in given map. This will play
     * nicely in combination with {@link #error()}
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     *
     * @param keyExtractor extract a key from the context object
     * @param map          all possible structure element
     * @param defaultElem  element to use when keyExtractor returns a value not found in given map
     * @deprecated renamed to partitionBy
     */
    @Deprecated
    public static <T, K> IStructureElementDeferred<T> defer(Function<T, K> keyExtractor,
            Map<K, IStructureElement<T>> map, IStructureElement<T> defaultElem) {
        return partitionBy(keyExtractor, map, defaultElem);
    }

    /**
     * This is the switch block for structure code, with default case support.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. The fallback will only be used when keyExtractor returns a value not found in given map. This will play
     * nicely in combination with {@link #error()}
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     *
     * @param keyExtractor extract a key from the context object
     * @param map          all possible structure element
     * @param defaultElem  element to use when keyExtractor returns a value not found in given map
     */
    public static <T, K> IStructureElementDeferred<T> partitionBy(Function<T, K> keyExtractor,
            Map<K, IStructureElement<T>> map, IStructureElement<T> defaultElem) {
        if (keyExtractor == null || map == null) {
            throw new IllegalArgumentException();
        }
        return defer(keyExtractor.andThen(key -> map.getOrDefault(key, defaultElem)));
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * Do pay place null values in the array.
     *
     * @param keyExtractor extract an index from the context object
     * @param array        all possible structure element
     * @deprecated renamed to partitionBy
     */
    @SafeVarargs
    @Deprecated
    public static <T> IStructureElementDeferred<T> defer(Function<T, Integer> keyExtractor,
            IStructureElement<T>... array) {
        return partitionBy(keyExtractor, array);
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * Do pay place null values in the array.
     *
     * @param keyExtractor extract an index from the context object
     * @param array        all possible structure element
     */
    @SafeVarargs
    public static <T> IStructureElementDeferred<T> partitionBy(Function<T, Integer> keyExtractor,
            IStructureElement<T>... array) {
        if (keyExtractor == null || array == null) {
            throw new IllegalArgumentException();
        }
        return defer(keyExtractor.andThen(i -> array[i]));
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * Do pay place null values in the array.
     *
     * @param keyExtractor extract an index from the context object
     * @param array        all possible structure element
     * @deprecated renamed to partitionBy
     */
    @Deprecated
    public static <T> IStructureElementDeferred<T> defer(Function<T, Integer> keyExtractor,
            List<IStructureElement<T>> array) {
        return partitionBy(keyExtractor, array);
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * Do pay place null values in the array.
     *
     * @param keyExtractor extract an index from the context object
     * @param array        all possible structure element
     */
    @SuppressWarnings("unchecked")
    public static <T> IStructureElementDeferred<T> partitionBy(Function<T, Integer> keyExtractor,
            List<IStructureElement<T>> array) {
        return partitionBy(keyExtractor, array.toArray(new IStructureElement[0]));
    }

    /**
     * This will defer the actual instantiation of structure element until the structure code is actually called. This
     * will allow the structure element <b>constructor</b> to access properties only present on the context object (e.g.
     * GT5 multiblock controller), e.g. hatch texture index to use. Use this if it might change for the same structure
     * definition, e.g. if your structure might switch mode and change some parameter, while basically remain the same
     * shape. Using this while {@link #lazy(Supplier)} is enough will not break anything, but would incur unnecessary
     * performance overhead.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     *
     * @param to downstream element supplier
     */
    public static <T> IStructureElementDeferred<T> defer(BiFunction<T, ItemStack, IStructureElement<T>> to) {
        if (to == null) {
            throw new IllegalArgumentException();
        }
        return new IStructureElementDeferred<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return to.apply(t, null).check(t, world, x, y, z);
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return to.apply(t, trigger).placeBlock(t, world, x, y, z, trigger);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return to.apply(t, trigger).spawnHint(t, world, x, y, z, trigger);
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return to.apply(t, trigger).getBlocksToPlace(t, world, x, y, z, trigger, env);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                return to.apply(t, trigger).survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return to.apply(t, trigger).survivalPlaceBlock(t, world, x, y, z, trigger, env);
            }
        };
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns a key not
     * found in map.
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     *
     * @param keyExtractor extract a key from the context object and trigger item
     * @param map          all possible structure element
     * @deprecated renamed to partitionBy
     */
    @Deprecated
    public static <T, K> IStructureElementDeferred<T> defer(BiFunction<T, ItemStack, K> keyExtractor,
            Map<K, IStructureElement<T>> map) {
        return partitionBy(keyExtractor, map);
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns a key not
     * found in map.
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     *
     * @param keyExtractor extract a key from the context object and trigger item
     * @param map          all possible structure element
     */
    public static <T, K> IStructureElementDeferred<T> partitionBy(BiFunction<T, ItemStack, K> keyExtractor,
            Map<K, IStructureElement<T>> map) {
        if (keyExtractor == null || map == null) {
            throw new IllegalArgumentException();
        }
        return defer(keyExtractor.andThen(map::get));
    }

    /**
     * This is the switch block for structure code, with default case support.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. The fallback will only be used when keyExtractor returns a value not found in given map. This will play
     * nicely in combination with {@link #error()}
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     *
     * @param keyExtractor extract a key from the context object and trigger item
     * @param map          all possible structure element
     * @param defaultElem  element to use when keyExtractor returns a value not found in given map
     * @deprecated renamed to partitionBy
     */
    @Deprecated
    public static <T, K> IStructureElementDeferred<T> defer(BiFunction<T, ItemStack, K> keyExtractor,
            Map<K, IStructureElement<T>> map, IStructureElement<T> defaultElem) {
        return partitionBy(keyExtractor, map, defaultElem);
    }

    /**
     * This is the switch block for structure code, with default case support.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. The fallback will only be used when keyExtractor returns a value not found in given map. This will play
     * nicely in combination with {@link #error()}
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     *
     * @param keyExtractor extract a key from the context object and trigger item
     * @param map          all possible structure element
     * @param defaultElem  element to use when keyExtractor returns a value not found in given map
     */
    public static <T, K> IStructureElementDeferred<T> partitionBy(BiFunction<T, ItemStack, K> keyExtractor,
            Map<K, IStructureElement<T>> map, IStructureElement<T> defaultElem) {
        if (keyExtractor == null || map == null) {
            throw new IllegalArgumentException();
        }
        return defer(keyExtractor.andThen(key -> map.getOrDefault(key, defaultElem)));
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * Do pay place null values in the array.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     *
     * @param keyExtractor extract a key from the context object and trigger item
     * @param array        all possible structure element
     */
    @SafeVarargs
    public static <T> IStructureElementDeferred<T> defer(BiFunction<T, ItemStack, Integer> keyExtractor,
            IStructureElement<T>... array) {
        if (keyExtractor == null || array == null) {
            throw new IllegalArgumentException();
        }
        return defer(keyExtractor.andThen(i -> array[i]));
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * Do pay place null values in the array.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     *
     * @param keyExtractor extract a key from the context object and trigger item
     * @param array        all possible structure element
     */
    public static <T> IStructureElementDeferred<T> defer(BiFunction<T, ItemStack, Integer> keyExtractor,
            List<IStructureElement<T>> array) {
        return defer(keyExtractor.andThen(array::get));
    }

    /**
     * This will defer the actual instantiation of structure element until the <b>first time</b> structure code is
     * actually called.
     * <p>
     * Similar to defer, but caches the first returned element returned and won't call it again. Initialization is not
     * thread safe. This will allow the structure element <b>constructor</b> to access properties only present on the
     * context object (e.g. GT5 multiblock controller), e.g. hatch texture index to use. Use this if the data you access
     * will remain constant.
     * <p>
     * This variant will override the check function of the structure element from second function with the one returned
     * from the structure element from first function.
     *
     * @param toCheck override the check function with the returned element
     * @param to      create structure element from the context object passed in
     */
    public static <T> IStructureElementDeferred<T> defer(Function<T, IStructureElement<T>> toCheck,
            BiFunction<T, ItemStack, IStructureElement<T>> to) {
        if (to == null) {
            throw new IllegalArgumentException();
        }
        return new IStructureElementDeferred<T>() {

            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                return toCheck.apply(t).check(t, world, x, y, z);
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return to.apply(t, trigger).placeBlock(t, world, x, y, z, trigger);
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return to.apply(t, trigger).spawnHint(t, world, x, y, z, trigger);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                return to.apply(t, trigger).survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return to.apply(t, trigger).survivalPlaceBlock(t, world, x, y, z, trigger, env);
            }
        };
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns a key not
     * found in map.
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     * <p>
     * This variant will override the check function of the structure element from second function with the one returned
     * from the structure element from first function.
     *
     * @param keyExtractorCheck key extractor of the structure element that will be used for check.
     * @param keyExtractor      extract a key from the context object and trigger item
     * @param map               all possible structure element
     * @deprecated renamed to partitionBy
     */
    @Deprecated
    public static <T, K> IStructureElementDeferred<T> defer(Function<T, K> keyExtractorCheck,
            BiFunction<T, ItemStack, K> keyExtractor, Map<K, IStructureElement<T>> map) {
        return partitionBy(keyExtractorCheck, keyExtractor, map);
    }

    /**
     * This is the switch block for structure code.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns a key not
     * found in map.
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     * <p>
     * This variant will override the check function of the structure element from second function with the one returned
     * from the structure element from first function.
     *
     * @param keyExtractorCheck key extractor of the structure element that will be used for check.
     * @param keyExtractor      extract a key from the context object and trigger item
     * @param map               all possible structure element
     */
    public static <T, K> IStructureElementDeferred<T> partitionBy(Function<T, K> keyExtractorCheck,
            BiFunction<T, ItemStack, K> keyExtractor, Map<K, IStructureElement<T>> map) {
        if (keyExtractor == null || map == null) {
            throw new IllegalArgumentException();
        }
        return defer(keyExtractorCheck.andThen(map::get), keyExtractor.<IStructureElement<T>>andThen(map::get));
    }

    /**
     * This is the switch block for structure code, with default case.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. defaultElem will be used if either extractor returned a key not found in map.
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     * <p>
     * This variant will override the check function of the structure element from second function with the one returned
     * from the structure element from first function.
     *
     * @param keyExtractorCheck key extractor of the structure element that will be used for check.
     * @param keyExtractor      extract a key from the context object and trigger item
     * @param map               all possible structure element
     * @deprecated renamed to partitionBy
     */
    @Deprecated
    public static <T, K> IStructureElementDeferred<T> defer(Function<T, K> keyExtractorCheck,
            BiFunction<T, ItemStack, K> keyExtractor, Map<K, IStructureElement<T>> map,
            IStructureElement<T> defaultElem) {
        return partitionBy(keyExtractorCheck, keyExtractor, map, defaultElem);
    }

    /**
     * This is the switch block for structure code, with default case.
     * <p>
     * This allows you to extract or compute a key from context object, and lookup a map for actual structure element to
     * use. defaultElem will be used if either extractor returned a key not found in map.
     * <p>
     * Do pay attention to what properties your map has though. You need to pay attention to how they consider equality
     * and how they consider null key/values. Guava ImmutableMap is usually a good enough choice.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     * <p>
     * This variant will override the check function of the structure element from second function with the one returned
     * from the structure element from first function.
     *
     * @param keyExtractorCheck key extractor of the structure element that will be used for check.
     * @param keyExtractor      extract a key from the context object and trigger item
     * @param map               all possible structure element
     */
    public static <T, K> IStructureElementDeferred<T> partitionBy(Function<T, K> keyExtractorCheck,
            BiFunction<T, ItemStack, K> keyExtractor, Map<K, IStructureElement<T>> map,
            IStructureElement<T> defaultElem) {
        if (keyExtractor == null || map == null) {
            throw new IllegalArgumentException();
        }
        return defer(
                keyExtractorCheck.andThen(k -> map.getOrDefault(k, defaultElem)),
                keyExtractor.andThen(k -> map.getOrDefault(k, defaultElem)));
    }

    /**
     * This is the switch block for structure code, with default case.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * Do pay place null values in the array.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     * <p>
     * This variant will override the check function of the structure element from second function with the one returned
     * from the structure element from first function.
     *
     * @param keyExtractorCheck key extractor of the structure element that will be used for check.
     * @param keyExtractor      extract a key from the context object and trigger item
     * @param array             all possible structure element
     * @deprecated renamed to partitionBy
     */
    @SafeVarargs
    @Deprecated
    public static <T> IStructureElementDeferred<T> defer(Function<T, Integer> keyExtractorCheck,
            BiFunction<T, ItemStack, Integer> keyExtractor, IStructureElement<T>... array) {
        return partitionBy(keyExtractorCheck, keyExtractor, array);
    }

    /**
     * This is the switch block for structure code, with default case.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * Do pay place null values in the array.
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     * <p>
     * This variant will override the check function of the structure element from second function with the one returned
     * from the structure element from first function.
     *
     * @param keyExtractorCheck key extractor of the structure element that will be used for check.
     * @param keyExtractor      extract a key from the context object and trigger item
     * @param array             all possible structure element
     */
    @SafeVarargs
    public static <T> IStructureElementDeferred<T> partitionBy(Function<T, Integer> keyExtractorCheck,
            BiFunction<T, ItemStack, Integer> keyExtractor, IStructureElement<T>... array) {
        if (keyExtractor == null || array == null) {
            throw new IllegalArgumentException();
        }
        return defer(keyExtractorCheck.andThen(i -> array[i]), keyExtractor.andThen(i -> array[i]));
    }

    /**
     * This is the switch block for structure code, with default case.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     * <p>
     * This variant will override the check function of the structure element from second function with the one returned
     * from the structure element from first function.
     *
     * @param keyExtractorCheck key extractor of the structure element that will be used for check.
     * @param keyExtractor      extract a key from the context object and trigger item
     * @param array             all possible structure element
     * @deprecated renamed to partitionBy
     */
    @Deprecated
    public static <T> IStructureElementDeferred<T> defer(Function<T, Integer> keyExtractorCheck,
            BiFunction<T, ItemStack, Integer> keyExtractor, List<IStructureElement<T>> array) {
        return partitionBy(keyExtractorCheck, keyExtractor, array);
    }

    /**
     * This is the switch block for structure code, with default case.
     * <p>
     * This allows you to extract or compute an index from context object, and lookup an array for actual structure
     * element to use. This will usually, but not guaranteed, to throw an exception at runtime if keyExtractor returns
     * an index not within the array bound
     * <p>
     * This variant also passes the trigger item to the function, allowing it to get more info.
     * <p>
     * This variant will override the check function of the structure element from second function with the one returned
     * from the structure element from first function.
     *
     * @param keyExtractorCheck key extractor of the structure element that will be used for check.
     * @param keyExtractor      extract a key from the context object and trigger item
     * @param array             all possible structure element
     */
    @SuppressWarnings("unchecked")
    public static <T> IStructureElementDeferred<T> partitionBy(Function<T, Integer> keyExtractorCheck,
            BiFunction<T, ItemStack, Integer> keyExtractor, List<IStructureElement<T>> array) {
        return partitionBy(keyExtractorCheck, keyExtractor, array.toArray(new IStructureElement[0]));
    }

    // endregion

    // region channels

    /**
     * See channels.md in docs folder
     */
    public static <T> IStructureElement<T> withChannel(String channel, IStructureElement<T> backing) {
        return new IStructureElement<T>() {

            public boolean check(T t, Level world, int x, int y, int z) {
                return backing.check(t, world, x, y, z);
            }

            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                ItemStack newTrigger = ChannelDataAccessor.withChannel(trigger, channel);
                // We know spawnHint will only be called on client side, so the lack of player isn't quite an issue
                // we check if it's null again just in case.
                if (newTrigger == trigger && StructureLib.getCurrentPlayer() != null)
                    warnNoExplicitSubChannel(StructureLib.getCurrentPlayer());
                return backing.spawnHint(t, world, x, y, z, newTrigger);
            }

            private void warnNoExplicitSubChannel(Player currentPlayer) {
                // throttle this warning a bit.
                // I'm sure we can finish a check/autoplace round within 100 milliseconds.
                StructureLibAPI.addThrottledChat(
                        new ChatThrottleKey.NoExplicitChannel(channel),
                        currentPlayer,
                        new TranslatableComponent("structurelib.autoplace.warning.no_explicit_channel", channel),
                        (short) 100);
            }

            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                // I hope a CREATIVE player know what he is doing...
                // no warning for yah
                return backing.placeBlock(t, world, x, y, z, ChannelDataAccessor.withChannel(trigger, channel));
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return backing.getBlocksToPlace(t, world, x, y, z, trigger, env);
            }

            @Deprecated
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, ServerPlayer actor, Consumer<Component> chatter) {
                ItemStack newTrigger = ChannelDataAccessor.withChannel(trigger, channel);
                if (newTrigger == trigger)
                    // we will bypass the chatter filter here, as this is a warning that player definitively want to see
                    // instead of some false positive error messages like item not find
                    warnNoExplicitSubChannel(actor);
                return backing.survivalPlaceBlock(t, world, x, y, z, newTrigger, s, actor, chatter);
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, Level world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                ItemStack newTrigger = ChannelDataAccessor.withChannel(trigger, channel);
                if (newTrigger == trigger)
                    // we will bypass the chatter filter here, as this is a warning that player definitively want to see
                    // instead of some false positive error messages like item not find
                    warnNoExplicitSubChannel(env.getActor());
                return backing.survivalPlaceBlock(t, world, x, y, z, newTrigger, env);
            }
        };
    }
    // endregion

    /**
     * Used internally, to generate skips for structure definitions
     */
    public static <T> IStructureNavigate<T> step(int a, int b, int c) {
        return step(new Vec3i(a, b, c));
    }

    /**
     * Used internally, to generate skips for structure definitions
     */
    @SuppressWarnings("unchecked")
    public static <T> IStructureNavigate<T> step(Vec3i step) {
        if (step == null || step.getX() < 0 || step.getY() < 0 || step.getZ() < 0) {
            throw new IllegalArgumentException();
        }
        return STEP.computeIfAbsent(step, vec3 -> {
            if (vec3.getZ() > 0) {
                return stepC(vec3.getX(), vec3.getY(), vec3.getZ());
            } else if (vec3.getY() > 0) {
                return stepB(vec3.getX(), vec3.getY(), vec3.getZ());
            } else {
                return stepA(vec3.getX(), vec3.getY(), vec3.getZ());
            }
        });
    }

    private static <T> IStructureNavigate<T> stepA(int a, int b, int c) {
        return new IStructureNavigate<T>() {

            @Override
            public int getStepA() {
                return a;
            }

            @Override
            public int getStepB() {
                return b;
            }

            @Override
            public int getStepC() {
                return c;
            }
        };
    }

    private static <T> IStructureNavigate<T> stepB(int a, int b, int c) {
        return new IStructureNavigate<T>() {

            @Override
            public int getStepA() {
                return a;
            }

            @Override
            public int getStepB() {
                return b;
            }

            @Override
            public int getStepC() {
                return c;
            }

            @Override
            public boolean resetA() {
                return true;
            }
        };
    }

    private static <T> IStructureNavigate<T> stepC(int a, int b, int c) {
        return new IStructureNavigate<T>() {

            @Override
            public int getStepA() {
                return a;
            }

            @Override
            public int getStepB() {
                return b;
            }

            @Override
            public int getStepC() {
                return c;
            }

            @Override
            public boolean resetA() {
                return true;
            }

            @Override
            public boolean resetB() {
                return true;
            }
        };
    }

    /**
     * Used only to get pseudo code in structure writer...
     * <p>
     * NOTE: GT specific code got removed. TODO add a mean
     *
     * @param tileEntityClassifier return a string that denote the type of a tile entity, or null if it's nothing
     *                             special. useful if the tile entity cannot be simply distinguished via getClass.
     */
    public static String getPseudoJavaCode(Level world, ExtendedFacing extendedFacing, int basePositionX,
            int basePositionY, int basePositionZ, int basePositionA, int basePositionB, int basePositionC,
            Function<? super BlockEntity, String> tileEntityClassifier, int sizeA, int sizeB, int sizeC,
            boolean transpose) {
        List<Block> blocks = new ArrayList<>();
        Set<Class<? extends BlockEntity>> tiles = new HashSet<>();
        Set<String> specialTiles = new HashSet<>();
        iterate(
                world,
                extendedFacing,
                basePositionX,
                basePositionY,
                basePositionZ,
                basePositionA,
                basePositionB,
                basePositionC,
                sizeA,
                sizeB,
                sizeC,
                ((w, x, y, z) -> {
                    BlockEntity tileEntity = w.getBlockEntity(new BlockPos(x, y, z));
                    if (tileEntity == null) {
                        Block block = w.getBlockState(new BlockPos(x, y, z)).getBlock();
                        if (block != null && block != Blocks.AIR) {
                            blocks.add(block);
                        }
                    } else {
                        String classification = tileEntityClassifier.apply(tileEntity);
                        if (classification == null) {
                            tiles.add(tileEntity.getClass());
                        } else specialTiles.add(classification);
                    }
                }));
        Map<String, Character> map = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        {
            int i = 0;
            char c;
            builder.append("\n\nStructure:\n").append("\nBlocks:\n");
            for (Block block : blocks) {
                c = NICE_CHARS.charAt(i++);
                if (i > NICE_CHARS.length()) {
                    return "Too complicated for nice chars";
                }
                map.put(net.minecraft.core.Registry.BLOCK.getKey(block).toString(), c);
                builder.append(c).append(" -> ofBlock...(").append(net.minecraft.core.Registry.BLOCK.getKey(block)).append(", ...);\n");
            }
            builder.append("\nTiles:\n");
            for (Class<? extends BlockEntity> tile : tiles) {
                c = NICE_CHARS.charAt(i++);
                if (i > NICE_CHARS.length()) {
                    return "Too complicated for nice chars";
                }
                map.put(tile.getCanonicalName(), c);
                builder.append(c).append(" -> ofTileAdder(").append(tile).append(", ...);\n");
            }
            builder.append("\nSpecial Tiles:\n");
            for (String tile : specialTiles) {
                c = NICE_CHARS.charAt(i++);
                if (i > NICE_CHARS.length()) {
                    return "Too complicated for nice chars";
                }
                map.put(tile, c);
                builder.append(c).append(" -> ofSpecialTileAdder(").append(tile)
                        .append(", ...); // You will probably want to change it to something else\n");
            }
        }
        builder.append("\nOffsets:\n").append(basePositionA).append(' ').append(basePositionB).append(' ')
                .append(basePositionC).append('\n');
        if (transpose) {
            builder.append("\nTransposed Scan:\n").append("new String[][]{\n").append("    {\"");
            iterate(
                    world,
                    extendedFacing,
                    basePositionX,
                    basePositionY,
                    basePositionZ,
                    basePositionA,
                    basePositionB,
                    basePositionC,
                    true,
                    sizeA,
                    sizeB,
                    sizeC,
                    ((w, x, y, z) -> {
                        BlockEntity tileEntity = w.getBlockEntity(new BlockPos(x, y, z));
                        if (tileEntity == null) {
                            Block block = w.getBlockState(new BlockPos(x, y, z)).getBlock();
                            if (block != null && block != Blocks.AIR) {
                                builder.append(
                                        map.get(
                                            net.minecraft.core.Registry.BLOCK.getKey(block).toString()));
                            } else {
                                builder.append(' ');
                            }
                        } else {
                            String classification = tileEntityClassifier.apply(tileEntity);
                            if (classification == null) {
                                classification = tileEntity.getClass().getCanonicalName();
                            }
                            builder.append(map.get(classification));
                        }
                    }),
                    () -> builder.append("\",\""),
                    () -> {
                        builder.setLength(builder.length() - 2);
                        builder.append("},\n    {\"");
                    });
            builder.setLength(builder.length() - 8);
            builder.append("\n}\n\n");
        } else {
            builder.append("\nNormal Scan:\n").append("new String[][]{{\n").append("    \"");
            iterate(
                    world,
                    extendedFacing,
                    basePositionX,
                    basePositionY,
                    basePositionZ,
                    basePositionA,
                    basePositionB,
                    basePositionC,
                    false,
                    sizeA,
                    sizeB,
                    sizeC,
                    ((w, x, y, z) -> {
                        BlockEntity tileEntity = w.getBlockEntity(new BlockPos(x, y, z));
                        if (tileEntity == null) {
                            Block block = w.getBlockState(new BlockPos(x, y, z)).getBlock();
                            if (block != null && block != Blocks.AIR) {
                                builder.append(
                                        map.get(
                                            net.minecraft.core.Registry.BLOCK.getKey(block).toString()));
                            } else {
                                builder.append(' ');
                            }
                        } else {
                            String classification = tileEntityClassifier.apply(tileEntity);
                            if (classification == null) {
                                classification = tileEntity.getClass().getCanonicalName();
                            }
                            builder.append(map.get(classification));
                        }
                    }),
                    () -> builder.append("\",\n").append("    \""),
                    () -> {
                        builder.setLength(builder.length() - 7);
                        builder.append("\n").append("},{\n").append("    \"");
                    });
            builder.setLength(builder.length() - 8);
            builder.append("}\n\n");
        }
        return (builder.toString().replaceAll("\"\"", "E"));
    }

    static <T> boolean iterateV2(IStructureElement<T>[] elements, Level world, ExtendedFacing extendedFacing,
            int basePositionX, int basePositionY, int basePositionZ, int basePositionA, int basePositionB,
            int basePositionC, IStructureWalker<T> predicate, String iterateType) {
        // change base position to base offset
        basePositionA = -basePositionA;
        basePositionB = -basePositionB;
        basePositionC = -basePositionC;

        int[] abc = new int[] { basePositionA, basePositionB, basePositionC };
        int[] xyz = new int[3];

        boolean failed = false;
        for (IStructureElement<T> element : elements) {
            if (element.isNavigating()) {
                abc[0] = (element.resetA() ? basePositionA : abc[0]) + element.getStepA();
                abc[1] = (element.resetB() ? basePositionB : abc[1]) + element.getStepB();
                abc[2] = (element.resetC() ? basePositionC : abc[2]) + element.getStepC();
            } else {
                extendedFacing.getLevelOffset(abc, xyz);
                xyz[0] += basePositionX;
                xyz[1] += basePositionY;
                xyz[2] += basePositionZ;

                if (StructureLibAPI.isDebugEnabled()) StructureLib.LOGGER.info(
                        "Multi [{}, {}, {}] {} step @ {} {}",
                        basePositionX,
                        basePositionY,
                        basePositionZ,
                        iterateType,
                        Arrays.toString(xyz),
                        Arrays.toString(abc));

                if (!world.getBlockState(new BlockPos(xyz[0], xyz[1], xyz[2])).isAir()) {
                    if (StructureLibAPI.isInstrumentEnabled()) {
                        StructureElementVisitedEvent.fireEvent(
                                world,
                                xyz[0],
                                xyz[1],
                                xyz[2],
                                abc[0] - basePositionA,
                                abc[1] - basePositionB,
                                abc[2] - basePositionC,
                                element);
                    }
                    if (!predicate.visit(element, world, xyz[0], xyz[1], xyz[2], abc[0], abc[1], abc[2])) {
                        if (StructureLibAPI.isDebugEnabled()) {
                            StructureLib.LOGGER.info(
                                    "Multi [{}, {}, {}] {} stop @ {} {}",
                                    basePositionX,
                                    basePositionY,
                                    basePositionZ,
                                    iterateType,
                                    Arrays.toString(xyz),
                                    Arrays.toString(abc));
                        }
                        failed = true;
                    }
                } else {
                    if (StructureLibAPI.isDebugEnabled()) {
                        StructureLib.LOGGER.info(
                                "Multi [{}, {}, {}] {} !blockExists @ {} {}",
                                basePositionX,
                                basePositionY,
                                basePositionZ,
                                iterateType,
                                Arrays.toString(xyz),
                                Arrays.toString(abc));
                    }
                    if (!predicate.blockNotLoaded(element, world, xyz[0], xyz[1], xyz[2], abc[0], abc[1], abc[2]))
                       failed = true;
                }
                abc[0] += 1;
            }
        }
        return !failed;
    }

    public static void iterate(Level world, ExtendedFacing extendedFacing, int basePositionX, int basePositionY,
            int basePositionZ, int basePositionA, int basePositionB, int basePositionC, int sizeA, int sizeB, int sizeC,
            IBlockPosConsumer iBlockPosConsumer) {
        sizeA -= basePositionA;
        sizeB -= basePositionB;
        sizeC -= basePositionC;

        int[] abc = new int[3];
        int[] xyz = new int[3];

        for (abc[2] = -basePositionC; abc[2] < sizeC; abc[2]++) {
            for (abc[1] = -basePositionB; abc[1] < sizeB; abc[1]++) {
                for (abc[0] = -basePositionA; abc[0] < sizeA; abc[0]++) {
                    extendedFacing.getLevelOffset(abc, xyz);
                    iBlockPosConsumer
                            .consume(world, xyz[0] + basePositionX, xyz[1] + basePositionY, xyz[2] + basePositionZ);
                }
            }
        }
    }

    public static void iterate(Level world, ExtendedFacing extendedFacing, int basePositionX, int basePositionY,
            int basePositionZ, int basePositionA, int basePositionB, int basePositionC, boolean transpose, int sizeA,
            int sizeB, int sizeC, IBlockPosConsumer iBlockPosConsumer, Runnable nextB, Runnable nextC) {
        sizeA -= basePositionA;
        sizeB -= basePositionB;
        sizeC -= basePositionC;

        int[] abc = new int[3];
        int[] xyz = new int[3];
        if (transpose) {
            for (abc[1] = -basePositionB; abc[1] < sizeB; abc[1]++) {
                for (abc[2] = -basePositionC; abc[2] < sizeC; abc[2]++) {
                    for (abc[0] = -basePositionA; abc[0] < sizeA; abc[0]++) {
                        extendedFacing.getLevelOffset(abc, xyz);
                        iBlockPosConsumer
                                .consume(world, xyz[0] + basePositionX, xyz[1] + basePositionY, xyz[2] + basePositionZ);
                    }
                    nextB.run();
                }
                nextC.run();
            }
        } else {
            for (abc[2] = -basePositionC; abc[2] < sizeC; abc[2]++) {
                for (abc[1] = -basePositionB; abc[1] < sizeB; abc[1]++) {
                    for (abc[0] = -basePositionA; abc[0] < sizeA; abc[0]++) {
                        extendedFacing.getLevelOffset(abc, xyz);
                        iBlockPosConsumer
                                .consume(world, xyz[0] + basePositionX, xyz[1] + basePositionY, xyz[2] + basePositionZ);
                    }
                    nextB.run();
                }
                nextC.run();
            }
        }
    }

    /**
     * Transposes shape (swaps B and C axis, can be used to un-transpose transposed shape) WARNING! Do not use on old
     * api...
     *
     * @param structurePiece shape (transposed shape)
     * @return transposed shape (untransposed shape)
     */
    public static String[][] transpose(String[][] structurePiece) {
        String[][] shape = new String[structurePiece[0].length][structurePiece.length];
        for (int i = 0; i < structurePiece.length; i++) {
            for (int j = 0; j < structurePiece[i].length; j++) {
                shape[j][i] = structurePiece[i][j];
            }
        }
        return shape;
    }
}
