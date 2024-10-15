package com.gtnewhorizon.structurelib.util.fabric;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.util.PlatformUtils;
import io.github.fabricators_of_create.porting_lib.util.NetworkUtil;
import io.github.fabricators_of_create.porting_lib.util.ServerLifecycleHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
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

public class PlatformUtilsImpl implements PlatformUtils {
    public boolean isFakePlayer(Player player){
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        return serverPlayer.getClass() != ServerPlayer.class;
    }

    public MinecraftServer getCurrentServer(){
        return ServerLifecycleHooks.getCurrentServer();
    }

    public boolean isServer(){
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    public void registerBlock(ResourceLocation id, Block block){
        BlockItem blockItem = new BlockItem(block, new Item.Properties().tab(StructureLib.getCreativeTab()));
        Registry.register(Registry.BLOCK, id, block);
        Registry.register(Registry.ITEM, id, blockItem);
    }

    public void registerItem(ResourceLocation id, Item item){
        Registry.register(Registry.ITEM, id, item);
    }

    public void openGui(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter){
        NetworkUtil.openGui(player, containerSupplier, extraDataWriter);
    }

    public <T extends AbstractContainerMenu> MenuType<T> create(TriFunction<Integer, Inventory, FriendlyByteBuf, T> factory) {
        return new ExtendedScreenHandlerType<>(factory::apply);
    }
}
