/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.waste;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

// CHROMA-PORT: import reika.chromaticraft.api.interfaces.WorldRift;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.base.TileEntityReactorPiping;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fission.thorium.TileEntityThoriumCore;

public class TileEntityWastePipe extends TileEntityReactorPiping implements NeutronTile {

	public TileEntityWastePipe(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.WASTEPIPE.get(), pos, state);
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.WASTEPIPE;
	}

	@Override
	public boolean isConnectedToNonSelf(Direction dir) {
		if (!this.isConnectionValidForSide(dir))
			return false;
		return ReactorTiles.getTE(level, this.getBlockPos().relative(dir)) != this.getTile();
	}

	@Override
	public boolean isValidFluid(Fluid f) {
		return f == ReactorFluids.getLegacyFluid("rc nuclear waste");
	}

	@Override
	protected void onIntake(BlockEntity te) {

	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

	@Override
	protected boolean isInteractableTile(BlockEntity te) {
		// CHROMA-PORT: WorldRift acceptance gated out (ChromatiCraft not in build).
		return this.isWasteAcceptingBlock(te);
	}

	private boolean isWasteAcceptingBlock(BlockEntity te) {
		// MOD-PORT: RotaryCraft BlockEntityCrystallizer is not yet ported (package commented out); re-add
		// `|| te instanceof BlockEntityCrystallizer` once it lands so the crystallizer can pull waste.
		return te instanceof TileEntityWastePipe || te instanceof TileEntityThoriumCore;
	}

}
