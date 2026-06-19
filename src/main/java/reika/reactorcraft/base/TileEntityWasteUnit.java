/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.Isotopes;
import reika.dragonapi.libraries.mathsci.ReikaNuclearHelper;
import reika.dragonapi.libraries.mathsci.ReikaTimeHelper;
import reika.reactorcraft.auxiliary.WasteManager;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorItems;

public abstract class TileEntityWasteUnit extends TileEntityInventoriedReactorBase {

	private long lastTickTime = -1;

	public TileEntityWasteUnit(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	// CHROMA-PORT: ChromatiCraft adjacency-upgrade decay acceleration (registerAdjacency +
	// the Chroma branch of getAcceleratorBoost) is gated out — ChromatiCraft is not in this
	// build, so getAcceleratorBoost falls back to its no-Chroma value of 1 exactly as the
	// original did when ChromatiCraft was absent.

	protected void fill() {
		for (int i = 0; i < this.getContainerSize(); i++) {
			if (this.getItem(i).isEmpty()) {
				ItemStack is = WasteManager.getFullyRandomWasteItem();
				this.setItem(i, is);
			}
		}
	}

	public abstract boolean leaksRadiation();

	public abstract boolean isValidIsotope(Isotopes i);

	protected abstract boolean canBeAccelerated();

	protected abstract double getBaseDecayRate();

	private double getAccelerationFactor() {
		double base = this.getBaseDecayRate();
		if (this.canBeAccelerated())
			base = Math.pow(base, this.getAcceleratorBoost());
		return base*192;
	}

	private double getAcceleratorBoost() {
		return 1;
	}

	protected final void decayWaste() {
		double mult = this.getAccelerationFactor();
		if (this.accountForOutGameTime())
			mult *= (1+this.getSkippedTicks());
		for (int i = 0; i < this.getContainerSize(); i++) {
			ItemStack s = this.getItem(i);
			if (!s.isEmpty() && s.getItem() == ReactorItems.WASTE.getItemInstance()) {
				Isotopes atom = Isotopes.getIsotope(s.getDamageValue());
				if (ReikaRandomHelper.doWithChance(mult/this.getBaseDecayRate()*0.5*ReikaNuclearHelper.getDecayChanceFromHalflife(Math.log(atom.getMCHalfLife())))) {
					if (this.leaksRadiation() && rand.nextBoolean())
						this.leakRadiation(level, getBlockPos());
				}
				if (ReikaNuclearHelper.shouldDecay(atom, mult)) {
					ReikaInventoryHelper.decrStack(i, this, Math.max(1, s.getCount()/2));
					this.onDecayWaste(i);
				}
			}
		}
	}

	protected abstract boolean accountForOutGameTime();

	private long getSkippedTicks() { //compensate for lag + make decay effectively run even with MC closed
		long time = System.currentTimeMillis();
		long dur = time-lastTickTime;
		long ticks = 0;
		if (dur > 50) {
			ticks = (dur/50)-1;
		}
		lastTickTime = time;
		return ticks;
	}

	protected void onDecayWaste(int i) {

	}

	protected void leakRadiation(Level world, BlockPos pos) {
		Direction dir = dirs[rand.nextInt(dirs.length)];
		if (!world.isClientSide())
			world.addFreshEntity(new EntityNeutron(world, pos, dir, NeutronType.WASTE));
	}

	@Override
	public final boolean isItemValidForSlot(int i, ItemStack is) {
		return is.getItem() == ReactorItems.WASTE.getItemInstance() && is.getDamageValue() < 1000 && this.isValidIsotope(Isotopes.getIsotope(is.getDamageValue())) && this.isValidSlot(i, is);
	}

	protected boolean isValidSlot(int i, ItemStack is) {
		return true;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public final boolean canItemEnterFromSide(Direction dir) {
		return true;
	}

	@Override
	public final boolean canItemExitToSide(Direction dir) {
		return true;
	}

	public final int countWaste() {
		int count = 0;
		for (int i = 0; i < this.getContainerSize(); i++) {
			ItemStack s = this.getItem(i);
			if (!s.isEmpty() && s.getItem() == ReactorItems.WASTE.getItemInstance()) {
				count += s.getCount();
			}
		}
		return count;
	}

	public final boolean hasWaste() {
		return this.countWaste() > 0;
	}

	public static double getHalfLife(ItemStack is) {
		if (is.getItem() != ReactorItems.WASTE.getItemInstance())
			return 0;
		return Isotopes.getIsotope(is.getDamageValue()).getMCHalfLife();
	}

	public static boolean isLongLivedWaste(ItemStack is) {
		return is.getItem() == ReactorItems.WASTE.getItemInstance() && getHalfLife(is) > 6*ReikaTimeHelper.YEAR.getMinecraftDuration();
	}

	public static boolean isLongLivedWaste(Isotopes i) {
		return i.getMCHalfLife() > 6*ReikaTimeHelper.YEAR.getMinecraftDuration();
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);
		lastTickTime = NBT.getLongOr("lasttime", -1);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);
		NBT.putLong("lasttime", lastTickTime);
	}
}
