package com.gtnewhorizon.structurelib;

import net.minecraft.entity.player.Player;
import net.minecraft.world.Level;

import com.gtnewhorizon.structurelib.gui.GuiScreenConfigureChannels;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, Player player, Level world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, Player player, Level world, int x, int y, int z) {
        switch (ID) {
            case 0:
                return new GuiScreenConfigureChannels(player.inventory.getStackInSlot(x));
            default:
                return null;
        }
    }
}
