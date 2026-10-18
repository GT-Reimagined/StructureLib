package com.gtnewhorizon.structurelib.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class StructureLibButton extends Button {
    protected StructureLibButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, Supplier::get);
    }
}
