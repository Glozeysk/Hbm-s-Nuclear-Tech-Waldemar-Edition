package com.hbm.tileentity.machine.oil;

import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.inventory.HydrotreaterRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.packet.AuxElectricityPacket;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Tuple.Triplet;

import api.hbm.energy.IEnergyUser;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//Ported from NTM:CE (Warfactory-Official/Hbm-s-Nuclear-Tech-CE): TileEntityMachineHydrotreater, adapted to this
//fork's Forge fluid system (no FluidTankNTM/pressure) and its own recipe list (HydrotreaterRecipes).
//Catalyst slot substitutes CE's catalytic_converter item with a screwdriver, per project decision.
public class TileEntityMachineHydrotreater extends TileEntityMachineBase implements ITickable, IEnergyUser, IFluidHandler, ITankPacketAcceptor {

	public static final long maxPower = 1_000_000;
	public long power;

	public FluidTank[] tanks;
	public Fluid[] tankTypes;

	public TileEntityMachineHydrotreater() {
		super(10);
		tanks = new FluidTank[4];
		tankTypes = new Fluid[] {null, ModForgeFluids.hydrogen, null, null};

		tanks[0] = new FluidTank(64000); //feedstock in
		tanks[1] = new FluidTank(ModForgeFluids.hydrogen, 0, 64000); //hydrogen in
		tanks[2] = new FluidTank(24000); //product out
		tanks[3] = new FluidTank(24000); //byproduct out
	}

	public String getName() {
		return "container.hydrotreater";
	}

	@Override
	public void update() {
		if(!world.isRemote) {

			this.updateConnections();
			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

			if(this.inputValidForTank(1))
				FFUtils.fillFromFluidContainer(inventory, tanks[0], 1, 2);
			FFUtils.fillFromFluidContainer(inventory, tanks[1], 3, 4);

			reform();

			FFUtils.fillFluidContainer(inventory, tanks[2], 5, 6);
			FFUtils.fillFluidContainer(inventory, tanks[3], 7, 8);

			detectAndSendChanges();
		}
	}

	private void reform() {
		Triplet<FluidStack, FluidStack, FluidStack> recipe = HydrotreaterRecipes.getRecipe(tankTypes[0]);

		if(recipe == null) {
			setTankType(2, null);
			setTankType(3, null);
			return;
		}

		setTankType(2, recipe.getY().getFluid());
		setTankType(3, recipe.getZ().getFluid());

		if(power < 20_000)
			return;
		if(tanks[0].getFluidAmount() < 1000)
			return;
		if(tanks[1].getFluidAmount() < recipe.getX().amount)
			return;
		if(inventory.getStackInSlot(9).isEmpty() || inventory.getStackInSlot(9).getItem() != ModItems.catalyst_cobalt)
			return;
		if(tanks[2].getFluidAmount() + recipe.getY().amount > tanks[2].getCapacity())
			return;
		if(tanks[3].getFluidAmount() + recipe.getZ().amount > tanks[3].getCapacity())
			return;

		tanks[0].drain(1000, true);
		tanks[1].drain(recipe.getX().amount, true);
		tanks[2].fill(recipe.getY().copy(), true);
		tanks[3].fill(recipe.getZ().copy(), true);
		power -= 20_000;
	}

	//The 4 dummy ports sit at the diagonal corners of the 3x3 base (pos +-1,0,+-1), each exposing 2 cardinal faces
	//to open air - that's where a wire physically touches. Direct-adjacent batteries push power without this (hence
	//"works touching, not through a wire"), but HBM's wire network only routes to tiles that actively subscribe.
	private void updateConnections() {
		this.trySubscribe(world, pos.add(2, 0, 1), ForgeDirection.EAST);
		this.trySubscribe(world, pos.add(1, 0, 2), ForgeDirection.SOUTH);
		this.trySubscribe(world, pos.add(2, 0, -1), ForgeDirection.EAST);
		this.trySubscribe(world, pos.add(1, 0, -2), ForgeDirection.NORTH);
		this.trySubscribe(world, pos.add(-2, 0, 1), ForgeDirection.WEST);
		this.trySubscribe(world, pos.add(-1, 0, 2), ForgeDirection.SOUTH);
		this.trySubscribe(world, pos.add(-2, 0, -1), ForgeDirection.WEST);
		this.trySubscribe(world, pos.add(-1, 0, -2), ForgeDirection.NORTH);
	}

