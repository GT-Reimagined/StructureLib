package com.gtnewhorizon.structurelib.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.item.ItemConstructableTrigger;


import trinsdar.networkapi.api.IPacket;

public class SetChannelDataMessage implements IPacket {

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

    public static SetChannelDataMessage decode(FriendlyByteBuf buf) {

        int size = buf.readVarInt();
        List<Map.Entry<String, Integer>> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(Pair.of(buf.readUtf(), buf.readVarInt()));
        }
        return new SetChannelDataMessage(data, buf.readEnum(InteractionHand.class));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(data.size());
        for (Entry<String, Integer> e : data) {
            buf.writeUtf(e.getKey());
            buf.writeVarInt(e.getValue());
        }
        buf.writeEnum(hand);
    }

    @Override
    public void handleClient(ServerPlayer sender) {
        ItemStack heldItem = sender.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ItemConstructableTrigger) {
            ChannelDataAccessor.wipeChannelData(heldItem);
            for (Entry<String, Integer> e : data) {
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
    }

    @Override
    public void handleServer() {
    }
}
