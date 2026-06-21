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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import java.util.HashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids./*FLUIDCONTAINER-PORT*/ FluidContainerRegistry;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.ReactorCoreTE;
import reika.reactorcraft.auxiliary.Temperatured;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.registry.MachineRegistry;
import reika.rotarycraft.tileentities.storage.TileEntityReservoir;

public class TileEntityWaterCell extends TileEntityReactorBase implements ReactorCoreTE, Temperatured {
	public TileEntityWaterCell(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.COOLANT.get(), pos, state);
	}


	private LiquidStates internalLiquid;

	public TileEntityWaterCell() {
		this.setLiquidState(LiquidStates.EMPTY);
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		thermalTicker.update();
		if (ReactorTiles.getTE(world, x, y-1, z) == this.getTile()) {
			TileEntityWaterCell te = (TileEntityWaterCell)world.getBlockEntity(x, y-1, z);
			if (te.getLiquidState() == LiquidStates.EMPTY && this.getLiquidState() != LiquidStates.EMPTY) {
				te.setLiquidState(this.getLiquidState());
				this.setLiquidState(LiquidStates.EMPTY);
			}
		}
		MachineRegistry m = MachineRegistry.getMachine(world, x, y+1, z);
		if (m == MachineRegistry.RESERVOIR) {
			TileEntityReservoir te = (TileEntityReservoir)this.getAdjacentTileEntity(Direction.UP);
			if (te.getLevel() >= 1000) {
				Fluid f = te.getFluid();
				if (this.canIntakeFluid(f)) {
					te.removeLiquid(1000);
					LiquidStates lq = LiquidStates.getState(f);
					this.setLiquidState(lq);
				}
			}
		}

		if (thermalTicker.checkCap() && !world.isClientSide()) {
			this.updateTemperature(world, x, y, z);
		}

		if (this.getLiquidState() == LiquidStates.EMPTY) {
			BlockEntity te = world.getBlockEntity(x, y+1, z);
			if (te instanceof IFluidHandler) {
				IFluidHandler ic = (IFluidHandler)te;
				FluidStack liq = ic.drain(Direction.DOWN, /*FLUIDCONTAINER-PORT*/ FluidContainerRegistry.BUCKET_VOLUME, false);
				if (liq != null && liq.amount >= /*FLUIDCONTAINER-PORT*/ FluidContainerRegistry.BUCKET_VOLUME) {
					ic.drain(Direction.DOWN, /*FLUIDCONTAINER-PORT*/ FluidContainerRegistry.BUCKET_VOLUME, true);
					if (liq.getFluid().equals(FluidRegistry.WATER)) {
						this.setLiquidState(LiquidStates.WATER);
					}
					else if (liq.getFluid().equals(ReactorCraft.D2O)) {
						this.setLiquidState(LiquidStates.HEAVY);
					}
				}
			}
		}
	}

	private boolean canIntakeFluid(Fluid f) {
		return f != null && LiquidStates.getState(f) != null && internalLiquid == LiquidStates.EMPTY;
	}

	@Override
	protected void updateTemperature(Level world, int x, int y, int z) {
		super.updateTemperature(world, x, y, z);
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, x, y, z);
		int dT = temperature-Tamb;
		if (dT > 0) {
			temperature -= dT/8;
		}
		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			BlockEntity te = this.getAdjacentTileEntity(dir);
			if (te instanceof Temperatured) {
				Temperatured tr = (Temperatured)te;
				if (internalLiquid != LiquidStates.HEAVY && tr.canDumpHeatInto(internalLiquid)) {
					int t = tr.getTemperature();
					int dt = t-this.getTemperature();
					if (dt > 0) {
						temperature += dt/2;
						tr.setTemperature(t-dt/2);
						if (rand.nextInt(5) == 0)
							this.setLiquidState(LiquidStates.EMPTY);
					}
				}
			}
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.COOLANT;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {/*
		if (ReikaMathLibrary.doWithChance(this.getChanceToStop())) {
			temperature += ReikaThermoHelper.getTemperatureIncrease(ReikaThermoHelper.WATER_HEAT, 1000, ReikaNuclearHelper.getUraniumFissionNeutronE());
			storedEnergy += ReikaNuclearHelper.getUraniumFissionNeutronE(); //3.8kJ per neutron (kinetic energy)
			return true;
		}*/
		if (this.getLiquidState() == LiquidStates.HEAVY) {
			e.moderate();
			ReactorAchievements.CANDU.triggerAchievement(this.getPlacer());
		}
		return false;
	}

	@Override
	public int getTemperature() {
		return temperature;
	}

	@Override
	public void setTemperature(int T) {
		temperature = T;
	}

	@Override
	public int getTextureState(Direction side) {
		return this.getLiquidState().ordinal();
	}

	public int getChanceToStop() {
		if (internalLiquid == null)
			return 0;
		if (internalLiquid == LiquidStates.HEAVY) {
			return 75;
		}
		if (internalLiquid == LiquidStates.WATER) {
			return 50;
		}
		return 0;
	}

	public enum LiquidStates {
		EMPTY(null),
		WATER(FluidRegistry.WATER),
		HEAVY(ReactorFluids.getLegacyFluid("rc heavy water")),
		SODIUM(ReactorFluids.getLegacyFluid("rc sodium")),
		LITHIUM(ReactorFluids.getLegacyFluid("rc lifbe"));

		public static final LiquidStates[] list = values();

		private static final HashMap<Fluid, LiquidStates> map = new HashMap();

		private final Fluid fluid;

		private LiquidStates(Fluid f) {
			fluid = f;
		}

		public boolean isWater() {
			return this == WATER || this == HEAVY;
		}

		public static LiquidStates getState(Fluid f) {
			return map.get(f);
		}

		static {
			for (int i = 1; i < list.length; i++) {
				LiquidStates lq = list[i];
				map.put(lq.fluid, lq);
			}
		}
	}

	public LiquidStates getLiquidState() {
		return internalLiquid;
	}

	public void setLiquidState(LiquidStates liq) {
		internalLiquid = liq;
		if (level != null)
			level.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public int getMaxTemperature() {
		return 1000;
	}

	private void onMeltdown(Level world, int x, int y, int z) {

	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		this.setLiquidState(LiquidStates.list[NBT.getInteger("liq")]);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		NBT.setInteger("liq", this.getLiquidState().ordinal());
	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return liq != LiquidStates.EMPTY && (this.getLiquidState().isWater() == liq.isWater());
	}
}
