package com.gtnewhorizon.structurelib.item;

import static com.gtnewhorizon.structurelib.StructureLibAPI.MOD_ID;

import java.util.List;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.AlignmentUtility;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemFrontRotationTool extends Item {

    public ItemFrontRotationTool() {
        super(new Properties().tab(StructureLib.creativeTab).stacksTo(1));
    }

    /*@Override
    public boolean onItemUseFirst(ItemStack stack, Player player, Level world, int x, int y, int z, int side,
                                  float hitX, float hitY, float hitZ) {
        return AlignmentUtility.handle(player, world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, Player p_77624_2_, List aList, boolean p_77624_4_) {
        aList.add(translateToLocal("item.structurelib.frontRotationTool.desc.0")); // Triggers Front Rotation Interface
        aList.add(EnumChatFormatting.BLUE + translateToLocal("item.structurelib.frontRotationTool.desc.1")); // Rotates
                                                                                                             // only the
                                                                                                             // front
                                                                                                             // panel,
        aList.add(EnumChatFormatting.BLUE + translateToLocal("item.structurelib.frontRotationTool.desc.2")); // which
                                                                                                             // allows
                                                                                                             // structure
                                                                                                             // rotation.
    }*/
}
