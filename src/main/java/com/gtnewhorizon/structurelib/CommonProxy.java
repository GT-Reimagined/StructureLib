package com.gtnewhorizon.structurelib;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.world.Level;

import com.gtnewhorizon.structurelib.net.ErrorHintParticleMessage;
import com.gtnewhorizon.structurelib.net.UpdateHintParticleMessage;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void hintParticleTinted(Level w, int x, int y, int z, IIcon[] icons, short[] RGBa) {}

    public void hintParticleTinted(Level w, int x, int y, int z, Block block, int meta, short[] RGBa) {}

    public void hintParticle(Level w, int x, int y, int z, IIcon[] icons) {}

    public void hintParticle(Level w, int x, int y, int z, Block block, int meta) {}

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

    public void addThrottledChat(Object throttleKey, Player player, IChatComponent text, short intervalRequired,
            boolean forceUpdateLastSend) {
        if (player instanceof ServerPlayer) {
            Map<Object, Long> submap = throttleMap.computeIfAbsent((ServerPlayer) player, p -> new HashMap<>());
            addThrottledChat(throttleKey, player, text, intervalRequired, forceUpdateLastSend, submap);
        }
    }

    protected static void addThrottledChat(Object throttleKey, Player player, IChatComponent text,
            short intervalRequired, boolean forceUpdateLastSend, Map<Object, Long> submap) {
        long now = System.currentTimeMillis();
        Long old;
        if (forceUpdateLastSend) {
            old = submap.put(throttleKey, now);
        } else {
            old = submap.get(throttleKey);
        }
        if (old == null || now - old >= intervalRequired) {
            player.addChatComponentMessage(text);
            if (!forceUpdateLastSend) submap.put(throttleKey, now);
        }
    }
}
