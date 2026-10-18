package com.gtnewhorizon.structurelib.alignment.constructable;

import static com.gtnewhorizon.structurelib.util.MiscUtils.getTagKeys;
import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static net.minecraft.nbt.Tag.TAG_INT;

import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Stream;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.gtnewhorizon.structurelib.StructureLibAPI;

/**
 * Provide accessor methods for channel data from trigger item.
 *
 * <a href="{@docRoot}/overview-summary.html#channels">Channels section on overview</a>
 */
public class ChannelDataAccessor {

    private static final String SECONDARY_HINT_TAG = "channels";

    private ChannelDataAccessor() {}

    /**
     * Return a trigger item that
     * <ul>
     * <li>Still carry all the subchannel in some unspecified ways</li>
     * <li>Have its master channel data replaced with specified subchannel's data</li>
     * </ul>
     *
     * If given subchannel does not exist, masterStack may or may not be returned to save performance. Code within
     * StructureLib can depend on this behavior, you cannot.
     *
     * @param masterStack trigger item. can be the return value of another {@link #withChannel(ItemStack, String)} call
     * @param channel     subchannel to use. will use current master channel data instead if this channel does not exist
     *                    Note: all channel identifiers are supposed to be lower case and not empty.
     * @return a trigger item with new master channel
     */
    public static ItemStack withChannel(ItemStack masterStack, String channel) {
        if (StringUtils.isEmpty(channel) || masterStack == null) throw new IllegalArgumentException();
        if (StructureLibAPI.isDebugEnabled() && !channel.toLowerCase(Locale.ROOT).equals(channel))
            throw new IllegalArgumentException("Channel name can be lower case ONLY");
        if (!masterStack.hasTag() || !masterStack.getTag().contains(SECONDARY_HINT_TAG, TAG_COMPOUND)
                || !masterStack.getTag().getCompound(SECONDARY_HINT_TAG).contains(channel, TAG_INT))
            return masterStack;
        ItemStack ret = new ItemStack(
                masterStack.getItem(),
                masterStack.getTag().getCompound(SECONDARY_HINT_TAG).getInt(channel));
        ret.setTag(masterStack.getTag());
        return ret;
    }

    /**
     * Check if given trigger item contains any subchannel
     *
     * @param masterStack trigger stack to check
     * @return true if contains any subchannel
     */
    public static boolean hasSubChannel(ItemStack masterStack) {
        if (masterStack.isEmpty()) throw new IllegalArgumentException();
        return masterStack.hasTag() && masterStack.getTag().contains(SECONDARY_HINT_TAG, TAG_COMPOUND);
    }

    /**
     * Check if given trigger item contains specified subchannel
     *
     * @param masterStack trigger stack to check
     * @param channel     channel identifier. Note: all channel identifiers are supposed to be lower case and not empty.
     * @return true if contains specified subchannel
     */
    public static boolean hasSubChannel(ItemStack masterStack, String channel) {
        if (StringUtils.isEmpty(channel) || masterStack.isEmpty()) throw new IllegalArgumentException();
        if (StructureLibAPI.isDebugEnabled() && !channel.toLowerCase(Locale.ROOT).equals(channel))
            throw new IllegalArgumentException("Channel name can be lower case ONLY");
        return !channel.isEmpty() && masterStack.hasTag()
                && masterStack.getTag().contains(SECONDARY_HINT_TAG, TAG_COMPOUND)
                && masterStack.getTag().getCompound(SECONDARY_HINT_TAG).contains(channel, TAG_INT);
    }

    /**
     * Get the subchannel data from given trigger item. Will use master channel instead if not present.
     *
     * @param masterStack trigger stack to query from
     * @param channel     channel identifier. Note: all channel identifiers are supposed to be lower case and not empty.
     * @return channel data
     */
    public static int getChannelData(ItemStack masterStack, String channel) {
        if (StringUtils.isEmpty(channel) || masterStack.isEmpty()) throw new IllegalArgumentException();
        if (StructureLibAPI.isDebugEnabled() && !channel.toLowerCase(Locale.ROOT).equals(channel))
            throw new IllegalArgumentException("Channel name can be lower case ONLY");
        if (!masterStack.hasTag() || !masterStack.getTag().contains(SECONDARY_HINT_TAG, TAG_COMPOUND)
                || !masterStack.getTag().getCompound(SECONDARY_HINT_TAG).contains(channel, TAG_INT))
            return masterStack.getCount();
        return masterStack.getTag().getCompound(SECONDARY_HINT_TAG).getInt(channel);
    }

