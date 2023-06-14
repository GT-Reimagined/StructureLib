package com.gtnewhorizon.structurelib.forge;

import com.gtnewhorizon.structurelib.Registry;
import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.command.CommandConfigureChannels;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(StructureLibAPI.MOD_ID)
public class StructureLibForge extends StructureLib {
    public StructureLibForge(){
        creativeTab = new CreativeModeTab("structurelib:tab") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Registry.HINT_0);
            }
        };
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, this::onRegisterBlock);
        MinecraftForge.EVENT_BUS.addListener(this::onCommandRegistration);
        preInit();
    }

    private void onRegisterBlock(RegistryEvent.Register<Block> event){
        Registry.init();
    }

    private void onCommandRegistration(RegisterCommandsEvent event){
        CommandConfigureChannels.registerCommands(event.getDispatcher(), event.getEnvironment());
    }
}
