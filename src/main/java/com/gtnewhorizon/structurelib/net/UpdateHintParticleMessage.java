package com.gtnewhorizon.structurelib.net;

import static com.gtnewhorizon.structurelib.StructureLib.LOGGER;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;


import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class UpdateHintParticleMessage implements Packet<UpdateHintParticleMessage> {
    static final StreamCodec<RegistryFriendlyByteBuf, UpdateHintParticleMessage> CODEC = StreamCodec.of((b, msg) -> {
        b.writeBlockPos(msg.pos);
        b.writeInt((msg.r << 24) | (msg.g << 16) | (msg.b << 8) | msg.a);
    }, b -> {
        BlockPos p = b.readBlockPos();
        int rgba = b.readInt();
        return new UpdateHintParticleMessage(p, (short) ((rgba >> 24) & 0xFF), (short) ((rgba >> 16) & 0xFF), (short) ((rgba >> 8) & 0xFF), (short) ((rgba) & 0xFF));
    });

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "error_hint_particle");
    public static ClientboundPacketType<UpdateHintParticleMessage> PACKET_TYPE = CodecPacketType.Client.create(ID, CODEC, msg -> () -> {
        boolean updateResult = StructureLibAPI.updateHintParticleTint(
            StructureLib.getCurrentPlayer(),
            StructureLib.getCurrentPlayer().level(),
            msg.pos.getX(),
            msg.pos.getY(),
            msg.pos.getZ(),
            new short[] { msg.r, msg.g, msg.b, msg.a, });
        if (StructureLibAPI.isDebugEnabled()) LOGGER.debug(
            "Server instructed to update hint particle at ({}, {}, {}), result {}!",
            msg.pos.getX(),
            msg.pos.getY(),
            msg.pos.getZ(),
            updateResult);
    });

    private BlockPos pos;
    private short r;
    private short g;
    private short b;
    private short a;

    public UpdateHintParticleMessage() {}

    public UpdateHintParticleMessage(BlockPos pos, short r, short g, short b, short a) {
        this.pos = pos;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public PacketType<UpdateHintParticleMessage> type() {
        return PACKET_TYPE;
    }
}
