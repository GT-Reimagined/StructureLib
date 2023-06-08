package com.gtnewhorizon.structurelib.item;

import static com.gtnewhorizon.structurelib.StructureLibAPI.MOD_ID;

import java.util.List;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.alignment.constructable.ConstructableUtility;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemConstructableTrigger extends Item {

    public ItemConstructableTrigger() {
        super(new Properties().tab(StructureLib.creativeTab));
    }

    /*@Override
    public ItemStack onItemRightClick(ItemStack stack, Level world, Player player) {
        if (world.isRemote && getMovingObjectPositionFromPlayer(world, player, true) == null)
            player.openGui(StructureLib.instance(), 0, world, player.inventory.currentItem, 0, 0);
        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, Player player, Level world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
        return ConstructableUtility.handle(stack, player, world, x, y, z, side);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack aStack, Player ep, List aList, boolean boo) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            aList.add(
                    translateToLocalFormatted(
                            "item.structurelib.constructableTrigger.desc.lshift.0",
                            ChannelDataAccessor.countChannelData(aStack)));
            ChannelDataAccessor.iterateChannelData(aStack).map(e -> e.getKey() + ": " + e.getValue())
                    .forEach(aList::add);
        } else {
            aList.add(translateToLocal("item.structurelib.constructableTrigger.desc.0")); // Triggers Constructable
                                                                                          // Interface
            aList.add(BLUE + translateToLocal("item.structurelib.constructableTrigger.desc.1")); // Shows multiblock
                                                                                                 // construction
                                                                                                 // details,
            aList.add(BLUE + translateToLocal("item.structurelib.constructableTrigger.desc.2")); // just Use on a
                                                                                                 // multiblock
                                                                                                 // controller.
            aList.add(BLUE + translateToLocal("item.structurelib.constructableTrigger.desc.3")); // (Sneak Use in
                                                                                                 // creative to build)
            aList.add(BLUE + translateToLocal("item.structurelib.constructableTrigger.desc.4")); // Quantity affects
                                                                                                 // tier/mode/type
            if (ChannelDataAccessor.hasSubChannel(aStack))
                aList.add(translateToLocal("item.structurelib.constructableTrigger.desc.5"));
        }
    }*/
}
