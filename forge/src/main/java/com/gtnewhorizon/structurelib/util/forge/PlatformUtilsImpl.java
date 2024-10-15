package com.gtnewhorizon.structurelib.util.forge;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.util.PlatformUtils;
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
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.Consumer;

public class PlatformUtilsImpl implements PlatformUtils {
    public boolean isFakePlayer(Player player){
        return player instanceof FakePlayer;
    }

    public boolean isServer(){
        return FMLEnvironment.dist.isDedicatedServer();
    }

    public MinecraftServer getCurrentServer(){
        return ServerLifecycleHooks.getCurrentServer();
    }

    public void registerBlock(ResourceLocation id, Block block){
        block.setRegistryName(id);
        BlockItem blockItem = new BlockItem(block, new Item.Properties().tab(StructureLib.getCreativeTab()));
        blockItem.setRegistryName(id);
        ForgeRegistries.BLOCKS.register(block);
        ForgeRegistries.ITEMS.register(blockItem);
    }

    public void registerItem(ResourceLocation id, Item item){
        item.setRegistryName(id);
        ForgeRegistries.ITEMS.register(item);
    }

    public void openGui(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter){
        NetworkHooks.openGui(player, containerSupplier, extraDataWriter);
    }

    public <T extends AbstractContainerMenu> MenuType<T> create(TriFunction<Integer, Inventory, FriendlyByteBuf, T> factory) {
        return IForgeMenuType.create(factory::apply);
    }
}
