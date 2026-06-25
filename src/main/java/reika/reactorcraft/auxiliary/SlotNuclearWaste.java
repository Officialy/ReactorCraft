/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import reika.reactorcraft.base.TileEntityWasteUnit;
import reika.reactorcraft.registry.ReactorItems;

/** 26.2 port: only accepts nuclear-waste items the waste unit will take ({@code isItemValid}→{@code mayPlace}). */
public class SlotNuclearWaste extends Slot {

	private final TileEntityWasteUnit tile;

	public SlotNuclearWaste(TileEntityWasteUnit te, int id, int x, int y)
	{
		super(te, id, x, y);
		tile = te;
	}

	@Override
	public final boolean mayPlace(ItemStack is)
	{
		return is.getItem() == ReactorItems.WASTE.getItemInstance() && tile.isItemValidForSlot(this.getSlotIndex(), is);
	}

}
