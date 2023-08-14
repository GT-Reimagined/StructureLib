package com.gtnewhorizon.structurelib.fabric;

import com.gtnewhorizon.structurelib.Registry;
import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.command.CommandConfigureChannels;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class StructureLibFabric extends StructureLib implements ModInitializer {
    @Override
    public void onInitialize() {
        creativeTab = FabricItemGroupBuilder.build(new ResourceLocation(StructureLibAPI.MOD_ID, "tab"), () -> new ItemStack(Registry.HINT_0));
        super.preInit();
        Registry.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> CommandConfigureChannels.registerCommands(dispatcher, dedicated ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED));
    }
}
