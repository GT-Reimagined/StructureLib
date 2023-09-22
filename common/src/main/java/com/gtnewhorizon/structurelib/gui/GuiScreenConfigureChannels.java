package com.gtnewhorizon.structurelib.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Collectors;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.gui.GuiScrollableList.IGuiScreen;

public class GuiScreenConfigureChannels extends AbstractContainerScreen<ContainerConfigureChannels> implements IGuiScreen {

    private static final int KEY_MAX_WIDTH = 50;
    private final ItemStack trigger;
    //private final GuiChannelsList list;
    private final InteractionHand hand;
    private EditBox key, value;
    protected int guiTop, guiLeft;
    private List<Button> buttonList = new ArrayList<>();

    public GuiScreenConfigureChannels(ContainerConfigureChannels container, Inventory invPlayer, Component title) {
        super(container, invPlayer, title);
        this.trigger = invPlayer.player.getItemInHand(container.hand);
        this.hand = container.hand;
        /*list = new GuiChannelsList(152, 100, 12, 12, 14);
        list.addSelectionListener((list, selectedIndex) -> {
            Entry<String, Integer> e = list.getElementAt(selectedIndex);
            key.setValue(e.getKey());
            value.setValue(e.getValue().toString());
            updateButtons();
        });*/
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (this.width - this.getXSize()) / 2;
        guiTop = (this.height - this.getYSize()) / 2;
        key = new EditBox(this.font, guiLeft + 45, guiTop + 119, 151 + 12 - 45, 12, Component.literal("")){
            @Override
            public void insertText(String textToWrite) {
                super.insertText(textToWrite.toLowerCase(Locale.ROOT));
                updateButtons();
            }

            @Override
            public void setValue(String text) {
                super.setValue(text);
                updateButtons();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean flag = mouseX >= this.x && mouseX < this.x + this.width
                    && mouseY >= this.y
                    && mouseY < this.y + this.height;
                if (flag && button == 1) {
                    key.setValue("");
                    value.setValue("");
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
        value = new EditBox(this.font, guiLeft + 45, guiTop + 139, 151 + 12 - 45, 12, Component.literal("")){
            @Override
            public void insertText(String text) {
                if (text != null && text.codePoints().allMatch(Character::isDigit)) {
                    super.insertText(text);
                    updateButtons();
                }
            }

            @Override
            public void setFocused(boolean focused) {
                if (!focused && isFocused() && !StringUtils.isBlank(getValue())) {
                    int result;
                    try {
                        result = Math.max(Integer.parseInt(getValue()), 1);
                    } catch (NumberFormatException e) {
                        result = 1;
                    }
                    setValue(String.valueOf(result));
                }
                super.setFocused(focused);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean flag = mouseX >= this.x && mouseX < this.x + this.width
                    && mouseY >= this.y
                    && mouseY < this.y + this.height;
                if (flag && button == 1) {
                    setValue("");
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public void setValue(String text) {
                super.setValue(text);
                updateButtons();
            }
        };
        this.key.setMaxLength(32500);
        this.value.setMaxLength(32500);
        this.addRenderableOnly(key);
        this.addRenderableOnly(value);
        //list.onGuiInit(this);
        addButton(new Button(
            guiLeft + 12,
            guiTop + 157,
            47,
            20,
            Component.translatable("item.structurelib.constructableTrigger.gui.add"),
            b -> {
                int value = getValue();
                if (value <= 0) return;
                ChannelDataAccessor.setChannelData(trigger, key.getValue(), value);
            }
        ));
        addButton(new Button(
            guiLeft + 65,
            guiTop + 157,
            47,
            20,
            Component.translatable("item.structurelib.constructableTrigger.gui.unset"),
            b -> {
                ChannelDataAccessor.unsetChannelData(trigger, key.getValue());
            }
        ));
        addButton(new Button(
            guiLeft + 118,
            guiTop + 157,
            47,
            20,
            Component.translatable("item.structurelib.constructableTrigger.gui.wipe"),
            b -> {
                ChannelDataAccessor.wipeChannelData(trigger);
            }
        ));
        updateButtons();
    }

    @Override
    public int getGuiLeft() {
        return guiLeft;
    }

    @Override
    public int getGuiTop() {
        return guiTop;
    }

    @Override
    public int getXSize() {
        return 176;
    }

    @Override
    public int getYSize() {
        return 188;
    }

    @Override
    public void addButton(Button button) {
        getButtonList().add(button);
        addWidget(button);
    }

    @Override
    public void removeButton(Button button) {
        getButtonList().remove(button);
        removeWidget(button);
    }

    @Override
    public int getOverlayOffsetX() {
        return 0;
    }

    @Override
    public void doActionPerformed(Button but) {

    }

    @SuppressWarnings("unchecked")
    private List<Button> getButtonList() {
        return buttonList;
    }

    /*@Override
    public void handleMouseInput() {
        int delta = Mouse.getEventDWheel();
        if (delta != 0) list.handleDWheel(delta);
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mX, int mY, int button) {
        key.mouseClicked(mX, mY, button);
        value.mouseClicked(mX, mY, button);
        super.mouseClicked(mX, mY, button);
    }

    @Override
    protected void keyTyped(char aChar, int aKey) {
        switch (aKey) {
            case Keyboard.KEY_TAB:
                if (key.isFocused()) {
                    key.setFocused(false);
                    value.setFocused(true);
                } else {
                    key.setFocused(true);
                    value.setFocused(false);
                }
                return;
            case Keyboard.KEY_RETURN:
            case Keyboard.KEY_NUMPADENTER:
                GuiButton add = getButtonList().get(0);
                if (add.enabled) doActionPerformed(add);
                return;
            case Keyboard.KEY_UP:
                if (list.selectedIndex > 0) list.setSelection(list.selectedIndex - 1);
                return;
            case Keyboard.KEY_DOWN:
                if (list.selectedIndex < list.getNumElements() - 1) list.setSelection(list.selectedIndex + 1);
                return;
        }
        if (key.textboxKeyTyped(aChar, aKey)) {
            updateButtons();
            return;
        }
        if (value.textboxKeyTyped(aChar, aKey)) return;
        super.keyTyped(aChar, aKey);
    }*/

    private void updateButtons() {
        // this will be called from setText of key and value. NEVER UPDATE THE VALUE OF THESE HERE OR GET A
        // STACKOVERFLOW!
        String keyText = key.getValue();
        boolean existing = !StringUtils.isEmpty(keyText) && ChannelDataAccessor.hasSubChannel(trigger, keyText);
        String translation = existing ? "item.structurelib.constructableTrigger.gui.set" : "item.structurelib.constructableTrigger.gui.add";
        getButtonList().get(0).setMessage(Component.translatable(translation));
        getButtonList().get(0).active = !StringUtils.isBlank(value.getValue());
        getButtonList().get(1).active = existing && !StringUtils.isBlank(value.getValue());
    }

    private int getValue() {
        try {
            return Integer.parseInt(value.getValue());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        StructureLib.instance().proxy().uploadChannels(trigger, hand);
    }

    /*@Override
    public void updateScreen() {
        super.updateScreen();
        key.updateCursorCounter();
        value.updateCursorCounter();
    }*/


    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        drawTexture(poseStack, new ResourceLocation("structurelib", "textures/gui/channels.png"), guiLeft, guiTop, 0, 0, 176, 188);
        //list.drawScreen(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
        this.font.draw(poseStack, Component.translatable("item.structurelib.constructableTrigger.gui.key"), 12, 122, 4210752);
        this.font.draw(poseStack, Component.translatable("item.structurelib.constructableTrigger.gui.value"), 12, 142, 4210752);
    }

    public void drawTexture(PoseStack stack, ResourceLocation loc, int left, int top, int x, int y, int sizeX, int sizeY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, loc);
        //AbstractGui.blit(stack, left, top, x, y, sizeX, sizeY);
        GuiComponent.blit(stack, left, top, x, y, sizeX, sizeY, this.getXSize(), this.getYSize());
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public Screen getGui() {
        return this;
    }

    /*private class GuiChannelsList extends GuiScrollableList<Entry<String, Integer>> {

        private List<Entry<String, Integer>> cache;

        public GuiChannelsList(int width, int height, int originX, int originY, int slotHeight) {
            super(width, height, originX, originY, slotHeight);
        }

        @Override
        public Entry<String, Integer> getElementAt(int index) {
            return ChannelDataAccessor.iterateChannelData(trigger).sorted().skip(index).findFirst().orElse(null);
        }

        @Override
        public int getNumElements() {
            return ChannelDataAccessor.countChannelData(trigger);
        }

        @Override
        protected boolean elementClicked(int elementIndex, boolean doubleClick, int mXRelative, int mYRelative) {
            if (mXRelative >= margin + 1 && mXRelative <= margin + 5
                    && mYRelative >= margin / 2 + 1
                    && mYRelative <= margin / 2 + 5) {
                Entry<String, Integer> e = getElementAt(elementIndex);
                if (e != null) {
                    ChannelDataAccessor.unsetChannelData(trigger, e.getKey());
                    return false;
                }
            }
            return super.elementClicked(elementIndex, doubleClick, mXRelative, mYRelative);
        }

        @Override
        protected void prepareDrawElements() {
            cache = ChannelDataAccessor.iterateChannelData(trigger).sorted().collect(Collectors.toList());
        }

        @Override
        protected void drawElement(int elementIndex, int x, int y, int height, Tessellator tessellator,
                boolean isHovering) {
            if (elementIndex < 0 || elementIndex >= cache.size()) {
                return;
            }
            Entry<String, Integer> e = cache.get(elementIndex);
            if (e == null) return;
            if (elementIndex > 0) {
                // args x1, x2, y1, color
                drawHorizontalLine(minX + 1, maxX - 1, y - 2, 0xffaaaaaa);
            }
            int keyMaxWidth = KEY_MAX_WIDTH * 2;
            if (isHovering) {
                mc.renderEngine.bindTexture(new ResourceLocation("structurelib", "textures/gui/channels.png"));
                GL11.glColor4f(1, 1, 1, 1);
                drawTexturedModalRect(x + margin + 1, y + margin / 2 + 1, 251, 251, 5, 5);
            }
            fontRendererObj.drawString(trim(e.getKey(), keyMaxWidth - 9), x + margin + 8, y + margin / 2, 0xffffff);
            int valueX = x + margin + keyMaxWidth + margin;
            fontRendererObj.drawString(trim(e.getValue().toString(), maxX - valueX), valueX, y + margin / 2, 0xffffff);
        }

        @Override
        public void drawScreen(PoseStack poseStack, int mX, int mY, float partialTick) {
            super.drawScreen(poseStack, mX, mY, partialTick);
            // args x1, y1, y2, color
            drawVerticalLine(minX + margin + margin / 2 + KEY_MAX_WIDTH * 2, minY + 1, maxY - 1, 0xffaaaaaa);
        }

        public String trim(String e, int keyMaxWidth) {
            // TODO optimize this to not create a billion string, or maybe cache this
            String s = fontRendererObj.trimStringToWidth(e, keyMaxWidth);
            if (s.length() != e.length()) {
                StringBuilder buffer = new StringBuilder(s).deleteCharAt(s.length() - 1).append("...");
                while ((s = fontRendererObj.trimStringToWidth(buffer.toString(), keyMaxWidth)).length()
                        != buffer.length())
                    // drop last original char
                    buffer.deleteCharAt(s.length() - 4);
                do {
                    buffer.append('.');
                } while (fontRendererObj.getStringWidth(buffer.toString()) <= keyMaxWidth);
                s = buffer.deleteCharAt(s.length() - 1).insert(s.length() - 3, EnumChatFormatting.GRAY).toString();
            }
            return s;
        }
    }*/
}
