package com.gtnewhorizon.structurelib.net;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentProvider;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.util.PlatformUtils;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AlignmentMessage<T extends AlignmentMessage<T>> implements Packet<T> {

    BlockPos pos;
    String dimensionID;
    int mAlign;

    public AlignmentMessage(BlockPos pos, String dimension, int align) {

    }

    private AlignmentMessage(IAlignmentProvider provider) {
        if (!(provider instanceof BlockEntity base)) throw new IllegalArgumentException("Provider must be a BlockEntity");
        IAlignment alignment = provider.getAlignment();
        if (alignment == null) throw new IllegalArgumentException("Passed in provider does not provide an alignment!");
        pos = base.getBlockPos();
        if (base.getLevel() == null) throw new IllegalStateException("Somehow world is null!");
        dimensionID = base.getLevel().dimension().location().toString();
        mAlign = alignment.getExtendedFacing().getIndex();
    }

    private AlignmentMessage(Level world, BlockPos pos, IAlignment front) {
        this.pos = pos;
        dimensionID = world.dimension().location().toString();
        mAlign = front.getExtendedFacing().getIndex();
    }

    public static class AlignmentQuery extends AlignmentMessage<AlignmentQuery> {

        public static final ServerHandler HANDLER = new ServerHandler();

        public AlignmentQuery(BlockPos pos, String dimension, int align) {
            super(pos, dimension, align);
        }


        public AlignmentQuery(IAlignmentProvider provider) {
            super(provider);
        }

        @Override
        public ResourceLocation getID() {
            return StructureLib.ALIGNMENT_QUERY;
        }

        @Override
        public PacketHandler<AlignmentQuery> getHandler() {
            return HANDLER;
        }
    }

    public static class AlignmentData extends AlignmentMessage<AlignmentData> {
        public static final ClientHandler HANDLER = new ClientHandler();

        public AlignmentData(BlockPos pos, String dimension, int align) {
            super(pos, dimension, align);
        }

        public AlignmentData(IAlignmentProvider provider) {
            super(provider);
        }

        public AlignmentData(Level world, BlockPos pos, IAlignment front) {
            super(world, pos, front);
        }

        @Override
        public ResourceLocation getID() {
            return StructureLib.ALIGNMENT_DATA;
        }

        @Override
        public PacketHandler<AlignmentData> getHandler() {
            return HANDLER;
        }
    }

    static class ClientHandler implements PacketHandler<AlignmentData> {
        @Override
        public void encode(AlignmentData msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
            buf.writeUtf(msg.dimensionID);
            buf.writeInt(msg.mAlign);
        }

        @Override
        public AlignmentData decode(FriendlyByteBuf buf) {
            return new AlignmentData(buf.readBlockPos(), buf.readUtf(), buf.readInt());
        }

        @Override
        public PacketContext handle(AlignmentData msg) {
            return (player, level) -> {
                if (StructureLib.getCurrentPlayer().level.dimension().location().toString().equals(msg.dimensionID)) {
                    BlockEntity te = StructureLib.getCurrentPlayer().level
                        .getBlockEntity(msg.pos);
                    if (te instanceof IAlignmentProvider provider) {
                        IAlignment alignment = provider.getAlignment();
                        if (alignment != null) {
                            alignment.setExtendedFacing(ExtendedFacing.byIndex(msg.mAlign));
                        }
                    }
                }
            };
        }
    }

    static class ServerHandler implements PacketHandler<AlignmentQuery> {
        @Override
        public void encode(AlignmentQuery msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
            buf.writeUtf(msg.dimensionID);
            buf.writeInt(msg.mAlign);
        }

        @Override
        public AlignmentQuery decode(FriendlyByteBuf buf) {
            return new AlignmentQuery(buf.readBlockPos(), buf.readUtf(), buf.readInt());
        }

        @Override
        public PacketContext handle(AlignmentQuery msg) {
            return (player, level) -> {
                Level world = PlatformUtils.getCurrentServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(msg.dimensionID)));
                if (world != null) {
                    BlockEntity te = world.getBlockEntity(msg.pos);
                    if (te instanceof IAlignmentProvider provider) {
                        IAlignment alignment = provider.getAlignment();
                        if (alignment == null) return;
                        StructureLib.CHANNEL.sendToPlayer(new AlignmentData(world, msg.pos, alignment), player);
                    }
                }
            };
        }
    }
}
