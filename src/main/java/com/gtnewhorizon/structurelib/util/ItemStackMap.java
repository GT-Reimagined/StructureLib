package com.gtnewhorizon.structurelib.util;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A map class for ItemStack keys with wildcard damage/NBT. Optimised for lookup.
 * <p>
 * This map does NOT support null values or null keys! null will be silently ignored!
 * <p>
 * Originally created by CodeChicken for NotEnoughItems. Adapted to {@code Map<ItemStack, T>} interface by glee8e
 * <p>
 * Edited later to add the option to ignore NBT tags to match vanilla furnace recipe map behavior.
 *
 * @author CodeChiken
 * @author glee8e, mitchej123
 */
@SuppressWarnings("unused")
public final class ItemStackMap<T> extends AbstractMap<ItemStack, T> {

    public static final CompoundTag WILDCARD_TAG;

    static {
        WILDCARD_TAG = new CompoundTag();
        WILDCARD_TAG.putBoolean("*", true);
    }

    private final HashMap<Item, DetailMap> itemMap = new HashMap<>();
    private int size;

    private final boolean NBTSensitive;

    static ItemStack newItemStack(Item item, int size, CompoundTag tag) {
        ItemStack stack = new ItemStack(item, size);
        stack.setTag(tag);
        return stack;
    }

    static ItemStack wildcard(Item item, boolean nbtsensitive) {
        return newItemStack(item, 1, nbtsensitive ? WILDCARD_TAG : null);
    }

    static boolean isWildcard(CompoundTag tag) {
        return tag != null && tag.getBoolean("*");
    }

    @SuppressWarnings("unused")
    public ItemStackMap() {
        this.NBTSensitive = false;
    }

    @SuppressWarnings("unused")
    public ItemStackMap(boolean NBTSensitive) {
        this.NBTSensitive = NBTSensitive;
    }

    @Override
    public T get(Object key) {
        if (!(key instanceof ItemStack)) return null;
        ItemStack stack = ((ItemStack) key);
        if (stack.getItem() == null) return null;
        DetailMap map = itemMap.get(stack.getItem());
        return map == null ? null : map.get(stack);
    }

    @Override
    public T put(ItemStack key, T value) {
        if (key == null || key.getItem() == null || value == null) return null;
        return itemMap.computeIfAbsent(key.getItem(), k -> new DetailMap(this.NBTSensitive)).put(key, value);
    }

