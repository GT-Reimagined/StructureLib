package com.gtnewhorizon.structurelib.alignment.constructable;

import java.util.WeakHashMap;


import com.gtnewhorizon.structurelib.StructureLibConfig;
import com.gtnewhorizon.structurelib.util.PlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IItemSource;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;

public class ConstructableUtility {

    private static final WeakHashMap<ServerPlayer, Long> lastUse = new WeakHashMap<>();
    private static long clientSideLastUse = 0;

    private ConstructableUtility() {}

    public static boolean handle(ItemStack aStack, Player aPlayer, Level aLevel, int aX, int aY, int aZ,
                                 int aSide) {
        StructureLibAPI.startHinting(aLevel);
        boolean ret = handle0(aStack, aPlayer, aLevel, aX, aY, aZ, aSide);
        StructureLibAPI.endHinting(aLevel);
        return ret;
    }

    private static boolean handle0(ItemStack aStack, Player aPlayer, Level aLevel, int aX, int aY, int aZ,
            int aSide) {
        BlockEntity tBlockEntity = aLevel.getBlockEntity(new BlockPos(aX, aY, aZ));
        if (tBlockEntity == null || PlatformUtils.INSTANCE.isFakePlayer(aPlayer)) {
            return aPlayer instanceof ServerPlayer;
        }
        if (aPlayer instanceof ServerPlayer) {
            // not sneaking - client side will generate hologram. nothing to do on server side
            if (!aPlayer.isShiftKeyDown()) return true;

            long timePast = System.currentTimeMillis() - getLastUseMilis(aPlayer);
            if (timePast < StructureLibConfig.COMMON.AUTO_PLACE_INTERVAL) {
                aPlayer.sendMessage(
                        new TranslatableComponent(
                                "item.structurelib.constructableTrigger.too_fast",
                                StructureLibConfig.COMMON.AUTO_PLACE_INTERVAL - timePast), aPlayer.getUUID());
                return true;
            }
        } else if (!StructureLib.isCurrentPlayer(aPlayer)) {
            return false;
        }

        IConstructable constructable = null;
        if (tBlockEntity instanceof IConstructableProvider)
            constructable = ((IConstructableProvider) tBlockEntity).getConstructable();
        else if (tBlockEntity instanceof IConstructable) {
            constructable = (IConstructable) tBlockEntity;
        } else if (IMultiblockInfoContainer.contains(tBlockEntity.getClass())) {
            ExtendedFacing facing = tBlockEntity instanceof IAlignment ? ((IAlignment) tBlockEntity).getExtendedFacing()
                    : ExtendedFacing.of(Direction.from3DDataValue(aSide));
            constructable = IMultiblockInfoContainer.<BlockEntity>get(tBlockEntity.getClass())
                    .toConstructable(tBlockEntity, facing);
        }

        if (constructable == null) return false;

        if (aPlayer instanceof ServerPlayer) {
            ServerPlayer playerMP = (ServerPlayer) aPlayer;
            // server side and sneaking (already checked above)
            // do construct
            if (aPlayer.isCreative()) {
                constructable.construct(aStack, false);
            } else if (constructable instanceof ISurvivalConstructable) {
                int built = ((ISurvivalConstructable) constructable).survivalConstruct(
                        aStack,
                    StructureLibConfig.COMMON.AUTO_PLACE_BUDGET,
                        ISurvivalBuildEnvironment.create(IItemSource.fromPlayer(playerMP), playerMP));
                if (built > 0) {
                    playerMP.sendMessage(new TranslatableComponent("structurelib.autoplace.built_stat", built), playerMP.getUUID());
                } else if (built == -1) {
                    playerMP.sendMessage(new TranslatableComponent("structurelib.autoplace.complete"), playerMP.getUUID());
                }
                setLastUseMilis(aPlayer);
            } else {
                playerMP.sendMessage(new TranslatableComponent("structurelib.autoplace.error.not_enabled"), playerMP.getUUID());
            }
            return true;
        }
        // client side
        // particles and text
        constructable.construct(aStack, true);
        if (System.currentTimeMillis() - getLastUseMilis(aPlayer) >= 300)
            StructureLib.addClientSideChatMessages(constructable.getStructureDescription(aStack));
        setLastUseMilis(aPlayer);
        return false;
    }

    private static void setLastUseMilis(Player aPlayer) {
        if (!(aPlayer instanceof ServerPlayer))
            // assume client side
            clientSideLastUse = System.currentTimeMillis();
        else lastUse.put((ServerPlayer) aPlayer, System.currentTimeMillis());
    }

    private static long getLastUseMilis(Player aPlayer) {
        if (!(aPlayer instanceof ServerPlayer))
            // assume client side
            return clientSideLastUse;
        return lastUse.getOrDefault(aPlayer, 0L);
    }
}
