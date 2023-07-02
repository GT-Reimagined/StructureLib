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

import java.util.function.Consumer;

public class PlatformUtils {
    @ExpectPlatform
    public static boolean isFakePlayer(Player player){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isServer(){
        throw new AssertionError();
    }

    public static boolean isClient(){
        return !isServer();
    }

    @ExpectPlatform
    public static MinecraftServer getCurrentServer(){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerBlock(ResourceLocation id, Block block){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerItem(ResourceLocation id, Item item){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void openGui(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends AbstractContainerMenu> MenuType<T> create(TriFunction<Integer, Inventory, FriendlyByteBuf, T> factory) {
        throw new AssertionError();
    }
}