    @Override
    public T remove(Object key) {
        if (!(key instanceof ItemStack)) return null;
        ItemStack stack = ((ItemStack) key);
        if (stack.getItem() == null) return null;
        DetailMap map = itemMap.get(stack.getItem());
        return map == null ? null : map.remove(stack);
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof ItemStack)) return false;
        ItemStack stack = ((ItemStack) key);
        if (stack.getItem() == null) return false;
        DetailMap map = itemMap.get(stack.getItem());
        return map != null && map.get(stack) != null;
    }

    @Override
    public T merge(ItemStack key, T value, BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
        if (key == null || key.getItem() == null || value == null || remappingFunction == null) return null;
        DetailMap map = itemMap.get(key.getItem());
        return itemMap.computeIfAbsent(key.getItem(), k -> new DetailMap(this.NBTSensitive))
                .merge(key, value, remappingFunction);
    }

    @Override
    public T computeIfAbsent(ItemStack key, Function<? super ItemStack, ? extends T> mappingFunction) {
        if (key == null || key.getItem() == null || mappingFunction == null) return null;
        DetailMap map = itemMap.get(key.getItem());
        return itemMap.computeIfAbsent(key.getItem(), k -> new DetailMap(this.NBTSensitive))
                .computeIfAbsent(key, mappingFunction);
    }

    @Override
    public void clear() {
        itemMap.clear();
        size = 0;
    }

    @Override
    public Set<Entry<ItemStack, T>> entrySet() {
        return new SetView();
    }

    private static class DetailIter<T> implements Iterator<Map.Entry<ItemStack, T>> {

        private final Item owner;
        private final ItemStackMap<T>.DetailMap backing;

        @Nullable
        private final Iterator<Map.Entry<CompoundTag, T>> tagIter;

        private DetailIterState state = DetailIterState.NOT_STARTED;
        private boolean removed = false;

        private DetailIter(Map.Entry<Item, ItemStackMap<T>.DetailMap> input) {
            this.owner = input.getKey();
            this.backing = input.getValue();
            tagIter = backing.tagMap != null ? backing.tagMap.entrySet().iterator() : null;
        }

        private DetailIterState nextState() {
            switch (state) {
                case NOT_STARTED:
                    if (backing.hasWildcard) return DetailIterState.WILDCARD;
                case WILDCARD:
                case TAG:
                    if (tagIter != null && tagIter.hasNext()) return DetailIterState.TAG;
                case DONE:
                    return DetailIterState.DONE;
                default:
                    throw new IllegalStateException("Unexpected value: " + state);
            }
        }

        @Override
        public void remove() {
            if (removed) throw new IllegalStateException("remove() called twice");
            state.remove(this);
            removed = true;
        }

        @Override
        public boolean hasNext() {
            return nextState() != DetailIterState.DONE;
        }

        @Override
        public Map.Entry<ItemStack, T> next() {
            DetailIterState nextState = nextState();
            if (nextState == DetailIterState.DONE) throw new NoSuchElementException();
            state = nextState;
            removed = false;
            return nextState.get(this);
        }

        private enum DetailIterState {

            NOT_STARTED {

                @Override
                <T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
                    throw new AssertionError("Should not call get on NOT_STARTED");
                }

                @Override
                <T> void remove(DetailIter<T> iter) {
                    throw new IllegalStateException("next() never called");
                }
            },
            WILDCARD {

                @Override
                <T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
                    return new Map.Entry<ItemStack, T>() {

                        private final ItemStack key = wildcard(iter.owner, iter.backing.NBTSensitive);

                        @Override
                        public ItemStack getKey() {
                            return key;
                        }

                        @Override
                        public T getValue() {
                            return iter.backing.wildcard;
                        }

                        @Override
                        public T setValue(T value) {
                            return iter.backing.wildcard = value;
                        }
                    };
                }

                @Override
                <T> void remove(DetailIter<T> iter) {
                    assert iter.backing.hasWildcard;
                    iter.backing.wildcard = null;
                    iter.backing.hasWildcard = false;
                }
            },
            TAG {

                @Override
                <T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
                    assert iter.tagIter != null;
                    Map.Entry<CompoundTag, T> entry = iter.tagIter.next();
                    return new ItemStackEntry<>(newItemStack(iter.owner, 1, entry.getKey()), entry);
                }

                @Override
                <T> void remove(DetailIter<T> iter) {
                    assert iter.tagIter != null;
                    iter.tagIter.remove();
                }
            },
            DONE {

                @Override
                <T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
                    throw new AssertionError("Should not call get on DONE");
                }

                @Override
                <T> void remove(DetailIter<T> iter) {
                    throw new AssertionError("Should not call remove on DONE");
                }
            };

            abstract <T> Map.Entry<ItemStack, T> get(DetailIter<T> iter);

            abstract <T> void remove(DetailIter<T> iter);

            private static class ItemStackEntry<T> implements Map.Entry<ItemStack, T> {

                private final ItemStack key;
                private final Map.Entry<?, T> entry;

                public ItemStackEntry(ItemStack key, Map.Entry<?, T> entry) {
                    this.key = key;
                    this.entry = entry;
                }

                @Override
                public ItemStack getKey() {
                    return key;
                }

                @Override
                public T getValue() {
                    return entry.getValue();
                }

                @Override
                public T setValue(T value) {
                    return entry.setValue(value);
                }
            }
        }
    }

    private class DetailMap {

        private boolean hasWildcard;
        private T wildcard;
        private HashMap<CompoundTag, T> tagMap;
        private int size;

        private final boolean NBTSensitive;

        public DetailMap(boolean NBTSensitive) {
            this.NBTSensitive = NBTSensitive;
        }

        private KeyType getKeyType(CompoundTag tag) {
            return !NBTSensitive || isWildcard(tag) ? KeyType.WildcardTag : KeyType.NotWildcard;
        }

        public T get(ItemStack key) {
            if (wildcard != null) return wildcard;
            if (tagMap != null) {
                final T ret = tagMap.get(key.getTag());
                if (ret != null) return ret;
            }

            return null;
        }

        public T put(ItemStack key, T value) {
            try {
                switch (getKeyType(key.getTag())) {
                    case NotWildcard -> {
                        if (tagMap == null) tagMap = new HashMap<>();
                        return tagMap.put(key.getTag(), value);
                    }
                    case WildcardTag -> {
                        T ret = wildcard;
                        wildcard = value;
                        hasWildcard = true;
                        return ret;
                    }
                }
            } finally {
                updateSize();
            }
            return null;
        }

        public T remove(ItemStack key) {
            try {
                switch (getKeyType(key.getTag())) {
                    case NotWildcard -> {
                        return tagMap != null ? tagMap.remove(key.getTag()) : null;
                    }
                    case WildcardTag -> {
                        T ret = wildcard;
                        wildcard = null;
                        hasWildcard = false;
                        return ret;
                    }
                }
            } finally {
                updateSize();
            }
            return null;
        }

        private void updateSize() {
            int newSize = (hasWildcard ? 1 : 0) +  (tagMap != null ? tagMap.size() : 0);

            if (newSize != size) {
                ItemStackMap.this.size += newSize - size;
                size = newSize;
            }
        }

        public T merge(ItemStack key, T value, BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
            try {
                switch (getKeyType(key.getTag())) {
                    case NotWildcard -> {
                        if (tagMap == null) tagMap = new HashMap<>();
                        return tagMap.merge(key.getTag(), value, remappingFunction);
                    }
                    case WildcardTag -> {
                        T newValue = wildcard == null ? value : remappingFunction.apply(wildcard, value);
                        wildcard = newValue;
                        hasWildcard = newValue != null;
                        return newValue;
                    }
                }
            } finally {
                updateSize();
            }
            return null;
        }

        public T computeIfAbsent(ItemStack key, Function<? super ItemStack, ? extends T> mappingFunction) {
            try {
                switch (getKeyType(key.getTag())) {
                    case NotWildcard -> {
                        if (tagMap == null) tagMap = new HashMap<>();
                        return tagMap.computeIfAbsent(key.getTag(), x -> mappingFunction.apply(key));
                    }
                    case WildcardTag -> {
                        T newValue = wildcard == null ? mappingFunction.apply(key) : wildcard;
                        wildcard = newValue;
                        hasWildcard = newValue != null;
                        return newValue;
                    }
                }
            } finally {
                updateSize();
            }
            return null;
        }
    }

    private class SetView extends AbstractSet<Map.Entry<ItemStack, T>> {

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            return java.util.Objects.equals(get(entry.getKey()), entry.getValue());
        }

        @Override
        public boolean add(Map.Entry<ItemStack, T> itemStackTEntry) {
            return itemStackTEntry.getKey() != null && itemStackTEntry.getValue() != null
                    && put(itemStackTEntry.getKey(), itemStackTEntry.getValue()) == null;
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            return ItemStackMap.this.remove(entry.getKey(), entry.getValue());
        }

        @Nonnull
        @Override
        public Iterator<Map.Entry<ItemStack, T>> iterator() {
            return Iterators.concat(Iterators.transform(itemMap.entrySet().iterator(), DetailIter::new));
        }

        @Override
        public int size() {
            return size;
        }
    }

    private enum KeyType {

        NotWildcard, // NotWildcard
        WildcardTag, // Wildcard
        ;

        private static final KeyType[] VALUES = values();
    }
}
