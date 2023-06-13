package com.gtnewhorizon.structurelib.net;

import static com.gtnewhorizon.structurelib.StructureLib.LOGGER;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;


import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import trinsdar.networkapi.api.IPacket;

public class ErrorHintParticleMessage implements IPacket {

    private BlockPos pos;

    public ErrorHintParticleMessage(BlockPos pos) {
        this.pos = pos;
    }

    public static ErrorHintParticleMessage decode(FriendlyByteBuf buf) {
        return new ErrorHintParticleMessage(buf.readBlockPos());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void handleClient(ServerPlayer sender) {

    }

    @Override
    public void handleServer() {
        boolean updateResult = StructureLibAPI.markHintParticleError(
            StructureLib.getCurrentPlayer(),
            StructureLib.getCurrentPlayer().level,
            pos.getX(),
            pos.getY(),
            pos.getZ());
        if (StructureLibAPI.isDebugEnabled()) LOGGER.debug(
            "Server instructed to mark hint particle at ({}, {}, {}) error, result {}!",
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            updateResult);
    }

}
