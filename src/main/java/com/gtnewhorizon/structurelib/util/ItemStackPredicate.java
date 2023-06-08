package com.gtnewhorizon.structurelib.util;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ItemStackPredicate implements Predicate<ItemStack> {

    public static ItemStackPredicate from(Item item) {
        return new ItemStackPredicate(item);
    }

    public static ItemStackPredicate from(ItemStack itemStack) {
        return new ItemStackPredicate(itemStack.getItem());
    }

    public static ItemStackPredicate from(ItemStack itemStack, NBTMode mode) {
        return new ItemStackPredicate(itemStack.getItem())
                .setTag(mode, itemStack.getTag());
    }

    public static ItemStackPredicate from(Block block) {
        return new ItemStackPredicate(block.asItem());
    }

    private final Item item;

    private CompoundTag tag;
    private NBTMode mode = NBTMode.IGNORE;

    private ItemStackPredicate(Item item) {
        this.item = item;
    }

    public ItemStackPredicate setTag(NBTMode mode, CompoundTag tag) {
        this.mode = mode;
        this.tag = tag;
        return this;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        if (item != null) if (itemStack.getItem() != item) return false;
        return mode.test(tag, itemStack.getTag());
    }

    public enum NBTMode implements BiPredicate<CompoundTag, CompoundTag> {

        IGNORE {

            @Override
            public boolean test(CompoundTag lhs, CompoundTag rhs) {
                return true;
            }
        },
        IN {

            @Override
            public boolean test(CompoundTag lhs, CompoundTag rhs) {
                if (lhs == null || lhs.isEmpty()) return true;
                if (rhs == null || rhs.isEmpty()) return false;
                for (String key : MiscUtils.getTagKeys(lhs)) {
                    if (!rhs.contains(key, lhs.getTagType(key))) return false;
                    Tag tag = lhs.get(key);
                    if (tag instanceof CompoundTag compoundTag) {
                        if (!test(compoundTag, rhs.getCompound(key))) return false;
                    } else {
                        if (!tag.equals(rhs.get(key))) return false;
                    }
                }
                return true;
            }
        },
        EXACT {

            @Override
            public boolean test(CompoundTag lhs, CompoundTag rhs) {
                if (lhs != null && lhs.isEmpty()) lhs = null;
                if (rhs != null && rhs.isEmpty()) rhs = null;
                return Objects.equals(lhs, rhs);
            }
        },
        IGNORE_KNOWN_INSIGNIFICANT_TAGS {

            @Override
            public boolean test(CompoundTag lhs, CompoundTag rhs) {
                // fast path for empty tags
                if (rhs == null || rhs.isEmpty()) return lhs == null || lhs.isEmpty();
                // TODO make an implementation without copying a potentially huge tag
                rhs = (CompoundTag) rhs.copy();
                for (String s : KNOWN_INSIGNIFICANT_TAGS) rhs.remove(s);
                return EXACT.test(lhs, rhs);
            }
        };

        private static final String[] KNOWN_INSIGNIFICANT_TAGS = { "display", // TODO expand/refine this
        };
    }
}
