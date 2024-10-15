package com.gtnewhorizon.structurelib.util;

import com.gtnewhorizon.structurelib.StructureLib;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ServiceLoader;
import java.util.function.Consumer;

public interface PlatformUtils {
    PlatformUtils INSTANCE =  ServiceLoader.load(PlatformUtils.class).findFirst().orElseThrow(() -> new IllegalStateException("No implementation of PlatformUtils found"));

    boolean isFakePlayer(Player player);

    boolean isServer();

    default boolean isClient(){
        return !isServer();
    }

    MinecraftServer getCurrentServer();

    void registerBlock(ResourceLocation id, Block block);

    void registerItem(ResourceLocation id, Item item);

    void openGui(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter);

    <T extends AbstractContainerMenu> MenuType<T> create(TriFunction<Integer, Inventory, FriendlyByteBuf, T> factory);
}
