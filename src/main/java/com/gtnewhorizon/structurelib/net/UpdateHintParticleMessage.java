package com.gtnewhorizon.structurelib.net;

import static com.gtnewhorizon.structurelib.StructureLib.LOGGER;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;


import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import trinsdar.networkapi.api.IPacket;

public class UpdateHintParticleMessage implements IPacket {

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

    public static UpdateHintParticleMessage decode(FriendlyByteBuf buf) {
        return new UpdateHintParticleMessage(buf.readBlockPos(), buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(r);
        buf.writeByte(g);
        buf.writeByte(b);
        buf.writeByte(a);
    }

    @Override
    public void handleClient(ServerPlayer sender) {

    }

    @Override
    public void handleServer() {
        boolean updateResult = StructureLibAPI.updateHintParticleTint(
            StructureLib.getCurrentPlayer(),
            StructureLib.getCurrentPlayer().level,
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            new short[] { r, g, b, a, });
        if (StructureLibAPI.isDebugEnabled()) LOGGER.debug(
            "Server instructed to update hint particle at ({}, {}, {}), result {}!",
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            updateResult);
    }
}
