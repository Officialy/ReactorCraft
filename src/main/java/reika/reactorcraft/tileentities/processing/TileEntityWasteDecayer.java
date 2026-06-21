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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.Isotopes;
import reika.dragonapi.libraries.mathsci.isotopes.DecayData;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.modregistry.ModOreList;
import reika.reactorcraft.auxiliary.ReactorCoreTE;
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
			this.updateTemperature(world, x, y, z);
		}
	}

	private boolean feed() {
		Level world = level;
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		Block id = world.getBlock(x, y-1, z);
		int meta = world.getBlockMetadata(x, y-1, z);
		BlockEntity tile = this.getAdjacentTileEntity(Direction.DOWN);
		if (tile instanceof TileEntityWasteDecayer) {
			if (((TileEntityWasteDecayer)tile).feedIn(itemHandler.getStackInSlot(itemHandler.getSlots()-1))) {
				for (int i = itemHandler.getSlots()-1; i > 0; i--)
					itemHandler.getStackInSlot(i) = itemHandler.getStackInSlot(i-1);

				id = world.getBlock(x, y+1, z);
				meta = world.getBlockMetadata(x, y+1, z);
				tile = this.getAdjacentTileEntity(Direction.UP);
				if (tile instanceof TileEntityWasteDecayer) {
					itemHandler.getStackInSlot(0) = ((TileEntityWasteDecayer) tile).feedOut();
				}
				else
					itemHandler.getStackInSlot(0) = ItemStack.EMPTY;
			}
		}
		this.collapseInventory();
		return false;
	}

	private void collapseInventory() {
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			for (int k = itemHandler.getSlots()-1; k > 0; k--) {
				if (itemHandler.getStackInSlot(k) == null && itemHandler.getStackInSlot(k-1) != null) {
					itemHandler.getStackInSlot(k) = itemHandler.getStackInSlot(k-1);
					itemHandler.getStackInSlot(k-1) = ItemStack.EMPTY;
					return;
				}
				else if (ReikaItemHelper.areStacksCombinable(itemHandler.getStackInSlot(k), itemHandler.getStackInSlot(k-1), Integer.MAX_VALUE) && itemHandler.getStackInSlot(k).getCount() < Math.min(itemHandler.getStackInSlot(k).getMaxStackSize(), this.getInventoryStackLimit())) {
					itemHandler.getStackInSlot(k).getCount()++;
					ReikaInventoryHelper.decrStack(k-1, inv);
					return;
				}
			}
		}
	}

	private boolean feedIn(ItemStack is) {
		if (is == null)
			return true;
		if (!this.isItemValidForSlot(0, is))
			return false;
		if (itemHandler.getStackInSlot(0) == null) {
			itemHandler.getStackInSlot(0) = is.copy();
			return true;
		}
		return false;
	}

	private ItemStack feedOut() {
		if (itemHandler.getStackInSlot(itemHandler.getSlots()-1) == null)
			return null;
		else {
			ItemStack is = itemHandler.getStackInSlot(itemHandler.getSlots()-1).copy();
			itemHandler.getStackInSlot(itemHandler.getSlots()-1) = ItemStack.EMPTY;
			return is;
		}
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
		NeutronType type = e.getType();
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
			if (is != null && TileEntityWasteUnit.isLongLivedWaste(is)) {
				//ReikaJavaLibrary.pConsole("Trying to decay "+Isotopes.getIsotope(is.getDamageValue())+" in slot #"+i);
				if (this.tryDecay(i, is)) {
					//ReikaJavaLibrary.pConsole("Success");
					return true;
				}
			}
		}
		return false;
	}

	private boolean tryDecay(int i, ItemStack is) {/*
		int slot = is.getCount() == 1 ? i : ReikaInventoryHelper.findEmptySlot(inv);
		if (slot >= 0) {
			if (is.getCount() > 1)
				is.getCount()--;
			itemHandler.getStackInSlot(slot) = ReikaItemHelper.getSizedItemStack(Isotopes.getIsotope(is.getDamageValue()).getForcedFissionProduct(), 2);
			return true;
		}
		return false;*/
		DecayData split = Isotopes.getIsotope(is.getDamageValue()).getDecay();
		if (split == null) {
			ReikaInventoryHelper.decrStack(i, inv);
			//ReikaJavaLibrary.pConsole("Decayed into nothing");
			return true;
		}
		int amt = Mth.floor_double(split.amount);
		if (ReikaRandomHelper.doWithChance(split.amount-amt))
			amt++;
		if (amt == 0)
			return false;
		ItemStack add = this.getItem(split, amt);
		if (add == null) {
			return true;
		}
		//ReikaJavaLibrary.pConsole("Decayed into "+split.isotope+" x"+add.getCount());
		if (ReikaInventoryHelper.addToIInv(add, this)) {
			ReikaInventoryHelper.decrStack(i, inv);
			return true;
		}
		return false;
	}

	private ItemStack getItem(DecayData split, int amt) {
		if (split.isotope.getChemicalSymbol().equalsIgnoreCase("pb"))
			return ModOreList.LEAD.existsInGame() ? ModOreList.LEAD.getFirstProduct() : null;
		else if (split.isotope instanceof Isotopes)
			return new ItemStack(ReactorItems.WASTE.getItemInstance(), amt, ((Isotopes)split.isotope).ordinal());
		else
			return null;
	}

	@Override
	public final int getTextureState(Direction side) {
		if (side.offsetY != 0)
			return 4;
		Level world = level;
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		ReactorTiles src = this.getTile();
		ReactorTiles r = ReactorTiles.getTE(world, x, y-1, z);
		ReactorTiles r2 = ReactorTiles.getTE(world, x, y+1, z);
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

	@Override
	public int getInventoryStackLimit() {
		return 8;
	}

}
