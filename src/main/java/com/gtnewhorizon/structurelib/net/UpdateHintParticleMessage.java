package com.gtnewhorizon.structurelib.net;

import static com.gtnewhorizon.structurelib.StructureLib.LOGGER;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;


import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class UpdateHintParticleMessage implements Packet<UpdateHintParticleMessage> {

    public static final Handler HANDLER = new Handler();
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
    public PacketHandler<UpdateHintParticleMessage> getHandler() {
        return HANDLER;
    }

    @Override
    public ResourceLocation getID() {
        return StructureLib.UPDATE_HINT_PARTICLE;
    }

    private static class Handler implements PacketHandler<UpdateHintParticleMessage> {
        @Override
        public void encode(UpdateHintParticleMessage msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
            buf.writeByte(msg.r);
            buf.writeByte(msg.g);
            buf.writeByte(msg.b);
            buf.writeByte(msg.a);
        }

        @Override
        public UpdateHintParticleMessage decode(FriendlyByteBuf buf) {
            return new UpdateHintParticleMessage(buf.readBlockPos(), buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
        }

        @Override
        public PacketContext handle(UpdateHintParticleMessage msg) {
            return ((player, level) -> {
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
        }
    }
}
