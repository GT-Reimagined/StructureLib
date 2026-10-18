package com.gtnewhorizon.structurelib.net;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static com.gtnewhorizon.structurelib.StructureLib.LOGGER;

public class ErrorHintParticleMessage implements Packet<ErrorHintParticleMessage> {
    public static final Handler HANDLER = new Handler();

    private BlockPos pos;

    public ErrorHintParticleMessage(BlockPos pos) {
        this.pos = pos;
    }


    @Override
    public ResourceLocation getID() {
        return StructureLib.ERROR_HINT_PARTICLE;
    }

    @Override
    public PacketHandler<ErrorHintParticleMessage> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<ErrorHintParticleMessage> {
        @Override
        public void encode(ErrorHintParticleMessage msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
        }

        @Override
        public ErrorHintParticleMessage decode(FriendlyByteBuf buf) {
            return new ErrorHintParticleMessage(buf.readBlockPos());
        }

        @Override
        public PacketContext handle(ErrorHintParticleMessage msg) {
            return (player, level) -> {
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
            };
        }
    }

}
