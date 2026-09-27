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

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.ReactorCoreTE;
import reika.reactorcraft.auxiliary.Temperatured;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.blockentities.storage.BlockEntityReservoir;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityWaterCell extends TileEntityReactorBase implements ReactorCoreTE, Temperatured {

	public TileEntityWaterCell(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.COOLANT.get(), pos, state);
	}

	private LiquidStates internalLiquid = LiquidStates.EMPTY;

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		thermalTicker.update();
		if (ReactorTiles.getTE(world, pos.below()) == this.getTile()) {
			TileEntityWaterCell te = (TileEntityWaterCell) world.getBlockEntity(pos.below());
			if (te.getLiquidState() == LiquidStates.EMPTY && this.getLiquidState() != LiquidStates.EMPTY) {
				te.setLiquidState(this.getLiquidState());
				this.setLiquidState(LiquidStates.EMPTY);
			}
		}
		if (thermalTicker.checkCap() && !world.isClientSide()) {
			this.updateTemperature(world, pos);
		}

		if (!world.isClientSide() && internalLiquid == LiquidStates.EMPTY) {
			var source = world.getCapability(Capabilities.Fluid.BLOCK, pos.above(), Direction.DOWN);
			if (source != null) {
				try (Transaction tx = Transaction.openRoot()) {
					var extracted = ResourceHandlerUtil.extractFirst(source,
							resource -> resource.equals(FluidResource.of(Fluids.WATER))
									|| resource.equals(FluidResource.of(ReactorFluids.getLegacyFluid("rc heavy water"))),
							FluidType.BUCKET_VOLUME, tx);
					if (extracted != null && extracted.amount() == FluidType.BUCKET_VOLUME) {
						Fluid fluid = extracted.resource().getFluid();
						LiquidStates state = fluid == Fluids.WATER ? LiquidStates.WATER : LiquidStates.HEAVY;
						tx.commit();
						this.setLiquidState(state);
					}
				}
			}
		}
	}

	private boolean canIntakeFluid(Fluid f) {
		return f != null && LiquidStates.getState(f) != null && internalLiquid == LiquidStates.EMPTY;
	}

	@Override
	protected void updateTemperature(Level world, BlockPos pos) {
		super.updateTemperature(world, pos);
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, pos);
		int dT = temperature - Tamb;
		if (dT > 0) {
			temperature -= dT / 8;
		}
		for (Direction dir : dirs) {
			BlockEntity te = this.getAdjacentBlockEntity(dir);
			if (te instanceof Temperatured tr) {
				if (internalLiquid != LiquidStates.HEAVY && tr.canDumpHeatInto(internalLiquid)) {
					int t = tr.getTemperature();
					int dt = t - this.getTemperature();
					if (dt > 0) {
						temperature += dt / 2;
						tr.setTemperature(t - dt / 2);
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
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
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
		WATER(Fluids.WATER),
		HEAVY(ReactorFluids.getLegacyFluid("rc heavy water")),
		SODIUM(ReactorFluids.getLegacyFluid("rc sodium")),
		LITHIUM(ReactorFluids.getLegacyFluid("rc lifbe"));

		public static final LiquidStates[] list = values();

		private static final HashMap<Fluid, LiquidStates> map = new HashMap<>();

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
		// The base periodic NBT sync propagates the liquid-state render to clients.
	}

	@Override
	public int getMaxTemperature() {
		return 1000;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		this.setLiquidState(LiquidStates.list[NBT.getIntOr("liq", 0)]);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("liq", this.getLiquidState().ordinal());
	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return liq != LiquidStates.EMPTY && (this.getLiquidState().isWater() == liq.isWater());
	}
}
