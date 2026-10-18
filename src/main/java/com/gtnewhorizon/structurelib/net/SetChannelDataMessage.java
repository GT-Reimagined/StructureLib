package com.gtnewhorizon.structurelib.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.item.ItemConstructableTrigger;

public class SetChannelDataMessage implements Packet<SetChannelDataMessage> {

    public static final Handler HANDLER = new Handler();

    private final List<Map.Entry<String, Integer>> data;
    private final InteractionHand hand;

    public SetChannelDataMessage(List<Map.Entry<String, Integer>> data, InteractionHand hand) {
        this.data = data;
        this.hand = hand;
    }

    public SetChannelDataMessage(ItemStack trigger, InteractionHand hand) {
        List<Map.Entry<String, Integer>> data = new ArrayList<>();
        ChannelDataAccessor.iterateChannelData(trigger).forEach(data::add);
        this.data = data;
        this.hand = hand;
    }

    @Override
    public PacketHandler<SetChannelDataMessage> getHandler() {
        return HANDLER;
    }

    @Override
    public ResourceLocation getID() {
        return StructureLib.SET_CHANNEL_DATA;
    }

    private static class Handler implements PacketHandler<SetChannelDataMessage> {
        @Override
        public void encode(SetChannelDataMessage msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.data.size());
            for (Entry<String, Integer> e : msg.data) {
                buf.writeUtf(e.getKey());
                buf.writeVarInt(e.getValue());
            }
            buf.writeEnum(msg.hand);
        }

        @Override
        public SetChannelDataMessage decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<Map.Entry<String, Integer>> data = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                data.add(Pair.of(buf.readUtf(), buf.readVarInt()));
            }
            return new SetChannelDataMessage(data, buf.readEnum(InteractionHand.class));
        }

        @Override
        public PacketContext handle(SetChannelDataMessage msg) {
            return ((sender, level) -> {
                ItemStack heldItem = sender.getItemInHand(msg.hand);
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
                        sender.getDisplayName(),
                        heldItem);
                }
            });
        }
    }
}
