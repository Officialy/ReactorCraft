/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.processing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.Isotopes;
import reika.dragonapi.libraries.mathsci.Isotopes.DecayData;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.modregistry.ModOreList;
import reika.reactorcraft.auxiliary.ReactorCoreTE;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.base.TileEntityInventoriedReactorBase;
import reika.reactorcraft.base.TileEntityWasteUnit;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorTiles;

public class TileEntityWasteDecayer extends TileEntityInventoriedReactorBase implements ReactorCoreTE {

	public TileEntityWasteDecayer(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.WASTEDECAYER.get(), pos, state);
	}

	public static final int BASE_TEMP = 150;
	public static final int OPTIMAL_TEMP = 400;

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.WASTEDECAYER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (!world.isClientSide()) {
			this.feed();
		}

		thermalTicker.update();
		if (thermalTicker.checkCap()) {
			this.updateTemperature(world, pos);
		}
	}

	private boolean feed() {
		BlockEntity tile = this.getAdjacentBlockEntity(Direction.DOWN);
		if (tile instanceof TileEntityWasteDecayer) {
			if (((TileEntityWasteDecayer)tile).feedIn(itemHandler.getStackInSlot(itemHandler.getSlots()-1))) {
				for (int i = itemHandler.getSlots()-1; i > 0; i--)
					itemHandler.setStackInSlot(i, itemHandler.getStackInSlot(i-1));

				tile = this.getAdjacentBlockEntity(Direction.UP);
				if (tile instanceof TileEntityWasteDecayer) {
					itemHandler.setStackInSlot(0, ((TileEntityWasteDecayer) tile).feedOut());
				}
				else
					itemHandler.setStackInSlot(0, ItemStack.EMPTY);
			}
		}
		this.collapseInventory();
		return false;
	}

	private void collapseInventory() {
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			for (int k = itemHandler.getSlots()-1; k > 0; k--) {
				ItemStack a = itemHandler.getStackInSlot(k);
				ItemStack b = itemHandler.getStackInSlot(k-1);
				if (a.isEmpty() && !b.isEmpty()) {
					itemHandler.setStackInSlot(k, b);
					itemHandler.setStackInSlot(k-1, ItemStack.EMPTY);
					return;
				}
				else if (ReikaItemHelper.areStacksCombinable(a, b, Integer.MAX_VALUE) && a.getCount() < Math.min(a.getMaxStackSize(), this.getInventoryStackLimit())) {
					itemHandler.setStackInSlot(k, a.copyWithCount(a.getCount()+1));
					ReikaInventoryHelper.decrStack(k-1, itemHandler);
					return;
				}
			}
		}
	}

	private boolean feedIn(ItemStack is) {
		if (is.isEmpty())
			return true;
		if (!this.isItemValidForSlot(0, is))
			return false;
		if (itemHandler.getStackInSlot(0).isEmpty()) {
			itemHandler.setStackInSlot(0, is.copy());
			return true;
		}
		return false;
	}

	private ItemStack feedOut() {
		ItemStack last = itemHandler.getStackInSlot(itemHandler.getSlots()-1);
		if (last.isEmpty())
			return ItemStack.EMPTY;
		ItemStack is = last.copy();
		itemHandler.setStackInSlot(itemHandler.getSlots()-1, ItemStack.EMPTY);
		return is;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		NeutronType type = e.getNeutronType();
		if (!world.isClientSide() && type.canIrradiateMaterials() && ReikaRandomHelper.doWithChance(50)) {
			if (ReikaRandomHelper.doWithChance(this.getDecayChance()*e.getNeutronSpeed().getWasteConversionMultiplier()))
				this.tryDecay();
			return true;
		}
		return false;
	}

	private double getDecayChance() {
		return temperature < BASE_TEMP ? ReikaMathLibrary.linterpolate(temperature, 20, BASE_TEMP, 0, 5) : ReikaMathLibrary.linterpolate(temperature, BASE_TEMP, OPTIMAL_TEMP, 5, 25);
	}

	private boolean tryDecay() {
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack is = itemHandler.getStackInSlot(i);
			if (!is.isEmpty() && TileEntityWasteUnit.isLongLivedWaste(is)) {
				if (this.tryDecay(i, is)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean tryDecay(int i, ItemStack is) {
		DecayData split = Isotopes.getIsotope(is.getDamageValue()).getDecay();
		if (split == null) {
			ReikaInventoryHelper.decrStack(i, itemHandler);
			return true;
		}
		int amt = Mth.floor(split.amount);
		if (ReikaRandomHelper.doWithChance(split.amount-amt))
			amt++;
		if (amt == 0)
			return false;
		ItemStack add = this.getItem(split, amt);
		if (add == null) {
			return true;
		}
		if (ReikaInventoryHelper.addToIInv(add, this)) {
			ReikaInventoryHelper.decrStack(i, itemHandler);
			return true;
		}
		return false;
	}

	private ItemStack getItem(DecayData split, int amt) {
		if (split.isotope.getChemicalSymbol().equalsIgnoreCase("pb"))
			return ModOreList.LEAD.existsInGame() ? ModOreList.LEAD.getFirstOreBlock() : null;
		else if (split.isotope instanceof Isotopes) {
			ItemStack s = ReactorItems.WASTE.getStackOfMetadata(((Isotopes)split.isotope).ordinal());
			s.setCount(amt);
			return s;
		}
		else
			return null;
	}

	@Override
	public final int getTextureState(Direction side) {
		if (side.getStepY() != 0)
			return 4;
		ReactorTiles src = this.getTile();
		ReactorTiles r = ReactorTiles.getTE(level, this.getBlockPos().below());
		ReactorTiles r2 = ReactorTiles.getTE(level, this.getBlockPos().above());
		if (r2 == src && r == src)
			return 2;
		else if (r2 == src)
			return 1;
		else if (r == src)
			return 3;
		return 0;
	}

	@Override
	public int getContainerSize() {
		return 15;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack is) {
		return ReactorItems.WASTE.matchWith(is);
	}

	@Override
	public boolean canItemEnterFromSide(Direction dir) {
		return dir == Direction.UP;
	}

	@Override
	public boolean canItemExitToSide(Direction dir) {
		return dir == Direction.DOWN;
	}

	@Override
	public boolean canRemoveItem(int slot, ItemStack is) {
		return !TileEntityWasteUnit.isLongLivedWaste(is);
	}

	public int getInventoryStackLimit() {
		return 8;
	}

}
