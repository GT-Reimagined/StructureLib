package com.gtnewhorizon.structurelib.net;

import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.net.AlignmentMessage.AlignmentData;
import com.gtnewhorizon.structurelib.net.AlignmentMessage.AlignmentQuery;
import com.teamresourceful.resourcefullib.common.network.base.Networking;
import com.teamresourceful.resourcefullib.common.network.neoforge.NetworkImpl;
import net.minecraft.resources.ResourceLocation;

public class StructureLibNetworking {

    public static final Networking CHANNEL = NetworkImpl.getNetwork(ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "main"), 0,false);

    public static void init(){
        CHANNEL.register(AlignmentQuery.PACKET_TYPE);
        CHANNEL.register(AlignmentData.PACKET_TYPE);
        CHANNEL.register(ErrorHintParticleMessage.PACKET_TYPE);
        CHANNEL.register(UpdateHintParticleMessage.PACKET_TYPE);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, SET_CHANNEL_DATA, SetChannelDataMessage.HANDLER, SetChannelDataMessage.class);
    }
}
