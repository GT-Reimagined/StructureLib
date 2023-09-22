package com.gtnewhorizon.structurelib;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.gtnewhorizon.structurelib.util.PlatformUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import com.gtnewhorizon.structurelib.net.ErrorHintParticleMessage;
import com.gtnewhorizon.structurelib.net.UpdateHintParticleMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CommonProxy {

    @Environment(EnvType.CLIENT)
    public void hintParticleTinted(Level w, int x, int y, int z, TextureAtlasSprite[] icons, short[] RGBa) {}

    public void hintParticleTinted(Level w, int x, int y, int z, Block block, short[] RGBa) {}

    @Environment(EnvType.CLIENT)
    public void hintParticle(Level w, int x, int y, int z, TextureAtlasSprite[] icons) {}

    public void hintParticle(Level w, int x, int y, int z, Block block) {}

    public boolean updateHintParticleTint(Player player, Level w, int x, int y, int z, short[] rgBa) {
        if (player instanceof ServerPlayer serverPlayer) { // just in case
            StructureLib.CHANNEL.sendToPlayer(new UpdateHintParticleMessage(new BlockPos(x, y, z), rgBa[0], rgBa[1], rgBa[2], rgBa[3]),
                    serverPlayer);
            return true;
        } else {
            return false;
        }
    }

    public Player getCurrentPlayer() {
        return null;
    }

    public boolean isCurrentPlayer(Player player) {
        return false;
    }

    public void addClientSideChatMessages(String... messages) {}

    public void startHinting(Level w) {}

    public void endHinting(Level w) {}

    public void preInit() {}

    public long getOverworldTime() {
        return PlatformUtils.getCurrentServer().overworld().getGameTime();
    }

    public void uploadChannels(ItemStack trigger, InteractionHand hand) {}

    public boolean markHintParticleError(Player player, Level w, int x, int y, int z) {
        if (player instanceof ServerPlayer serverPlayer) { // just in case
            StructureLib.CHANNEL.sendToPlayer(new ErrorHintParticleMessage(new BlockPos(x, y, z)), serverPlayer);
            return true;
        } else {
            return false;
        }
    }

    private final Map<ServerPlayer, Map<Object, Long>> throttleMap = new WeakHashMap<>();

    public void addThrottledChat(Object throttleKey, Player player, Component text, short intervalRequired,
            boolean forceUpdateLastSend) {
        if (player instanceof ServerPlayer) {
            Map<Object, Long> submap = throttleMap.computeIfAbsent((ServerPlayer) player, p -> new HashMap<>());
            addThrottledChat(throttleKey, player, text, intervalRequired, forceUpdateLastSend, submap);
        }
    }

    protected static void addThrottledChat(Object throttleKey, Player player, Component text,
            short intervalRequired, boolean forceUpdateLastSend, Map<Object, Long> submap) {
        long now = System.currentTimeMillis();
        Long old;
        if (forceUpdateLastSend) {
            old = submap.put(throttleKey, now);
        } else {
            old = submap.get(throttleKey);
        }
        if (old == null || now - old >= intervalRequired) {
            player.displayClientMessage(text, false);
            if (!forceUpdateLastSend) submap.put(throttleKey, now);
        }
    }
}
