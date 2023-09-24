package com.gtnewhorizon.structurelib.forge;

import com.gtnewhorizon.structurelib.ClientProxy;
import com.gtnewhorizon.structurelib.Registry;
import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.command.CommandConfigureChannels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(StructureLibAPI.MOD_ID)
public class StructureLibForge extends StructureLib {
    public StructureLibForge(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterBlock);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::buildContents);
        MinecraftForge.EVENT_BUS.addListener(this::onCommandRegistration);
        preInit();
        if (FMLEnvironment.dist.isClient()){
            MinecraftForge.EVENT_BUS.addListener(this::onWorldLoad);
        }
    }

    private void buildContents(BuildCreativeModeTabContentsEvent event) {
        // Add to ingredients tab
        if (event.getTab() == creativeTab) {
            event.accept(Registry.CONSTRUCTABLE_TRIGGER);
            event.accept(Registry.FRONT_ROTATION_TOOL);
        }
    }

    private void onRegisterBlock(RegisterEvent event){
        if (event.getRegistryKey() == ForgeRegistries.Keys.BLOCKS) {
            Registry.init();
            creativeTab = CreativeModeTab.builder().icon(() -> new ItemStack(Registry.CONSTRUCTABLE_TRIGGER)).title(Component.translatable("itemGroup." + StructureLibAPI.MOD_ID)).build();
            net.minecraft.core.Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(StructureLibAPI.MOD_ID, "tab"), creativeTab);
        }
    }

    private void onCommandRegistration(RegisterCommandsEvent event){
        CommandConfigureChannels.registerCommands(event.getDispatcher(), event.getCommandSelection());
    }

    @OnlyIn(Dist.CLIENT)
    private void onWorldLoad(LevelEvent.Load event){
        ClientProxy.onLevelLoad(event.getLevel());
    }
}
