package com.hbm.render.item;

import org.lwjgl.opengl.GL11;

import com.hbm.forgefluid.HbmFluidContainer;
import com.hbm.render.RenderHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidCanisterRender extends TileEntityItemStackRenderer {

	public static final FluidCanisterRender INSTANCE = new FluidCanisterRender();
	public IBakedModel itemModel;
	public TransformType type;
	
	@Override
	public void renderByItem(ItemStack stack) {
		IBakedModel model = null;
		if(FluidUtil.getFluidContained(stack) != null && HbmFluidContainer.CANISTER.contains(FluidUtil.getFluidContained(stack).getFluid()))
			model = HbmFluidContainer.CANISTER.getEntry(FluidUtil.getFluidContained(stack).getFluid()).getRenderModel();
		if(model == null)
			model = itemModel;
		
		RenderHelper.bindBlockTexture();
		
		GL11.glPushMatrix();
		GL11.glTranslated(0.5, 0.5, 0.5);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		GL11.glPopMatrix();
		super.renderByItem(stack);
	}
	
}
