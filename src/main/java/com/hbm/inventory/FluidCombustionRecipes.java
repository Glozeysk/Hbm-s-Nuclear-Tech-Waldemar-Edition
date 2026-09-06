package com.hbm.inventory;

import java.util.HashSet;
import java.util.HashMap;

import com.hbm.forgefluid.ModForgeFluids;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidCombustionRecipes {
	
	public static HashMap<Fluid, Integer> resultingTU = new HashMap<Fluid, Integer>();
	//for 1000 mb
	public static void registerFluidCombustionRecipes() {
		//oil and ethanol stay manual (kept as manual fluids); everything else via HbmFluid.applyAllFuels
		addBurnableFluid(ModForgeFluids.oil, 10);
		addBurnableFluid(ModForgeFluids.ethanol, 75);

		addBurnableFluid("liquidhydrogen", 5);
		addBurnableFluid("liquiddeuterium", 5);
		addBurnableFluid("liquidtritium", 5);
		addBurnableFluid("crude_oil", 10);
		addBurnableFluid("oilgc", 10);
		addBurnableFluid("fuel", 120);
		addBurnableFluid("refined_biofuel", 150);
		addBurnableFluid("pyrotheum", 1_500);
		addBurnableFluid("ethanol", 30);
		addBurnableFluid("plantoil", 50);
		addBurnableFluid("acetaldehyde", 80);
		addBurnableFluid("biodiesel", 175);

		com.hbm.forgefluid.HbmFluid.applyAllFuels();
	}

	public static int getFlameEnergy(Fluid f){
		Integer heat = resultingTU.get(f);
		if(heat != null)
			return heat;
		return 0;
	}

	public static boolean hasFuelRecipe(Fluid fluid){
		return resultingTU.containsKey(fluid);
	}

	public static void addBurnableFluid(Fluid fluid, int heatPerMiliBucket) {
		resultingTU.put(fluid, heatPerMiliBucket);
	}

	public static void addBurnableFluid(String fluid, int heatPerMiliBucket){
		if(FluidRegistry.isFluidRegistered(fluid)){
			addBurnableFluid(FluidRegistry.getFluid(fluid), heatPerMiliBucket);
		}
	}

	public static void removeBurnableFluid(Fluid fluid){
		resultingTU.remove(fluid);
	}

	public static void removeBurnableFluid(String fluid){
		if(FluidRegistry.isFluidRegistered(fluid)){
			resultingTU.remove(FluidRegistry.getFluid(fluid));
		}
	}
}