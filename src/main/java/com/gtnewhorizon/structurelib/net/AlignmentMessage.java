package com.gtnewhorizon.structurelib.net;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentProvider;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public abstract class AlignmentMessage<T extends AlignmentMessage<T>> implements Packet<T> {

    BlockPos pos;
    String dimensionID;
    int mAlign;

    public AlignmentMessage(BlockPos pos, String dimension, int align) {
        this.pos = pos;
        this.dimensionID = dimension;
        this.mAlign = align;
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

    private static <T extends AlignmentMessage<T>> void encode(T data, RegistryFriendlyByteBuf buf){
        buf.writeBlockPos(data.pos);
        buf.writeUtf(data.dimensionID);
        buf.writeInt(data.mAlign);
    }

    public static class AlignmentQuery extends AlignmentMessage<AlignmentQuery> {

        static StreamCodec<RegistryFriendlyByteBuf, AlignmentQuery> CODEC = StreamCodec.ofMember(AlignmentMessage::encode, b -> new AlignmentQuery(b.readBlockPos(), b.readUtf(), b.readInt()));
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "alignment_query");
        public static ServerboundPacketType<AlignmentQuery> PACKET_TYPE = CodecPacketType.Server.create(ID, CODEC, msg -> (player) -> {
            Level world = ServerLifecycleHooks.getCurrentServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(msg.dimensionID)));
            if (world != null && player instanceof ServerPlayer serverPlayer) {
                BlockEntity te = world.getBlockEntity(msg.pos);
                if (te instanceof IAlignmentProvider provider) {
                    IAlignment alignment = provider.getAlignment();
                    if (alignment == null) return;
                    StructureLibNetworking.CHANNEL.sendToPlayer(new AlignmentData(world, msg.pos, alignment), serverPlayer);
                }
            }
        });

        public AlignmentQuery(BlockPos pos, String dimension, int align) {
            super(pos, dimension, align);
        }

        public AlignmentQuery(IAlignmentProvider provider) {
            super(provider);
        }

        @Override
        public PacketType<AlignmentQuery> type() {
            return PACKET_TYPE;
        }
    }

    public static class AlignmentData extends AlignmentMessage<AlignmentData> {
        static StreamCodec<RegistryFriendlyByteBuf, AlignmentData> CODEC = StreamCodec.ofMember(AlignmentMessage::encode, b -> new AlignmentData(b.readBlockPos(), b.readUtf(), b.readInt()));
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "alignment_data");
        public static ClientboundPacketType<AlignmentData> PACKET_TYPE = CodecPacketType.Client.create(ID, CODEC, msg -> () -> {
            if (StructureLib.getCurrentPlayer().level().dimension().location().toString().equals(msg.dimensionID)) {
                BlockEntity te = StructureLib.getCurrentPlayer().level()
                    .getBlockEntity(msg.pos);
                if (te instanceof IAlignmentProvider provider) {
                    IAlignment alignment = provider.getAlignment();
                    if (alignment != null) {
                        alignment.setExtendedFacing(ExtendedFacing.byIndex(msg.mAlign));
                    }
                }
            }
        });

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
        public PacketType<AlignmentData> type() {
            return PACKET_TYPE;
        }
    }
}
