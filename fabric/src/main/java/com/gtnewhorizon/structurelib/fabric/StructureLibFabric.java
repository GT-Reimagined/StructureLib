package com.gtnewhorizon.structurelib.fabric;

import com.gtnewhorizon.structurelib.Registry;
import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.command.CommandConfigureChannels;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class StructureLibFabric extends StructureLib implements ModInitializer {
    @Override
    public void onInitialize() {
        creativeTab = FabricItemGroupBuilder.build(new ResourceLocation(StructureLibAPI.MOD_ID, "tab"), () -> new ItemStack(Registry.CONSTRUCTABLE_TRIGGER));
        super.preInit();
        Registry.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandConfigureChannels.registerCommands(dispatcher, environment));
    }
}
