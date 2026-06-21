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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;

import reika.reactorcraft.base.TileEntityReactorPiping;
import reika.reactorcraft.registry.ReactorTiles;

public class TileEntityGasDuct extends TileEntityReactorPiping {
	public TileEntityGasDuct(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.GASPIPE.get(), pos, state);
	}


	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.GASPIPE;
	}

	@Override
	public IIcon getBlockIcon() {
		return Blocks.hardened_clay.getIcon(1, 0);
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
		return f.isGaseous();
	}

	@Override
	protected void onIntake(BlockEntity te) {

	}

	@Override
	public Block getPipeBlockType() {
		return Blocks.hardened_clay;
	}

	@Override
	public IIcon getGlassIcon() {
		return Blocks.glass.getIcon(0, 0);
	}

}