    /**
     * Set the subchannel data on given trigger item
     *
     * @param masterStack trigger stack to check
     * @param channel     channel identifier. Note: all channel identifiers are supposed to be lower case and not empty.
     * @param data        subchannel data. should always be a positive value
     */
    public static void setChannelData(ItemStack masterStack, String channel, int data) {
        if (StringUtils.isEmpty(channel) || masterStack == null) throw new IllegalArgumentException();
        if (StructureLibAPI.isDebugEnabled() && !channel.toLowerCase(Locale.ROOT).equals(channel))
            throw new IllegalArgumentException("Channel name can be lower case ONLY");
        if (data <= 0) throw new IllegalArgumentException();
        if (masterStack.getTag() == null) masterStack.setTag(new CompoundTag());
        CompoundTag main = masterStack.getTag();
        if (!main.contains(SECONDARY_HINT_TAG, TAG_COMPOUND)) main.put(SECONDARY_HINT_TAG, new CompoundTag());
        main.getCompound(SECONDARY_HINT_TAG).putInt(channel, data);
    }

    /**
     * Clear the given subchannel from given trigger item, if it exists
     *
     * @param masterStack trigger stack to unset
     * @param channel     channel identifier. Note: all channel identifiers are supposed to be lower case and not empty.
     */
    public static void unsetChannelData(ItemStack masterStack, String channel) {
        if (StringUtils.isEmpty(channel) || masterStack.isEmpty()) throw new IllegalArgumentException();
        if (StructureLibAPI.isDebugEnabled() && !channel.toLowerCase(Locale.ROOT).equals(channel))
            throw new IllegalArgumentException("Channel name can be lower case ONLY");
        if (masterStack.getTag() == null) masterStack.setTag(new CompoundTag());
        CompoundTag main = masterStack.getTag();
        if (!main.contains(SECONDARY_HINT_TAG, TAG_COMPOUND)) main.put(SECONDARY_HINT_TAG, new CompoundTag());
        CompoundTag tag = main.getCompound(SECONDARY_HINT_TAG);
        tag.remove(channel);
        if (tag.isEmpty()) main.remove(SECONDARY_HINT_TAG);
        if (main.isEmpty()) masterStack.setTag(null);
    }

    /**
     * Wipe all subchannel data on given trigger item
     *
     * @param masterStack trigger stack to wipe
     */
    public static void wipeChannelData(ItemStack masterStack) {
        if (masterStack.isEmpty()) throw new IllegalArgumentException();
        if (masterStack.getTag() != null) masterStack.getTag().remove(SECONDARY_HINT_TAG);
    }

    /**
     * Iterate over all subchannel data on trigger stack. Does not include master channel!!
     *
     * @param masterStack trigger stack to check
     * @return A java8 stream of pairs. Key is channel identifier and value is channel data. Pairs do not support
     *         mutation nor removal. Can still cause {@link java.util.ConcurrentModificationException} if underlying
     *         channel data is modified while stream is working.
     */
    public static Stream<Entry<String, Integer>> iterateChannelData(ItemStack masterStack) {
        if (!hasSubChannel(masterStack)) return Stream.empty();
        CompoundTag tag = masterStack.getTag().getCompound(SECONDARY_HINT_TAG);
        return getTagKeys(tag).stream().map(s -> new ImmutablePair<>(s, tag.getInt(s)));
    }

    /**
     * Get the number of subchannels present on given trigger item. Does not include master channel.
     *
     * @param masterStack trigger stack to query from
     * @return subchannel count
     */
    public static int countChannelData(ItemStack masterStack) {
        if (!hasSubChannel(masterStack)) return 0;
        CompoundTag tag = masterStack.getTag().getCompound(SECONDARY_HINT_TAG);
        return tag.getAllKeys().size();
    }
}
