package com.gtnewhorizon;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.block.BlockHint;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class Registry {
    public static final Block HINT_0 = add("hint_0", new BlockHint());
    public static final Block HINT_1 = add("hint_1", new BlockHint());
    public static final Block HINT_2 = add("hint_2", new BlockHint());
    public static final Block HINT_3 = add("hint_3", new BlockHint());
    public static final Block HINT_4 = add("hint_4", new BlockHint());
    public static final Block HINT_5 = add("hint_5", new BlockHint());
    public static final Block HINT_6 = add("hint_6", new BlockHint());
    public static final Block HINT_7 = add("hint_7", new BlockHint());
    public static final Block HINT_8 = add("hint_8", new BlockHint());
    public static final Block HINT_9 = add("hint_9", new BlockHint());
    public static final Block HINT_10 = add("hint_10", new BlockHint());
    public static final Block HINT_11 = add("hint_11", new BlockHint());
    public static final Block HINT_AIR = add("hint_air", new BlockHint());
    public static final Block HINT_DEFAULT = add("hint_default", new BlockHint());
    public static final Block HINT_ERROR = add("hint_error", new BlockHint());
    public static final Block HINT_NOAIR = add("hint_noair", new BlockHint());

    private static <B extends Block> B add(String name, B block) {
        block.setRegistryName(StructureLibAPI.MOD_ID, name);
        BlockItem blockItem = new BlockItem(block, new Item.Properties().tab(StructureLib.creativeTab));
        blockItem.setRegistryName(StructureLibAPI.MOD_ID, name);
        ForgeRegistries.BLOCKS.register(block);
        ForgeRegistries.ITEMS.register(blockItem);
        return block;
    }

    private static <B extends Item> B add(String name, B item) {
        item.setRegistryName(StructureLibAPI.MOD_ID, name);
        ForgeRegistries.ITEMS.register(item);
        return item;
    }
}
