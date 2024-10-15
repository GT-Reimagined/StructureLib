package com.gtnewhorizon.structurelib.gui;

import com.gtnewhorizon.structurelib.Registry;
import com.gtnewhorizon.structurelib.util.PlatformUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class ContainerConfigureChannels extends AbstractContainerMenu {
    final InteractionHand hand;
    static MenuType<ContainerConfigureChannels> menuType;
    public ContainerConfigureChannels(InteractionHand hand, @Nullable MenuType<?> arg, int i) {
        super(arg, i);
        this.hand = hand;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).getItem() == Registry.CONSTRUCTABLE_TRIGGER;
    }

    public static MenuType<ContainerConfigureChannels> getMenuType() {
        if (menuType == null){
            menuType = PlatformUtils.INSTANCE.create((i, c, f) -> {
                InteractionHand hand = f.readEnum(InteractionHand.class);
                return new ContainerConfigureChannels(hand, menuType, i);
            });
        }
        return menuType;
    }
}
