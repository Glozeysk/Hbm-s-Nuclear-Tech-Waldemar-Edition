package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.forgefluid.FFUtils;
import com.hbm.inventory.container.ContainerMachineHydrotreater;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

//Ported from NTM:CE, adapted to this fork's FFUtils tank rendering (no FluidTankNTM)
public class GUIMachineHydrotreater extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_hydrotreater.png");
	private TileEntityMachineHydrotreater hydrotreater;

	public GUIMachineHydrotreater(InventoryPlayer invPlayer, TileEntityMachineHydrotreater te) {
		super(new ContainerMachineHydrotreater(invPlayer, te));
		hydrotreater = te;

		this.xSize = 176;
		this.ySize = 238;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 18, 16, 52, hydrotreater.tanks[0], hydrotreater.tankTypes[0]);
		FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 53, guiTop + 18, 16, 52, hydrotreater.tanks[1], hydrotreater.tankTypes[1]);
		FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 125, guiTop + 18, 16, 52, hydrotreater.tanks[2], hydrotreater.tankTypes[2]);
		FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 143, guiTop + 18, 16, 52, hydrotreater.tanks[3], hydrotreater.tankTypes[3]);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 17, guiTop + 18, 16, 52, hydrotreater.power, TileEntityMachineHydrotreater.maxPower);

		if(this.mc.player.inventory.getItemStack().isEmpty() && this.isMouseOverSlot(this.inventorySlots.getSlot(9), mouseX, mouseY) && !this.inventorySlots.getSlot(9).getHasStack()) {
			List<Object[]> lines = new ArrayList<Object[]>();
			ItemStack screwdriver = new ItemStack(ModItems.screwdriver);
			lines.add(new Object[] {screwdriver});
			lines.add(new Object[] {screwdriver.getDisplayName()});
			this.drawStackText(lines, mouseX, mouseY, this.fontRenderer, 0);
		}

		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.hydrotreater.hasCustomInventoryName() ? this.hydrotreater.getInventoryName() : I18n.format(this.hydrotreater.getInventoryName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int j = (int) hydrotreater.getPowerScaled(52);
		drawTexturedModalRect(guiLeft + 17, guiTop + 70 - j, 176, 52 - j, 16, j);

		//FFUtils.drawLiquid draws top-anchored tiles (RenderHelper.drawScaledTexture's posY is the tile TOP, not
		//bottom), so the real bottom ends up at guiTop + (offsetY-44) + 16 = guiTop + offsetY - 28. To land the
		//liquid's bottom at guiTop+70 (matching the tank frame), pass offsetY = 70 + 28 = 98.
		FFUtils.drawLiquid(hydrotreater.tanks[0], guiLeft, guiTop, zLevel, 16, 52, 35, 98);
		FFUtils.drawLiquid(hydrotreater.tanks[1], guiLeft, guiTop, zLevel, 16, 52, 53, 98);
		FFUtils.drawLiquid(hydrotreater.tanks[2], guiLeft, guiTop, zLevel, 16, 52, 125, 98);
		FFUtils.drawLiquid(hydrotreater.tanks[3], guiLeft, guiTop, zLevel, 16, 52, 143, 98);
	}
}
