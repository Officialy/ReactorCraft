/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

public class TileEntityTurbineMeter extends TileEntityReactorBase {

	public TileEntityTurbineMeter(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.TURBINEMETER.get(), pos, state);
	}

	private int turbineY = -1;
	private int oldlvl;
	private int lvl;

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.TURBINEMETER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (this.getTicksExisted() == 0 || world.getGameTime() % 32 == 0) {
			this.remapTurbine(world, pos);
		}

		TileEntityTurbineCore te = this.getTurbine();
		lvl = te != null ? this.getRedstoneFrom(te) : 0;

		if (this.getTicksExisted() == 0 || oldlvl != lvl) {
			ReikaWorldHelper.causeAdjacentUpdates(world, pos);
		}

		oldlvl = lvl;
	}

	private void remapTurbine(Level world, BlockPos pos) {
		int x = pos.getX(), z = pos.getZ();
		for (int i = pos.getY() + 1; i < world.getMaxY(); i++) {
			BlockPos p = new BlockPos(x, i, z);
			ReactorTiles r = ReactorTiles.getTE(world, p);
			if (r != null && r.isTurbine()) {
				turbineY = i;
				return;
			}
			else {
				BlockState s = world.getBlockState(p);
				if (!s.isAir() && s.canOcclude()) {
					turbineY = -1;
					return;
				}
			}
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public int getRedstoneOverride() {
		return lvl;
	}

	private int getRedstoneFrom(TileEntityTurbineCore te) {
		int max = te.getMaxSpeed();
		return 15 * te.getOmega() / max;
	}

	public float getAnalogValue() {
		TileEntityTurbineCore te = this.getTurbine();
		return te != null ? (float) te.getOmega() / te.getMaxSpeed() : 0;
	}

	private TileEntityTurbineCore getTurbine() {
		BlockPos p = new BlockPos(this.getBlockPos().getX(), turbineY, this.getBlockPos().getZ());
		ReactorTiles r = ReactorTiles.getTE(level, p);
		if (r == null || !r.isTurbine())
			return null;
		return (TileEntityTurbineCore) level.getBlockEntity(p);
	}

	@Override
	public int getTextureState(Direction side) {
		return side == Direction.UP ? 2 : side == Direction.DOWN ? 0 : 1;
	}

}
