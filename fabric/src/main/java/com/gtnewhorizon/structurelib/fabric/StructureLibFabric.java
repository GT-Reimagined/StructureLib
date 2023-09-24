package com.gtnewhorizon.structurelib.fabric;

import com.gtnewhorizon.structurelib.Registry;
import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.command.CommandConfigureChannels;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class StructureLibFabric extends StructureLib implements ModInitializer {
    @Override
    public void onInitialize() {
        creativeTab = FabricItemGroup.builder().title(Component.translatable("itemGroup."+StructureLibAPI.MOD_ID)).icon(() -> new ItemStack(Registry.CONSTRUCTABLE_TRIGGER)).build();
        super.preInit();
        net.minecraft.core.Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(StructureLibAPI.MOD_ID, "tab"), creativeTab);
        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(StructureLibAPI.MOD_ID, "tab"))).register(e -> {
            e.accept(Registry.CONSTRUCTABLE_TRIGGER);
            e.accept(Registry.FRONT_ROTATION_TOOL);
        });
        Registry.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandConfigureChannels.registerCommands(dispatcher, environment));
    }
}