	private boolean inputValidForTank(int slot) {
		if(!inventory.getStackInSlot(slot).isEmpty()) {
			FluidStack containerFluid = FluidUtil.getFluidContained(inventory.getStackInSlot(slot));
			if(containerFluid != null && HydrotreaterRecipes.getRecipe(containerFluid.getFluid()) != null) {
				tankTypes[0] = containerFluid.getFluid();
				return true;
			}
		}
		return false;
	}

	public void setTankType(int idx, Fluid type) {
		if(tankTypes[idx] != type) {
			tankTypes[idx] = type;
			tanks[idx].setFluid(type != null ? new FluidStack(type, tanks[idx].getFluidAmount()) : null);
		}
	}

	private long detectPower;
	private FluidTank[] detectTanks = new FluidTank[] {null, null, null, null};
	private int syncTick = 0;

	private void detectAndSendChanges() {
		boolean mark = false;
		if(detectPower != power) {
			mark = true;
			detectPower = power;
		}
		for(int i = 0; i < tanks.length; i++) {
			if(!FFUtils.areTanksEqual(tanks[i], detectTanks[i])) {
				mark = true;
				detectTanks[i] = FFUtils.copyTank(tanks[i]);
			}
		}
		syncTick++;
		if(syncTick >= 5) {
			syncTick = 0;
			PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos.getX(), pos.getY(), pos.getZ(), tanks), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 20));
		}
		if(mark) {
			PacketDispatcher.wrapper.sendToAllAround(new AuxElectricityPacket(pos.getX(), pos.getY(), pos.getZ(), power), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 20));
			markDirty();
		}
	}

	@Override
	public void recievePacket(NBTTagCompound[] tags) {
		if(tags.length != 4)
			return;
		for(int i = 0; i < tanks.length; i++)
			tanks[i].readFromNBT(tags[i]);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if(nbt.hasKey("f0"))
			tankTypes[0] = FluidRegistry.getFluid(nbt.getString("f0"));
		power = nbt.getLong("power");
		if(nbt.hasKey("tanks"))
			FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if(tankTypes[0] != null)
			nbt.setString("f0", tankTypes[0].getName());
		nbt.setLong("power", power);
		nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));
		return super.writeToNBT(nbt);
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, int amount) {
		return i == 2 || i == 4 || i == 6 || i == 8;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] {tanks[0].getTankProperties()[0], tanks[1].getTankProperties()[0], tanks[2].getTankProperties()[0], tanks[3].getTankProperties()[0]};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(resource == null)
			return 0;
		if(tankTypes[0] != null && resource.getFluid() == tankTypes[0])
			return tanks[0].fill(resource, doFill);
		if(tanks[0].getFluidAmount() == 0 && HydrotreaterRecipes.getRecipe(resource.getFluid()) != null) {
			tankTypes[0] = resource.getFluid();
			this.markDirty();
			return tanks[0].fill(resource, doFill);
		}
		if(resource.getFluid() == ModForgeFluids.hydrogen)
			return tanks[1].fill(resource, doFill);
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if(resource == null)
			return null;
		if(resource.isFluidEqual(tanks[2].getFluid()))
			return tanks[2].drain(resource.amount, doDrain);
		if(resource.isFluidEqual(tanks[3].getFluid()))
			return tanks[3].drain(resource.amount, doDrain);
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if(tanks[2].getFluid() != null)
			return tanks[2].drain(maxDrain, doDrain);
		if(tanks[3].getFluid() != null)
			return tanks[3].drain(maxDrain, doDrain);
		return null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		return super.getCapability(capability, facing);
	}
}
