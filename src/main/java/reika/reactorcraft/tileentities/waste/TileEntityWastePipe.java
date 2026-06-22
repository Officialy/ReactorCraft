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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;

// CHROMA-PORT: import reika.chromaticraft.api.interfaces.WorldRift;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.base.TileEntityReactorPiping;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.MatBlocks;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fission.thorium.TileEntityThoriumCore;
import reika.rotarycraft.tileentities.processing.TileEntityCrystallizer;

public class TileEntityWastePipe extends TileEntityReactorPiping implements NeutronTile {
	public TileEntityWastePipe(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.WASTEPIPE.get(), pos, state);
	}


	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.WASTEPIPE;
	}

	@Override
	public IIcon getBlockIcon() {
		return MatBlocks.CONCRETE.getIcon();
	}

	@Override
	public IIcon getGlassIcon() {
		return Blocks.leaves.getIcon(0, 1);
	}

	public boolean isConnectedToNonSelf(Direction dir) {
		if (!this.isConnectionValidForSide(dir))
			return false;
		if (dir.offsetX == 0 && MinecraftForgeClient.getRenderPass() != 1)
			dir = dir.getOpposite();
		int dx = xCoord+dir.offsetX;
		int dy = yCoord+dir.offsetY;
		int dz = zCoord+dir.offsetZ;
		Level world = level;
		Block id = world.getBlock(dx, dy, dz);
		int meta = world.getBlockMetadata(dx, dy, dz);
		return id != this.getTile().getBlock() || meta != this.getTile();
	}

	@Override
	public boolean isValidFluid(Fluid f) {
		return f == ReactorFluids.getLegacyFluid("rc nuclear waste");
	}

	@Override
	protected void onIntake(BlockEntity te) {

	}

	@Override
	public Block getPipeBlockType() {
		return ReactorBlocks.MATS.getBlockInstance();
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

	@Override
	protected boolean isInteractableTile(BlockEntity te) {
		return te instanceof WorldRift || this.isWasteAcceptingBlock(te);
	}

	private boolean isWasteAcceptingBlock(BlockEntity te) {
		return te instanceof TileEntityWastePipe || te instanceof TileEntityThoriumCore || te instanceof TileEntityCrystallizer;
	}

}
