package com.gtnewhorizon.structurelib.gui;

import com.gtnewhorizon.structurelib.Registry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class ContainerConfigureChannels extends AbstractContainerMenu {
    final InteractionHand hand;
    protected ContainerConfigureChannels(InteractionHand hand, @Nullable MenuType<?> arg, int i) {
        super(arg, i);
        this.hand = hand;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).getItem() == Registry.CONSTRUCTABLE_TRIGGER;
    }
}
