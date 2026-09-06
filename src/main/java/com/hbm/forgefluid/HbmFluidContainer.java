package com.hbm.forgefluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;

/**
 * One instance per container kind (canister, cell, gas canister). Each fluid declares its container membership once
 * through the {@link HbmFluid} builder (e.g. {@code .canister("canister_fuel")}); the mapping fluid -> container model,
 * the derived translation key and the baked-model cache used to be hand-written three times in three parallel enums.
 * Consumers (item name, creative tab, render, fill gating, client model baking) read this registry instead.
 */
public class HbmFluidContainer {

	public static final List<HbmFluidContainer> ALL = new ArrayList<HbmFluidContainer>();

	public static final HbmFluidContainer CANISTER = new HbmFluidContainer("canister_empty", () -> ModItems.canister_generic);
	public static final HbmFluidContainer CELL = new HbmFluidContainer("cell_empty", () -> ModItems.cell);
	public static final HbmFluidContainer GAS_CANISTER = new HbmFluidContainer("gas_empty", () -> ModItems.gas_canister);

	public static class Entry {
		public final Fluid fluid; //null for the empty entry
		public final ModelResourceLocation model;
		public final String translateKey;
		private IBakedModel baked;

		private Entry(Fluid fluid, ModelResourceLocation model) {
			this.fluid = fluid;
			this.model = model;
			this.translateKey = "item." + model.getPath() + ".name";
		}

		public IBakedModel getRenderModel() { return baked; }
		public void putRenderModel(IBakedModel baked) { this.baked = baked; }
	}

	private final Supplier<Item> item;
	private final Entry empty;
	private final List<Entry> entries = new ArrayList<Entry>(); //empty at index 0, then fluids in registration order
	private final Map<Fluid, Entry> byFluid = new HashMap<Fluid, Entry>();

	private HbmFluidContainer(String emptyModel, Supplier<Item> item) {
		this.item = item;
		this.empty = new Entry(null, rl(emptyModel));
		this.entries.add(empty);
		ALL.add(this);
	}

	private static ModelResourceLocation rl(String model) {
		return new ModelResourceLocation(RefStrings.MODID + ":" + model, "inventory");
	}

	//called from HbmFluid.build() once the fluid exists
	void register(Fluid fluid, String model) {
		Entry e = new Entry(fluid, rl(model));
		entries.add(e);
		byFluid.put(fluid, e);
	}

	public Item getItem() { return item.get(); }

	public boolean contains(Fluid f) { return f != null && byFluid.containsKey(f); }

	//empty entry for null, the fluid's entry otherwise, or null if the fluid has no such container
	public Entry getEntry(Fluid f) { return f == null ? empty : byFluid.get(f); }

	public List<Entry> getEntries() { return entries; } //includes the empty entry

	//matches the old getFluids(): entry order, null at index 0 for empty
	public Fluid[] getFluids() {
		Fluid[] arr = new Fluid[entries.size()];
		for(int i = 0; i < entries.size(); i++)
			arr[i] = entries.get(i).fluid;
		return arr;
	}
}
