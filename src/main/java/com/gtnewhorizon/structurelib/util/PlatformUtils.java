package com.gtnewhorizon.structurelib.util;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

public class PlatformUtils {
    public static boolean isFakePlayer(Player player){
        return player instanceof FakePlayer;
    }

    public static boolean isServer(){
        return FMLEnvironment.dist.isDedicatedServer();
    }

    public static boolean isClient(){
        return !isServer();
    }

    public static void registerBlock(ResourceLocation id, Block block){
        block.setRegistryName(id);
        BlockItem blockItem = new BlockItem(block, new Item.Properties().tab(StructureLib.creativeTab));
        blockItem.setRegistryName(id);
        ForgeRegistries.BLOCKS.register(block);
        ForgeRegistries.ITEMS.register(blockItem);
    }

    public static void registerItem(ResourceLocation id, Item item){
        item.setRegistryName(id);
        ForgeRegistries.ITEMS.register(item);
    }
}
