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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeRenderConnector;

public abstract class TileEntityLine extends TileEntityReactorBase {

	private boolean[] connections = new boolean[6];

	public TileEntityLine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected final void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {

	}

	public final boolean isConnectedOnSideAt(Level world, BlockPos pos, Direction dir) {
		dir = dir.getStepX() == 0 ? dir.getOpposite() : dir;
		BlockPos dpos = pos.relative(dir);
		Block id = world.getBlockState(dpos).getBlock();
		if (id == Blocks.AIR)
			return false;
		if (ReactorTiles.getMachineFromBlock(id) == this.getTile())
			return true;
		BlockEntity te = world.getBlockEntity(dpos);
		return this.canConnectToMachine(id, dir, te);
	}

	protected boolean canConnectToMachine(Block id, Direction dir, BlockEntity te) {
		return false;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		for (int i = 0; i < 6; i++) {
			connections[i] = NBT.getBooleanOr("conn"+i, false);
		}
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		for (int i = 0; i < 6; i++) {
			NBT.putBoolean("conn"+i, connections[i]);
		}
	}

	/** Direction is relative to the piping block (so DOWN means the block is below the pipe) */
	public final boolean isConnectionValidForSide(Direction dir) {
		return connections[dir.ordinal()];
	}

	@Override
	public final AABB getRenderBoundingBox() {
		return new AABB(getBlockPos());
	}

	public final void recomputeConnections(Level world, BlockPos pos) {
		for (int i = 0; i < 6; i++) {
			connections[i] = this.isConnected(dirs[i]);
		}
		this.syncAllData(false);
	}

	public final void deleteFromAdjacentConnections(Level world, BlockPos pos) {
		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			BlockPos dpos = pos.relative(dir);
			ReactorTiles m = ReactorTiles.getTE(world, dpos);
			if (m == this.getTile()) {
				TileEntityLine te = (TileEntityLine)world.getBlockEntity(dpos);
				te.connections[dir.getOpposite().ordinal()] = false;
				te.syncAllData(false);
			}
		}
	}

	public final void addToAdjacentConnections(Level world, BlockPos pos) {
		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			BlockPos dpos = pos.relative(dir);
			ReactorTiles m = ReactorTiles.getTE(world, dpos);
			if (m == this.getTile()) {
				TileEntityLine te = (TileEntityLine)world.getBlockEntity(dpos);
				te.connections[dir.getOpposite().ordinal()] = true;
				te.syncAllData(false);
			}
		}
	}

	private boolean isConnected(Direction dir) {
		BlockPos dpos = getBlockPos().relative(dir);
		ReactorTiles m = this.getTile();
		ReactorTiles m2 = ReactorTiles.getTE(level, dpos);
		if (m == m2)
			return true;
		BlockEntity te = level.getBlockEntity(dpos);
		if (te instanceof PipeRenderConnector)
			return ((PipeRenderConnector)te).canConnectToPipeOnSide(dir);
		return false;
	}

	@Override
	public final int getPacketDelay() {
		return 4*super.getPacketDelay();
	}

	public abstract ResourceLocation getTexture();

	public void onEntityCollided(Entity e) {

	}
}
