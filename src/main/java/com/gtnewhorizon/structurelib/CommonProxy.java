package com.gtnewhorizon.structurelib;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import com.gtnewhorizon.structurelib.net.ErrorHintParticleMessage;
import com.gtnewhorizon.structurelib.net.UpdateHintParticleMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CommonProxy {

    @OnlyIn(Dist.CLIENT)
    public void hintParticleTinted(Level w, int x, int y, int z, TextureAtlasSprite[] icons, short[] RGBa) {}

    public void hintParticleTinted(Level w, int x, int y, int z, Block block, short[] RGBa) {}

    @OnlyIn(Dist.CLIENT)
    public void hintParticle(Level w, int x, int y, int z, TextureAtlasSprite[] icons) {}

    public void hintParticle(Level w, int x, int y, int z, Block block) {}

    public boolean updateHintParticleTint(Player player, Level w, int x, int y, int z, short[] rgBa) {
        if (player instanceof ServerPlayer) { // just in case
            StructureLib.net.sendTo(
                    new UpdateHintParticleMessage(x, (short) y, z, rgBa[0], rgBa[1], rgBa[2], rgBa[3]),
                    (ServerPlayer) player);
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

    public void preInit(FMLPreInitializationEvent e) {}

    public long getOverworldTime() {
        return MinecraftServer.getServer().getEntityLevel().getTotalLevelTime();
    }

    public void uploadChannels(ItemStack trigger) {}

    public boolean markHintParticleError(Player player, Level w, int x, int y, int z) {
        if (player instanceof ServerPlayer) { // just in case
            StructureLib.net.sendTo(new ErrorHintParticleMessage(x, (short) y, z), (ServerPlayer) player);
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
            player.sendMessage(text, player.getUUID());
            if (!forceUpdateLastSend) submap.put(throttleKey, now);
        }
    }
}
