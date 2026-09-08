package com.hbm.handler.jei;

import com.hbm.handler.jei.JeiRecipes.HydrotreatingRecipe;
import com.hbm.lib.RefStrings;

import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.ResourceLocation;

//Reuses the same generic "in -> out" background as CrackingRecipeHandler; no new JEI art needed.
public class HydrotreatingRecipeHandler implements IRecipeCategory<HydrotreatingRecipe> {

	public static ResourceLocation gui_rl = new ResourceLocation(RefStrings.MODID + ":textures/gui/jei/gui_nei_two.png");

	protected final IDrawable background;

	public HydrotreatingRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 43, 34, 133 - 43, 52 - 34);
	}

	@Override
	public String getUid() {
		return JEIConfig.HYDROTREATING;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("tile.machine_hydrotreater.name");
	}

	@Override
	public String getModName() {
		return RefStrings.MODID;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, HydrotreatingRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		//feedstock + hydrogen cost on the left
		guiItemStacks.init(0, true, 0, 0);
		guiItemStacks.init(1, true, 18, 0);

		//product + byproduct on the right, past the arrow
		guiItemStacks.init(2, false, 54, 0);
		guiItemStacks.init(3, false, 72, 0);

		guiItemStacks.set(ingredients);
	}

}
