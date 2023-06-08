package com.gtnewhorizon.structurelib.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class InventoryIterable<Inv extends Inventory> implements Iterable<ItemStack> {

    private final Inv inv;
    private final int maxSlot;

    public InventoryIterable(Inv inv) {
        this(inv, -1);
    }

    InventoryIterable(Inv inv, int maxSlot) {
        this.inv = inv;
        this.maxSlot = maxSlot;
    }

    public Inv getInventory() {
        return inv;
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return new Iterator<ItemStack>() {

            private int ptr = 0;

            @Override
            public boolean hasNext() {
                return ptr < inv.getContainerSize() && (maxSlot == -1 || ptr < maxSlot);
            }

            @Override
            public ItemStack next() {
                if (!hasNext()) throw new NoSuchElementException();
                return inv.getItem(ptr++);
            }

            @Override
            public void remove() {
                inv.setItem(ptr - 1, ItemStack.EMPTY);
            }
        };
    }
}
