/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fission;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.base.TileEntityNuclearCore;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorFuel;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;

public class TileEntityFuelRod extends TileEntityNuclearCore {
	public TileEntityFuelRod(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.FUEL.get(), pos, state);
	}


	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}
	/*
	private int getSameCoreHeatConductionFraction() {
		return 12;
	}
	 */

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.FUEL;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack is) {
		if (itemHandler.getStackInSlot(i) != null)
			return false;
		if (this.isFuel(is))
			return i < 4;
		if (is.getItem() == ReactorItems.DEPLETED.getItemInstance())
			return i < 4;
		return false;
	}

	private boolean isFuel(ItemStack is) {
		if (is.getItem() == ReactorItems.FUEL.getItemInstance())
			return true;
		//if (is.getItem() == ReactorItems.THORIUM.getItemInstance())
		//	return true;
		if (is.getItem() == ReactorItems.PLUTONIUM.getItemInstance())
			return true;
		return false;
	}

	@Override
	public boolean canRemoveItem(int i, ItemStack is) {
		if (is.getItem() == ReactorItems.WASTE.getItemInstance())
			return true;
		if (is.getItem() == ReactorItems.DEPLETED.getItemInstance())
			return true;
		return false;
	}

	private ReactorFuel getFuel() {
		return ReactorFuel.getFrom(itemHandler.getStackInSlot(3));
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		super.onNeutron(e, world, x, y, z);
		if (!world.isClientSide()) {
			if (e.getType().canTriggerFission() && ReikaRandomHelper.doWithChance(e.getNeutronSpeed().getInteractionMultiplier())) {
				if (this.checkPoisonedChance())
					return true;
				if (this.isFissile()) {
					ReactorFuel f = this.getFuel();
					if (ReikaRandomHelper.doWithChance(f.fissionChance+f.voidCoefficient*(temperature-100))) {
						ReactorAchievements.FISSION.triggerAchievement(this.getPlacer());
						if (ReikaRandomHelper.doWithChance(f.consumeChance)) {
							ItemStack is = itemHandler.getStackInSlot(3);
							itemHandler.getStackInSlot(3) = f.getFissionProduct(is);
							if (itemHandler.getStackInSlot(3) != null && itemHandler.getStackInSlot(3).getItem() != is.getItem())
								this.tryPushSpentFuel(3);
							if (ReikaRandomHelper.doWithChance(f.wasteChance))
								this.addWaste();
						}
						this.spawnNeutronBurst(world, x, y, z);
						temperature += f.temperatureStep;
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean isFissile() {
		return this.getFuel() != ItemStack.EMPTY;
	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return liq.isWater();
	}

	@Override
	public ReactorType getReactorType() {
		return ReactorType.FISSION;
	}
}
