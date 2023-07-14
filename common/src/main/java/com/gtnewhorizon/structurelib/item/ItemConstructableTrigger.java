package com.gtnewhorizon.structurelib.item;

import static com.gtnewhorizon.structurelib.StructureLibAPI.MOD_ID;

import java.util.List;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.alignment.constructable.ConstructableUtility;
import com.gtnewhorizon.structurelib.gui.ContainerConfigureChannels;
import com.gtnewhorizon.structurelib.util.PlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemConstructableTrigger extends Item implements MenuProvider {

    public ItemConstructableTrigger() {
        super(new Properties().tab(StructureLib.getCreativeTab()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide){
            PlatformUtils.openGui((ServerPlayer) player, this, f -> f.writeEnum(usedHand));
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return ConstructableUtility.handle(context.getItemInHand(), context.getPlayer(), context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(), context.getClickedFace().get3DDataValue()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent(getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory arg, Player arg2) {
        return new ContainerConfigureChannels(InteractionHand.MAIN_HAND, ContainerConfigureChannels.getMenuType(), i);
    }

    /*

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
