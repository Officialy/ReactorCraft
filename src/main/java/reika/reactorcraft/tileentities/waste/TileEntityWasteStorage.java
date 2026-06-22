/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.waste;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.libraries.ReikaAABBHelper;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.mathsci.Isotopes;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.Feedable;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.base.TileEntityWasteUnit;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.RangedEffect;

public class TileEntityWasteStorage extends TileEntityWasteUnit implements RangedEffect, Feedable {
	public TileEntityWasteStorage(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.STORAGE.get(), pos, state);
	}


	@Override
	public int getContainerSize() {
		return 12;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (rand.nextInt(20) == 0)
			this.sickenMobs(world, x, y, z);

		if (!world.isClientSide()) {
			this.decayWaste();
			this.feed();
		}

		if (world.provider.isHellWorld || ReikaWorldHelper.getAmbientTemperatureAt(world, x, y, z) > 100) {
			if (this.hasWaste()) {
				ReikaParticleHelper.SMOKE.spawnAroundBlock(world, x, y, z, 3);
				if (rand.nextInt(4) == 0)
					ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.fizz");
				if (rand.nextInt(200) == 0) {
					world.removeBlock(x, y, z);
					world.newExplosion(null, x+0.5, y+0.5, y+0.5, 4F, true, true);
				}
			}
		}
	}

	@Override
	protected boolean accountForOutGameTime() {
		return true;
	}

	@Override
	protected void onDecayWaste(int i) {
		super.onDecayWaste(i);
		if (ReikaInventoryHelper.isEmpty(this))
			ReactorAchievements.DECAY.triggerAchievement(this.getPlacer());
	}

	private void sickenMobs(Level world, int x, int y, int z) {
		int r = this.getRange();
		AABB box = ReikaAABBHelper.getBlockAABB(x, y, z).expand(r, r, r);
		List<LivingEntity> li = world.getEntitiesWithinAABB(LivingEntity.class, box);
		for (LivingEntity e : li) {
			if (!RadiationIntensity.MODERATE.hasSufficientShielding(e)) {
				double dd = ReikaMathLibrary.py3d(e.posX-x-0.5, e.posY-y-0.5, e.posZ-z-0.5);
				if (ReikaWorldHelper.canBlockSee(world, x, y, z, e.posX, e.posY, e.posZ, dd)) {
					RadiationEffects.instance.applyEffects(e, RadiationIntensity.MODERATE);
				}
			}
		}
	}

	@Override
	protected boolean isValidSlot(int i, ItemStack is) {
		return this.isAppropriateWasteSlot(is, i);
	}

	private boolean isAppropriateWasteSlot(ItemStack is, int slot) {
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack in = itemHandler.getStackInSlot(i);
			if (ReikaItemHelper.matchStacks(is, in)) {
				if (in.getCount()+is.getCount() <= Math.min(this.getInventoryStackLimit(), is.getMaxStackSize())) {
					return i == slot;
				}
			}
		}
		return itemHandler.getStackInSlot(slot) == ItemStack.EMPTY;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.STORAGE;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public boolean leaksRadiation() {
		return false;
	}

	@Override
	public boolean canRemoveItem(int slot, ItemStack is) {
		return false;
	}

	@Override
	public boolean isValidIsotope(Isotopes i) {
		return this.isLongLivedWaste(i);//i.getMCHalfLife() > ReikaTimeHelper.YEAR.getMinecraftDuration();
	}

	@Override
	public int getRange() {
		int amt = this.countWaste();
		return this.getRangeFromWasteCount(amt);
	}

	@Override
	public int getMaxRange() {
		int amt = this.getContainerSize();
		return this.getRangeFromWasteCount(amt);
	}

	public int getRangeFromWasteCount(int amt) {
		return (int)Math.sqrt(amt);
	}

	@Override
	public final int getInventoryStackLimit() {
		return 16;
	}

	public boolean feed() {
		Level world = level;
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		Block id = world.getBlock(x, y-1, z);
		int meta = world.getBlockMetadata(x, y-1, z);
		BlockEntity tile = this.getAdjacentTileEntity(Direction.DOWN);
		if (tile instanceof TileEntityWasteStorage) {
			if (((Feedable)tile).feedIn(itemHandler.getStackInSlot(itemHandler.getSlots()-1))) {
				for (int i = itemHandler.getSlots()-1; i > 0; i--)
					itemHandler.getStackInSlot(i) = itemHandler.getStackInSlot(i-1);

				id = world.getBlock(x, y+1, z);
				meta = world.getBlockMetadata(x, y+1, z);
				tile = this.getAdjacentTileEntity(Direction.UP);
				if (tile instanceof TileEntityWasteStorage) {
					itemHandler.getStackInSlot(0) = ((Feedable) tile).feedOut();
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
				if (itemHandler.getStackInSlot(k-1) != null) {
					if (itemHandler.getStackInSlot(k) == null) {
						itemHandler.getStackInSlot(k) = itemHandler.getStackInSlot(k-1);
						itemHandler.getStackInSlot(k-1) = ItemStack.EMPTY;
					}
					else if (ReikaItemHelper.matchStacks(itemHandler.getStackInSlot(k), itemHandler.getStackInSlot(k-1)) && ItemStack.areItemStackTagsEqual(itemHandler.getStackInSlot(k), itemHandler.getStackInSlot(k-1)) && itemHandler.getStackInSlot(k).getCount()+itemHandler.getStackInSlot(k-1).getCount() <= Math.min(this.getInventoryStackLimit(), itemHandler.getStackInSlot(k).getMaxStackSize())) {
						itemHandler.getStackInSlot(k).getCount() += itemHandler.getStackInSlot(k-1).getCount();
						itemHandler.getStackInSlot(k-1) = ItemStack.EMPTY;
					}
				}
			}
		}
	}

	@Override
	public boolean feedIn(ItemStack is) {
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

	@Override
	public ItemStack feedOut() {
		if (itemHandler.getStackInSlot(itemHandler.getSlots()-1) == null)
			return null;
		else {
			ItemStack is = itemHandler.getStackInSlot(itemHandler.getSlots()-1).copy();
			itemHandler.getStackInSlot(itemHandler.getSlots()-1) = ItemStack.EMPTY;
			return is;
		}
	}

	@Override
	protected boolean canBeAccelerated() {
		return true;
	}

	@Override
	protected double getBaseDecayRate() {
		return 1.75;
	}

}
