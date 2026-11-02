package com.gtnewhorizon.structurelib.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.net.AlignmentMessage.AlignmentQuery;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.item.ItemConstructableTrigger;

public class SetChannelDataMessage implements Packet<SetChannelDataMessage> {
    static final StreamCodec<RegistryFriendlyByteBuf, SetChannelDataMessage> CODEC = StreamCodec.of((b, msg) -> {
        b.writeVarInt(msg.data.size());
        for (Entry<String, Integer> e : msg.data) {
            b.writeUtf(e.getKey());
            b.writeVarInt(e.getValue());
        }
        b.writeEnum(msg.hand);
    }, b -> {
        int size = b.readVarInt();
        List<Entry<String, Integer>> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(Pair.of(b.readUtf(), b.readVarInt()));
        }
        return new SetChannelDataMessage(data, b.readEnum(InteractionHand.class));
    });
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "set_channel_data");
    public static ServerboundPacketType<SetChannelDataMessage> PACKET_TYPE = CodecPacketType.Server.create(ID, CODEC, msg -> (player) -> {
        ItemStack heldItem = player.getItemInHand(msg.hand);
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ItemConstructableTrigger) {
            ChannelDataAccessor.wipeChannelData(heldItem);
            for (Entry<String, Integer> e : msg.data) {
                ChannelDataAccessor.setChannelData(heldItem, e.getKey(), e.getValue());
            }
            // since this is a set all channel request from the client, we would assume client already know
            // what this would look like on the client, so no sync
        } else {
            StructureLib.LOGGER.warn(
                "{} trying to set channel data on {}, which is not a hologram projector!",
                player.getDisplayName(),
                heldItem);
        }
    });

    private final List<Entry<String, Integer>> data;
    private final InteractionHand hand;

    public SetChannelDataMessage(List<Entry<String, Integer>> data, InteractionHand hand) {
        this.data = data;
        this.hand = hand;
    }

    public SetChannelDataMessage(ItemStack trigger, InteractionHand hand) {
        List<Entry<String, Integer>> data = new ArrayList<>();
        ChannelDataAccessor.iterateChannelData(trigger).forEach(data::add);
        this.data = data;
        this.hand = hand;
    }

    @Override
    public PacketType<SetChannelDataMessage> type() {
        return PACKET_TYPE;
    }
}
