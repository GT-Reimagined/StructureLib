package com.gtnewhorizon.structurelib;

import com.gtnewhorizon.structurelib.block.BlockHint;
import com.gtnewhorizon.structurelib.item.ItemConstructableTrigger;
import com.gtnewhorizon.structurelib.item.ItemFrontRotationTool;
import com.gtnewhorizon.structurelib.util.PlatformUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class Registry {
    /*public static final Block HINT_0 = add("hint_0", new BlockHint());
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
    public static final Block HINT_NOAIR = add("hint_noair", new BlockHint());*/

    public static final Item FRONT_ROTATION_TOOL = add("front_rotation_tool", new ItemFrontRotationTool());
    public static final Item CONSTRUCTABLE_TRIGGER = add("constructable_trigger", new ItemConstructableTrigger());

    public static void init(){}

    private static <B extends Block> B add(String name, B block) {
        PlatformUtils.registerBlock(new ResourceLocation(StructureLibAPI.MOD_ID, name), block);
        return block;
    }

    private static <B extends Item> B add(String name, B item) {
        PlatformUtils.registerItem(new ResourceLocation(StructureLibAPI.MOD_ID, name), item);
        return item;
    }

    public static Block getHint(int dots){
        return switch (dots){
            /*case 1 -> HINT_0;
            case 2 -> HINT_1;
            case 3 -> HINT_2;
            case 4 -> HINT_3;
            case 5 -> HINT_4;
            case 6 -> HINT_5;
            case 7 -> HINT_6;
            case 8 -> HINT_7;
            case 9 -> HINT_8;
            case 10 -> HINT_9;
            case 11 -> HINT_10;
            case 12 -> HINT_11;*/
            default -> Blocks.AIR;
        };
    }
}
