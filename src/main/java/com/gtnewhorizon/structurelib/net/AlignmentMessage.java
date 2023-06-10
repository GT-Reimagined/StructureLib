package com.gtnewhorizon.structurelib.net;

import com.gtnewhorizon.structurelib.StructureLibAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentProvider;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.checkerframework.checker.units.qual.A;
import trinsdar.networkapi.api.INetwork;
import trinsdar.networkapi.api.IPacket;

public abstract class AlignmentMessage implements IPacket {

    BlockPos pos;
    String dimensionID;
    int mAlign;

    public AlignmentMessage(BlockPos pos, String dimension, int align) {

    }


    public void encode(FriendlyByteBuf pBuffer) {
        pBuffer.writeBlockPos(pos);
        pBuffer.writeUtf(dimensionID);
        pBuffer.writeInt(mAlign);
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

    @Override
    public void handleClient(ServerPlayer sender) {

    }

    @Override
    public void handleServer() {

    }

    public static class AlignmentQuery extends AlignmentMessage {

        public AlignmentQuery(BlockPos pos, String dimension, int align) {
            super(pos, dimension, align);
        }


        public static AlignmentQuery decode(FriendlyByteBuf buf) {
            return new AlignmentQuery(buf.readBlockPos(), buf.readUtf(), buf.readInt());
        }

        public AlignmentQuery(IAlignmentProvider provider) {
            super(provider);
        }

        public AlignmentQuery(Level world, BlockPos pos, IAlignment front) {
            super(world, pos, front);
        }

        @Override
        public void handleClient(ServerPlayer sender) {
            Level world = INetwork.getInstance().getCurrentServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimensionID)));
            if (world != null) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof IAlignmentProvider provider) {
                    IAlignment alignment = provider.getAlignment();
                    if (alignment == null) return;
                    INetwork.getInstance().sendToClient(StructureLib.ALIGNMENT_DATA, new AlignmentData(world, pos, alignment), sender);
                }
            }
        }
    }

    public static class AlignmentData extends AlignmentMessage {

        public AlignmentData(BlockPos pos, String dimension, int align) {
            super(pos, dimension, align);
        }

        public static AlignmentData decode(FriendlyByteBuf buf) {
            return new AlignmentData(buf.readBlockPos(), buf.readUtf(), buf.readInt());
        }

        public AlignmentData(IAlignmentProvider provider) {
            super(provider);
        }

        public AlignmentData(Level world, BlockPos pos, IAlignment front) {
            super(world, pos, front);
        }

        @Override
        public void handleServer() {
            if (StructureLib.getCurrentPlayer().level.dimension().location().toString().equals(dimensionID)) {
                BlockEntity te = StructureLib.getCurrentPlayer().level
                    .getBlockEntity(pos);
                if (te instanceof IAlignmentProvider provider) {
                    IAlignment alignment = provider.getAlignment();
                    if (alignment != null) {
                        alignment.setExtendedFacing(ExtendedFacing.byIndex(mAlign));
                    }
                }
            }
        }
    }
}
