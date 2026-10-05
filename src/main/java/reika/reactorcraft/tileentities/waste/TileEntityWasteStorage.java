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

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

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
import reika.reactorcraft.container.MenuWasteStorage;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
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
	public boolean hasGui() {
		return true;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new MenuWasteStorage(id, inv, this);
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (rand.nextInt(20) == 0)
			this.sickenMobs(world, pos);

		if (!world.isClientSide()) {
			this.decayWaste();
			this.feed();
		}

		if (world.dimension() == Level.NETHER || ReikaWorldHelper.getAmbientTemperatureAt(world, pos) > 100) {
			if (this.hasWaste()) {
				ReikaParticleHelper.SMOKE.spawnAroundBlock(world, pos, 3);
				if (rand.nextInt(4) == 0)
					ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.FIRE_EXTINGUISH);
				if (rand.nextInt(200) == 0) {
					world.removeBlock(pos, false);
					world.explode(null, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 4F, Level.ExplosionInteraction.BLOCK);
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
		if (ReikaInventoryHelper.isEmpty(itemHandler))
			ReactorAchievements.DECAY.triggerAchievement(this.getPlacer());
	}

	private void sickenMobs(Level world, BlockPos pos) {
		int r = this.getRange();
		AABB box = ReikaAABBHelper.getBlockAABB(pos).inflate(r, r, r);
		List<LivingEntity> li = world.getEntitiesOfClass(LivingEntity.class, box);
		for (LivingEntity e : li) {
			if (!RadiationIntensity.MODERATE.hasSufficientShielding(e)) {
				double dd = ReikaMathLibrary.py3d(e.getX()-pos.getX()-0.5, e.getY()-pos.getY()-0.5, e.getZ()-pos.getZ()-0.5);
				if (ReikaWorldHelper.canBlockSee(world, pos.getX(), pos.getY(), pos.getZ(), e.getX(), e.getY(), e.getZ(), dd)) {
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
		return itemHandler.getStackInSlot(slot).isEmpty();
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
		return isLongLivedWaste(i);
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

	public final int getInventoryStackLimit() {
		return 16;
	}

	public boolean feed() {
		BlockEntity tile = this.getAdjacentBlockEntity(Direction.DOWN);
		if (tile instanceof TileEntityWasteStorage) {
			if (((Feedable)tile).feedIn(itemHandler.getStackInSlot(itemHandler.getSlots()-1))) {
				for (int i = itemHandler.getSlots()-1; i > 0; i--)
					itemHandler.setStackInSlot(i, itemHandler.getStackInSlot(i-1));

				tile = this.getAdjacentBlockEntity(Direction.UP);
				if (tile instanceof TileEntityWasteStorage) {
					itemHandler.setStackInSlot(0, ((Feedable) tile).feedOut());
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
				ItemStack b = itemHandler.getStackInSlot(k-1);
				if (!b.isEmpty()) {
					ItemStack a = itemHandler.getStackInSlot(k);
					if (a.isEmpty()) {
						itemHandler.setStackInSlot(k, b);
						itemHandler.setStackInSlot(k-1, ItemStack.EMPTY);
					}
					else if (ReikaItemHelper.matchStacks(a, b) && a.getCount()+b.getCount() <= Math.min(this.getInventoryStackLimit(), a.getMaxStackSize())) {
						itemHandler.setStackInSlot(k, a.copyWithCount(a.getCount()+b.getCount()));
						itemHandler.setStackInSlot(k-1, ItemStack.EMPTY);
					}
				}
			}
		}
	}

	@Override
	public boolean feedIn(ItemStack is) {
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

	@Override
	public ItemStack feedOut() {
		ItemStack last = itemHandler.getStackInSlot(itemHandler.getSlots()-1);
		if (last.isEmpty())
			return ItemStack.EMPTY;
		ItemStack is = last.copy();
		itemHandler.setStackInSlot(itemHandler.getSlots()-1, ItemStack.EMPTY);
		return is;
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
