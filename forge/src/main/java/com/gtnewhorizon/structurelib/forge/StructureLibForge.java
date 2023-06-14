package com.gtnewhorizon.structurelib.forge;

import com.gtnewhorizon.structurelib.Registry;
import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
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
        preInit();
    }

    private void onRegisterBlock(RegistryEvent.Register<Block> event){
        Registry.init();
    }
}
