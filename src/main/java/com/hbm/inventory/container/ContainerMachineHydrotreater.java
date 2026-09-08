package com.hbm.inventory.container;

import com.hbm.inventory.SlotMachineOutput;
import com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

//Ported from NTM:CE, layout adapted to this fork's slot count (10)
public class ContainerMachineHydrotreater extends Container {

	private TileEntityMachineHydrotreater hydrotreater;

	public ContainerMachineHydrotreater(InventoryPlayer invPlayer, TileEntityMachineHydrotreater te) {

		hydrotreater = te;

		//Battery - seated under the energy bar (round socket in the new GUI art)
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 0, 17, 90));
		//Feedstock (water) Input/Output
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 1, 35, 90));
		this.addSlotToContainer(new SlotMachineOutput(te.inventory, 2, 35, 108));
		//Hydrogen Input/Output
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 3, 53, 90));
		this.addSlotToContainer(new SlotMachineOutput(te.inventory, 4, 53, 108));
		//Product Input/Output
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 5, 125, 90));
		this.addSlotToContainer(new SlotMachineOutput(te.inventory, 6, 125, 108));
		//Byproduct Input/Output
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 7, 143, 90));
		this.addSlotToContainer(new SlotMachineOutput(te.inventory, 8, 143, 108));
		//Screwdriver (catalyst substitute) - centered "C" gauge socket in the new GUI art
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 9, 89, 36));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 156 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 214));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack ret = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if(slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			ret = stack.copy();

			if(index <= 9) {
				if(!this.mergeItemStack(stack, 10, this.inventorySlots.size(), true))
					return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 0, 1, false))
				if(!this.mergeItemStack(stack, 1, 2, false))
					if(!this.mergeItemStack(stack, 3, 4, false))
						if(!this.mergeItemStack(stack, 5, 6, false))
							if(!this.mergeItemStack(stack, 7, 8, false))
								if(!this.mergeItemStack(stack, 9, 10, false))
									return ItemStack.EMPTY;

			if(stack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return ret;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return hydrotreater.isUseableByPlayer(player);
	}
}
