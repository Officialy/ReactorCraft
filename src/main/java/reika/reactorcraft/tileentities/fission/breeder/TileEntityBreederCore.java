/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fission.breeder;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.base.TileEntityNuclearCore;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;

public class TileEntityBreederCore extends TileEntityNuclearCore {
	public TileEntityBreederCore(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.BREEDER.get(), pos, state);
	}


	private final StepTimer timer2 = new StepTimer(10);

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		//ReikaJavaLibrary.pConsole(temperature+":"+this, temperature > 700);

		if (DragonAPI.debugtest) {
			ReikaInventoryHelper.clearInventory(this);
			ReikaInventoryHelper.addToIInv(ReactorItems.BREEDERFUEL.getStackOf(), this);
		}

		timer2.update();

		if (timer2.checkCap()) {
			for (int i = 2; i < 6; i++) {
				Direction dir = dirs[i];
				BlockPos p = pos.relative(dir);
				ReactorTiles r = ReactorTiles.getTE(world, p);/*
				if (r == ReactorTiles.COOLANT) {
					TileEntityWaterCell w = (TileEntityWaterCell)world.getBlockEntity(p);
					int T = w.getTemperature();
					int dT = temperature-T;
					if (dT > 0) {
						w.setTemperature(T+dT/4);
						temperature -= dT/4;
					}
				}*/
				if (r == ReactorTiles.SODIUMBOILER) {
					TileEntitySodiumHeater te = (TileEntitySodiumHeater)world.getBlockEntity(p);
					int dTemp = temperature-te.getTemperature();
					if (dTemp > 0) {
						temperature -= dTemp/16;
						te.setTemperature(te.getTemperature()+dTemp/16);
					}
				}
			}
		}
		//ReikaJavaLibrary.pConsole(temperature);

		//ReikaInventoryHelper.addToIInv(ReactorItems.BREEDERFUEL.getStackOf(), this);
	}

	@Override
	public boolean isFissile() {
		return ReikaInventoryHelper.locateInInventory(ReactorItems.BREEDERFUEL.getItemInstance(), itemHandler) != -1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if (!itemHandler.getStackInSlot(i).isEmpty())
			return false;
		if (itemstack.getItem() == ReactorItems.BREEDERFUEL.getItemInstance())
			return i < 4;
		if (itemstack.getItem() == ReactorItems.PLUTONIUM.getItemInstance())
			return i < 4;
		return false;
	}

	@Override
	public int getMaxTemperature() {
		return 900;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		super.onNeutron(e, world, pos);
		if (!world.isClientSide()) {
			if (this.checkPoisonedChance())
				return true;
			if (ReikaRandomHelper.doWithChance(25+temperature/100) && this.isFissile() && ReikaRandomHelper.doWithChance(e.getNeutronSpeed().getInteractionMultiplier())) {
				int slot = ReikaInventoryHelper.locateInInventory(ReactorItems.BREEDERFUEL.getItemInstance(), itemHandler);
				if (slot != -1) {
					if (e.getNeutronType().canTriggerFuelConversion() && ReikaRandomHelper.doWithChance(5*e.getNeutronSpeed().getWasteConversionMultiplier())) {
						int dmg = itemHandler.getStackInSlot(slot).getDamageValue();
						if (dmg == ReactorItems.BREEDERFUEL.getNumberMetadatas()-1) {
							itemHandler.setStackInSlot(slot, ReactorItems.PLUTONIUM.getStackOf());
							this.tryPushSpentFuel(slot);
							ReactorAchievements.PLUTONIUM.triggerAchievement(this.getPlacer());
						}
						else {
							itemHandler.setStackInSlot(slot, ReactorItems.BREEDERFUEL.getStackOfMetadata(dmg+1));
						}
						temperature += 50;
					}
					else {
						temperature += temperature >= 700 ? 30 : 20;
					}
					this.spawnNeutronBurst(world, pos);

					if (ReikaRandomHelper.doWithChance(10)) {
						this.addWaste();
					}

					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean canRemoveItem(int slot, ItemStack is) {
		if (is.getItem() == ReactorItems.PLUTONIUM.getItemInstance())
			return true;
        return is.getItem() == ReactorItems.WASTE.getItemInstance();
    }

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.BREEDER;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return liq == LiquidStates.SODIUM;
	}

	@Override
	public ReactorType getReactorType() {
		return ReactorType.BREEDER;
	}

}
