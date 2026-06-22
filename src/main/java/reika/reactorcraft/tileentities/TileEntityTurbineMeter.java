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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.base.TileEntityReactorBase;
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
		if (this.getTicksExisted() == 0 || world.getTotalWorldTime()%32 == 0) {
			this.remapTurbine(world, x, y, z);
		}

		TileEntityTurbineCore te = this.getTurbine();
		lvl = te != null ? this.getRedstoneFrom(te) : 0;

		if (this.getTicksExisted() == 0 || oldlvl != lvl) {
			ReikaWorldHelper.causeAdjacentUpdates(world, x, y, z);
		}

		oldlvl = lvl;
	}

	private void remapTurbine(Level world, int x, int y, int z) {
		for (int i = y+1; i < world.provider.getHeight(); i++) {
			ReactorTiles r = ReactorTiles.getTE(world, x, i, z);
			if (r != null && r.isTurbine()) {
				turbineY = i;
				return;
			}
			else {
				Block b = world.getBlock(x, i, z);
				if (b != Blocks.air && b.getLightOpacity(world, x, i, z) > 0) {
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
		return 15*te.getOmega()/max;
	}

	public float getAnalogValue() {
		TileEntityTurbineCore te = this.getTurbine();
		return te != null ? (float)te.getOmega()/te.getMaxSpeed() : 0;
	}

	private TileEntityTurbineCore getTurbine() {
		ReactorTiles r = ReactorTiles.getTE(level, xCoord, turbineY, zCoord);
		if (r == null || !r.isTurbine())
			return null;
		return (TileEntityTurbineCore)this.getBlockEntity(xCoord, turbineY, zCoord);
	}

	@Override
	public int getTextureState(Direction side) {
		return side == Direction.UP ? 2 : side == Direction.DOWN ? 0 : 1;
	}

}
