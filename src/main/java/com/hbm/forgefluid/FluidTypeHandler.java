package com.hbm.forgefluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.render.misc.EnumSymbol;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidTypeHandler {

	private static Map<String, FluidProperties> fluidProperties = new HashMap<String, FluidProperties>();
	public static final FluidProperties NONE = new FluidProperties(0, 0, 0, EnumSymbol.NONE);
	
	public static FluidProperties getProperties(Fluid f){
		if(f == null)
			return NONE;
		FluidProperties p = fluidProperties.get(f.getName());
		return p != null ? p : NONE;
	}
	
	public static FluidProperties getProperties(FluidStack f){
		if(f == null)
			return NONE;
		return getProperties(f.getFluid());
	}

	public static float getDFCEfficiency(Fluid f){
		FluidProperties prop = getProperties(f);
		return prop.dfcFuel;
	}
	
	public static boolean isAntimatter(Fluid f){
		return containsTrait(f, FluidTrait.AMAT);
	}
	
	public static boolean isCorrosivePlastic(Fluid f){
		return containsTrait(f, FluidTrait.CORROSIVE) || containsTrait(f, FluidTrait.CORROSIVE_2);
	}
	
	public static boolean isCorrosiveIron(Fluid f){
		return containsTrait(f, FluidTrait.CORROSIVE_2);
	}
	
	public static boolean isHot(Fluid f){
		if(f == null)
			return false;
		return f.getTemperature() >= 373;
	}

	public static boolean isReallyHot(Fluid f){
		if(f == null)
			return false;
		return f.getTemperature() > 873;
	}

	public static boolean is1300Hot(Fluid f){
		if(f == null)
			return false;
		return f.getTemperature() > 1573;
	}

	public static boolean is1500Hot(Fluid f){
		if(f == null)
			return false;
		return f.getTemperature() > 1773;
	}

	public static boolean isExtremelyHot(Fluid f){
		if(f == null)
			return false;
		return f.getTemperature() > 3273;
	}

	public static boolean is20KHot(Fluid f){
		if(f == null)
			return false;
		return f.getTemperature() > 20273;
	}

	public static boolean noID(Fluid f){
		return containsTrait(f, FluidTrait.NO_ID);
	}

	public static boolean noContainer(Fluid f){
		return containsTrait(f, FluidTrait.NO_CONTAINER);
	}
	
	public static boolean containsTrait(Fluid f, FluidTrait t){
		if(f == null)
			return false;
		FluidProperties p = fluidProperties.get(f.getName());
		if(p == null)
			return false;
		return p.traits.contains(t) || p.traitValues.containsKey(t);
	}

	//Reads a valued trait (combustion/rtg/etc.); absent trait returns def. Zero is never stored, so 0 == not present.
	public static double getTraitValue(Fluid f, FluidTrait t, double def){
		if(f == null)
			return def;
		FluidProperties p = fluidProperties.get(f.getName());
		if(p == null)
			return def;
		Double v = p.traitValues.get(t);
		return v != null ? v : def;
	}

	//Lets the declarative HbmFluid builder feed properties into the central map at registration time.
	public static void registerProperties(String name, FluidProperties p){
		fluidProperties.put(name, p);
	}
	
	//Using strings so it's possible to specify properties for fluids from other mods
	public static void registerFluidProperties(){
		fluidProperties.put(FluidRegistry.WATER.getName(), new FluidProperties(0, 0, 0, EnumSymbol.NONE));
		fluidProperties.put(FluidRegistry.LAVA.getName(), new FluidProperties(4, 0, 0, EnumSymbol.NOWATER));

		//oil, ethanol and schrabidic stay manual (common names / custom Fluid subclass); everything else via HbmFluid.applyAllProperties
		fluidProperties.put(ModForgeFluids.oil.getName(), new FluidProperties(2, 1, 0, EnumSymbol.NONE));
		fluidProperties.put(ModForgeFluids.ethanol.getName(), new FluidProperties(2, 3, 1, EnumSymbol.NONE));
		fluidProperties.put(ModForgeFluids.schrabidic.getName(), new FluidProperties(5, 0, 5, 1.7F, EnumSymbol.ACID, FluidTrait.CORROSIVE_2));

		fluidProperties.put(ModForgeFluids.toxic_fluid.getName(), new FluidProperties(3, 0, 4, EnumSymbol.RADIATION, FluidTrait.CORROSIVE_2));
		fluidProperties.put(ModForgeFluids.radwater_fluid.getName(), new FluidProperties(2, 0, 0, EnumSymbol.RADIATION));
		fluidProperties.put(ModForgeFluids.mud_fluid.getName(), new FluidProperties(4, 0, 1, EnumSymbol.ACID, FluidTrait.CORROSIVE_2));
		fluidProperties.put(ModForgeFluids.corium_fluid.getName(), new FluidProperties(4, 0, 2, EnumSymbol.RADIATION, FluidTrait.CORROSIVE_2));
		fluidProperties.put(ModForgeFluids.volcanic_lava_fluid.getName(), new FluidProperties(4, 1, 1, EnumSymbol.NOWATER));

		HbmFluid.applyAllProperties();
	}
	
	public static class FluidProperties {
		
		public final int poison;
		public final int flammability;
		public final int reactivity;
		public final float dfcFuel;
		public final EnumSymbol symbol;
		public final List<FluidTrait> traits = new ArrayList<>();
		public final Map<FluidTrait, Double> traitValues = new HashMap<>();

		public FluidProperties(int p, int f, int r, EnumSymbol symbol, FluidTrait... traits) {
			this(p, f, r, 0, symbol, traits);
		}
		
		public FluidProperties(int p, int f, int r, float dfc, EnumSymbol symbol, FluidTrait... traits) {
			this.poison = p;
			this.flammability = f;
			this.reactivity = r;
			this.dfcFuel = dfc;
			this.symbol = symbol;
			for(FluidTrait trait : traits)
				this.traits.add(trait);
		}
	}
	
	public static enum FluidTrait {
		//flag traits
		AMAT,
		CORROSIVE,
		CORROSIVE_2,
		NO_CONTAINER,
		NO_ID,
		//valued traits: stored with a number in FluidProperties.traitValues; absent or zero means "not present"
		COMBUSTION_TU,
		COMBUSTION_HEAT;
	}
}
