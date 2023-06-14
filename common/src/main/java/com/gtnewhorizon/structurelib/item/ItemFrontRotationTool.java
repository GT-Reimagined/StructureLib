package com.gtnewhorizon.structurelib.item;

import static com.gtnewhorizon.structurelib.StructureLibAPI.MOD_ID;

import java.util.List;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.AlignmentUtility;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemFrontRotationTool extends Item {

    public ItemFrontRotationTool() {
        super(new Properties().tab(StructureLib.getCreativeTab()).stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (AlignmentUtility.handle(context.getPlayer(), context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ())){
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(new TranslatableComponent("item.structurelib.frontRotationTool.desc.0")); // Triggers Front Rotation Interface
        // Rotates only the front panel,
        tooltipComponents.add(new TranslatableComponent("item.structurelib.frontRotationTool.desc.1").withStyle(ChatFormatting.BLUE));
        // which allows structure rotation.
        tooltipComponents.add(new TranslatableComponent("item.structurelib.frontRotationTool.desc.2").withStyle(ChatFormatting.BLUE));
    }
}
