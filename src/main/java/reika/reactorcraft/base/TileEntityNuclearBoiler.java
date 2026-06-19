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

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import reika.dragonapi.instantiable.data.Proportionality;
import reika.reactorcraft.auxiliary.TypedReactorCoreTE;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;

public abstract class TileEntityNuclearBoiler extends TileEntityTankedReactorMachine implements TypedReactorCoreTE {

	protected int steam;
	protected Proportionality<ReactorType> type = new Proportionality();

	public TileEntityNuclearBoiler(BlockEntityType<?> t, BlockPos pos, BlockState state) {
		super(t, pos, state);
		this.setReactorType(this.getDefaultReactorType(), 1);
	}

	public abstract ReactorType getDefaultReactorType();

	public final void setReactorType(ReactorType t, double amt) {
		type.addValue(t, amt);
	}

	public final void setReactorTypes(Proportionality<ReactorType> p) {
		for (ReactorType r : p.getElements()) {
			this.setReactorType(r, p.getValue(r));
		}
	}

	public final ReactorType getReactorType() {
		return type.getLargestCategory();
	}

	public final Collection<ReactorType> getReactorTypeSet() {
		return type.getElements();
	}

	public final double getReactorTypeFraction(ReactorType r) {
		return type.getFraction(r);
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		thermalTicker.update();

		if (thermalTicker.checkCap() && !world.isClientSide()) {
			this.updateTemperature(world, pos);
		}

		this.balanceFluid(world, pos);
	}

	@Override
	protected final void updateTemperature(Level world, BlockPos pos) {
		super.updateTemperature(world, pos);

		if (temperature > this.getMaxTemperature())
			this.overheat(world, pos);
	}

	protected abstract void overheat(Level world, BlockPos pos);

	@Override
	public final int getTemperature() {
		return temperature;
	}

	@Override
	public final void setTemperature(int T) {
		temperature = T;
	}

	@Override
	public final boolean canDumpHeatInto(LiquidStates liq) {
		return false;
	}

	@Override
	public final boolean canReceiveFrom(Direction from) {
		return from == Direction.DOWN;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

	protected void balanceFluid(Level world, BlockPos pos) {
		for (int i = 0; i < 2; i++) {
			Direction dir = dirs[i];
			BlockPos dpos = pos.relative(dir);
			ReactorTiles r = ReactorTiles.getTE(world, dpos);
			if (r == this.getTile()) {
				TileEntityNuclearBoiler te = (TileEntityNuclearBoiler)world.getBlockEntity(dpos);
				if (te.tank.getFluidLevel() < tank.getFluidLevel() && (te.tank.isEmpty() || te.tank.getActualFluid() == tank.getActualFluid())) {
					int dl = tank.getFluidLevel()-te.tank.getFluidLevel();
					te.tank.addLiquid(dl/4+1, tank.getActualFluid());
					tank.removeLiquid(dl/4+1);
				}
			}
		}
	}

	public final void addLiquid(int amt, Fluid fluid) {
		tank.addLiquid(amt, fluid);
	}

	@Override
	public final int getTextureState(Direction side) {
		if (side.getStepY() != 0)
			return 0;
		ReactorTiles src = this.getTile();
		ReactorTiles r = ReactorTiles.getTE(level, getBlockPos().below());
		ReactorTiles r2 = ReactorTiles.getTE(level, getBlockPos().above());
		if (r2 == src && r == src)
			return 2;
		else if (r2 == src)
			return 1;
		else if (r == src)
			return 3;
		return 0;
	}

	public final int removeSteam() {
		int s = steam;
		steam = 0;
		return s;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		type = new Proportionality();
		CompoundTag tag = NBT.getCompoundOrEmpty("types");
		for (String key : tag.keySet()) {
			type.addValue(ReactorType.valueOf(key), tag.getDoubleOr(key, 0));
		}
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		CompoundTag tag = new CompoundTag();
		for (ReactorType r : type.getElements()) {
			tag.putDouble(r.name(), type.getValue(r));
		}
		NBT.put("types", tag);
	}
}
