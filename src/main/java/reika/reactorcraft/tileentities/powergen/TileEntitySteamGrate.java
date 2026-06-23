/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.powergen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.DragonAPI;
import reika.reactorcraft.auxiliary.SteamTile;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.blocks.BlockSteam;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.WorkingFluid;
import reika.rotarycraft.api.interfaces.Screwdriverable;

public class TileEntitySteamGrate extends TileEntityReactorBase implements Screwdriverable, SteamTile {

	public TileEntitySteamGrate(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.GRATE.get(), pos, state);
	}

	private int steam;
	private boolean requireRedstone;

	private WorkingFluid fluid = WorkingFluid.EMPTY;

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		this.getSteam(world, pos);

		if (!world.isClientSide() && this.canMakeSteam(world, pos)) {
			steam--;
			world.setBlock(pos.above(), this.getSteamState(), 3);
		}

		if (steam <= 0) {
			fluid = WorkingFluid.EMPTY;
		}

		if (DragonAPI.debugtest)
			steam = 3;
	}

	private boolean canMakeSteam(Level world, BlockPos pos) {
		if (steam <= 0)
			return false;
		if (this.hasRedstoneSignal() != requireRedstone)
			return false;
		// DRAGONAPI-PORT: AtmosphereHandler.isNoAtmo (Galacticraft vacuum check) gone — assume atmosphere.
		return ((BlockSteam) ReactorBlocks.STEAM.get()).canMoveInto(world, pos.above());
	}

	/** Freshly produced steam: no-decay + turbine-capable, ammonia flag from the working fluid. */
	private BlockState getSteamState() {
		return ReactorBlocks.STEAM.get().defaultBlockState()
				.setValue(BlockSteam.NO_DECAY, true)
				.setValue(BlockSteam.POWERED, true)
				.setValue(BlockSteam.AMMONIA, fluid == WorkingFluid.AMMONIA);
	}

	private boolean canTakeInWorkingFluid(WorkingFluid f) {
		if (f == WorkingFluid.EMPTY)
			return false;
		if (fluid == WorkingFluid.EMPTY)
			return true;
		return fluid == f;
	}

	private void getSteam(Level world, BlockPos pos) {
		for (Direction dir : dirs) {
			BlockPos npos = pos.relative(dir);
			if (ReactorTiles.getTE(world, npos) == ReactorTiles.STEAMLINE) {
				TileEntitySteamLine te = (TileEntitySteamLine) world.getBlockEntity(npos);
				if (this.canTakeInWorkingFluid(te.getWorkingFluid())) {
					fluid = te.getWorkingFluid();
					int ds = te.getSteam() - steam;
					if (ds > 0) {
						int rm = ds / 4 + 1;
						steam += rm;
						te.removeSteam(rm);
					}
				}
			}
		}
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.GRATE;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		steam = NBT.getIntOr("energy", 0);

		fluid = WorkingFluid.getFromNBT(NBT);

		requireRedstone = NBT.getBooleanOr("red", false);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("energy", steam);

		fluid.saveToNBT(NBT);

		NBT.putBoolean("red", requireRedstone);
	}

	@Override
	public boolean onShiftRightClick(Level world, BlockPos pos, Direction side) {
		return requireRedstone = !requireRedstone;
	}

	@Override
	public boolean onRightClick(Level world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public int getSteam() {
		return steam;
	}

}
