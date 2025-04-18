/*
 * This file is licensed under the GNU Lesser General Public License v3.0,
 * part of Bubustein's Money Mod.
 * Copyright (c) 2022-2025 BUBUSTEIN (GitHub username: BUBUSTEIN13)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package tk.bubustein.money.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import tk.bubustein.money.MoneyMod;

@Environment(EnvType.CLIENT)
public class BankMachineScreen extends AbstractContainerScreen<BankMachineMenu> {
    private static final ResourceLocation BANK_MACHINE_LOCATION = ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID,"textures/container/bank_machine.png");
    private boolean widthTooNarrow;

    public BankMachineScreen(BankMachineMenu craftingMenu, Inventory inventory, Component component) {
        super(craftingMenu, inventory, component);
    }
    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.titleLabelX = 29;
    }
    public void containerTick() {
        super.containerTick();
    }
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics, i, j, f);
        super.render(guiGraphics, i, j, f);
        this.renderTooltip(guiGraphics, i, j);
    }
    protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int k = this.leftPos;
        int l = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderType::guiTextured, BANK_MACHINE_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
    protected boolean isHovering(int i, int j, int k, int l, double d, double e) {
        return (!this.widthTooNarrow) && super.isHovering(i, j, k, l, d, e);
    }
    protected void slotClicked(Slot slot, int i, int j, ClickType clickType) {
        super.slotClicked(slot, i, j, clickType);
    }
}