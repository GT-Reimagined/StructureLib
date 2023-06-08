package com.gtnewhorizon.structurelib.alignment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.FakePlayer;

public class AlignmentUtility {

    private AlignmentUtility() {}

    public static boolean handle(Player aPlayer, Level aLevel, int aX, int aY, int aZ) {
        BlockEntity tBlockEntity = aLevel.getBlockEntity(new BlockPos(aX, aY, aZ));
        if (tBlockEntity == null || aPlayer instanceof FakePlayer) {
            return aPlayer instanceof ServerPlayer;
        }
        if (aPlayer instanceof ServerPlayer && tBlockEntity instanceof IAlignmentProvider provider) {
            IAlignment alignment = provider.getAlignment();
            if (alignment != null) {
                if (aPlayer.isShiftKeyDown()) {
                    alignment.toolSetFlip(null);
                } else {
                    alignment.toolSetRotation(null);
                }
                return true;
            }
        }
        return false;
    }
}
