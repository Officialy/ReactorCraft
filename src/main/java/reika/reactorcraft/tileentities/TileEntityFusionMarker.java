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

import java.util.ArrayList;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fusion.TileEntityToroidMagnet.Aim;

public class TileEntityFusionMarker extends TileEntityReactorBase {
	public TileEntityFusionMarker(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.MARKER.get(), pos, state);
	}


	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.MARKER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {

	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	public boolean renderLines() {
		return this.hasRedstoneSignal();
	}

	public ArrayList<Aim> getAimPoints() {
		ArrayList li = new ArrayList();
		li.add(Aim.N);
		li.add(Aim.N);
		li.add(Aim.NNW1);
		li.add(Aim.NNW2);
		li.add(Aim.NNW3);
		li.add(Aim.NW);
		li.add(Aim.WNW1);
		li.add(Aim.WNW2);
		li.add(Aim.WNW3);
		li.add(Aim.W);
		li.add(Aim.W);
		li.add(Aim.W);
		li.add(Aim.WSW1);
		li.add(Aim.WSW2);
		li.add(Aim.WSW3);
		li.add(Aim.SW);
		li.add(Aim.SSW1);
		li.add(Aim.SSW2);
		li.add(Aim.SSW3);
		li.add(Aim.S);
		li.add(Aim.S);
		li.add(Aim.S);
		li.add(Aim.SSE1);
		li.add(Aim.SSE2);
		li.add(Aim.SSE3);
		li.add(Aim.SE);
		li.add(Aim.ESE1);
		li.add(Aim.ESE2);
		li.add(Aim.ESE3);
		li.add(Aim.E);
		li.add(Aim.E);
		li.add(Aim.E);
		li.add(Aim.ENE1);
		li.add(Aim.ENE2);
		li.add(Aim.ENE3);
		li.add(Aim.NE);
		li.add(Aim.NNE1);
		li.add(Aim.NNE2);
		li.add(Aim.NNE3);
		li.add(Aim.N);
		return li;
	}

	// 1.21.5: BlockEntity.getRenderBoundingBox was removed; kept as a helper for the renderer's bounds.
	// Aim lines span the whole tokamak, so render bounds are infinite.
	public AABB getRenderBoundingBox() {
		return AABB.INFINITE;
	}

}
