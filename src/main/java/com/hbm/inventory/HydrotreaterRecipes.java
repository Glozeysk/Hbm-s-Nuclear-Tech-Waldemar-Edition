package com.hbm.inventory;

import java.util.HashMap;

import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.util.Tuple.Triplet;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Recipe list for the hydrotreater: input fluid -> (hydrogen cost, output 1, output 2).
 * Ported from NTM:CE's HydrotreatingRecipes, trimmed to fluids that exist in this fork.
 */
public class HydrotreaterRecipes {

	public static HashMap<Fluid, Triplet<FluidStack, FluidStack, FluidStack>> recipes = new HashMap<Fluid, Triplet<FluidStack, FluidStack, FluidStack>>();

	public static void registerRecipes() {
		//test recipe: 1000mB water + 100mB hydrogen -> 300mB heavy water + 200mB oxygen
		recipes.put(net.minecraftforge.fluids.FluidRegistry.WATER, new Triplet<FluidStack, FluidStack, FluidStack>(
				new FluidStack(ModForgeFluids.hydrogen, 100),
				new FluidStack(ModForgeFluids.heavywater, 300),
				new FluidStack(ModForgeFluids.oxygen, 200)
		));
		//second test recipe: 1000mB crackoil + 100mB hydrogen -> 700mB diesel + 100mB gas
		recipes.put(ModForgeFluids.crackoil, new Triplet<FluidStack, FluidStack, FluidStack>(
				new FluidStack(ModForgeFluids.hydrogen, 100),
				new FluidStack(ModForgeFluids.diesel, 700),
				new FluidStack(ModForgeFluids.gas, 100)
		));
	}

	public static Triplet<FluidStack, FluidStack, FluidStack> getRecipe(Fluid input) {
		return recipes.get(input);
	}
}
