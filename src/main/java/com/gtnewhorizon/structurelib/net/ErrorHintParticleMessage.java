package com.gtnewhorizon.structurelib.net;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.net.AlignmentMessage.AlignmentData;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import static com.gtnewhorizon.structurelib.StructureLib.LOGGER;

public class ErrorHintParticleMessage implements Packet<ErrorHintParticleMessage> {
    static final StreamCodec<RegistryFriendlyByteBuf, ErrorHintParticleMessage> CODEC = StreamCodec.of((b, msg) -> {
        b.writeBlockPos(msg.pos);
    }, b -> {
        return new ErrorHintParticleMessage(b.readBlockPos());
    });

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "error_hint_particle");
    public static ClientboundPacketType<ErrorHintParticleMessage> PACKET_TYPE = CodecPacketType.Client.create(ID, CODEC, msg -> () -> {
        boolean updateResult = StructureLibAPI.markHintParticleError(
            StructureLib.getCurrentPlayer(),
            StructureLib.getCurrentPlayer().level(),
            msg.pos.getX(),
            msg.pos.getY(),
            msg.pos.getZ());
        if (StructureLibAPI.isDebugEnabled()) LOGGER.debug(
            "Server instructed to mark hint particle at ({}, {}, {}) error, result {}!",
            msg.pos.getX(),
            msg.pos.getY(),
            msg.pos.getZ(),
            updateResult);
    });
    private final BlockPos pos;

    public ErrorHintParticleMessage(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public PacketType<ErrorHintParticleMessage> type() {
        return PACKET_TYPE;
    }
}
