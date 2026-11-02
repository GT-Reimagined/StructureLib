package com.gtnewhorizon.structurelib.net;

import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.net.AlignmentMessage.AlignmentData;
import com.gtnewhorizon.structurelib.net.AlignmentMessage.AlignmentQuery;
import com.teamresourceful.resourcefullib.common.network.Network;
import net.minecraft.resources.ResourceLocation;

public class StructureLibNetworking {

    public static final Network CHANNEL = new Network(ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "main"), 0,false);

    public static void init(){
        CHANNEL.register(AlignmentQuery.PACKET_TYPE);
        CHANNEL.register(AlignmentData.PACKET_TYPE);
        CHANNEL.register(ErrorHintParticleMessage.PACKET_TYPE);
        CHANNEL.register(UpdateHintParticleMessage.PACKET_TYPE);
        CHANNEL.register(SetChannelDataMessage.PACKET_TYPE);
    }
}
