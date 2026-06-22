/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.blocks.multi;

import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import reika.dragonapi.instantiable.data.blockstruct.StructuredBlockArray;
import reika.dragonapi.instantiable.data.blockstruct.filledblockarray.BlockMatchFailCallback;
import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.dragonapi.libraries.ReikaDirectionHelper;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityReactorFlywheel;

public class BlockFlywheelMulti extends BlockReCMultiBlock {

	/** The named casing parts of the flywheel multiblock (legacy variants 0..2). */
	public enum FlywheelPart implements StringRepresentable {
		HUB,   // 0: the inner ring adjacent to the flywheel core
		SPOKE, // 1: the diagonal connectors
		RIM;   // 2: the outer ring

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

	public static final EnumProperty<FlywheelPart> PART = EnumProperty.create("part", FlywheelPart.class);

	public BlockFlywheelMulti(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(PART, FlywheelPart.HUB).setValue(FORMED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(PART);
	}

	private boolean isPart(BlockGetter world, int x, int y, int z, FlywheelPart p) {
		BlockState s = world.getBlockState(new BlockPos(x, y, z));
		return s.is(this) && s.getValue(PART) == p;
	}

	private BlockKey casing(FlywheelPart p) {
		return new BlockKey(this.defaultBlockState().setValue(PART, p));
	}

	@Override
	public Boolean checkForFullMultiBlock(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		if (blocks.getSize() != 20)
			return false;
		int midX = blocks.getMinX() + blocks.getSizeX() / 2;
		int midY = blocks.getMinY() + blocks.getSizeY() / 2;
		int midZ = blocks.getMinZ() + blocks.getSizeZ() / 2;
		if (ReactorTiles.getTE(world, new BlockPos(midX, midY, midZ)) != ReactorTiles.FLYWHEEL) {
			if (call != null)
				call.onBlockFailure(world, midX, midY, midZ, new BlockKey(ReactorTiles.FLYWHEEL));
			return false;
		}
		TileEntityReactorFlywheel te = (TileEntityReactorFlywheel) world.getBlockEntity(new BlockPos(midX, midY, midZ));
		Direction left = ReikaDirectionHelper.getLeftBy90(te.getFacing());

		for (int i = 1; i <= 2; i++) {
			int dx = midX + left.getStepX() * i;
			int dz = midZ + left.getStepZ() * i;
			FlywheelPart seek = i == 1 ? FlywheelPart.HUB : FlywheelPart.RIM;
			if (!this.isPart(world, dx, midY, dz, seek)) {
				if (call != null)
					call.onBlockFailure(world, dx, midY, dz, this.casing(seek));
				return false;
			}
			dx = midX - left.getStepX() * i;
			dz = midZ - left.getStepZ() * i;
			if (!this.isPart(world, dx, midY, dz, seek)) {
				if (call != null)
					call.onBlockFailure(world, dx, midY, dz, this.casing(seek));
				return false;
			}
			if (!this.isPart(world, midX, midY - i, midZ, seek)) {
				if (call != null)
					call.onBlockFailure(world, midX, midY - i, midZ, this.casing(seek));
				return false;
			}
			if (!this.isPart(world, midX, midY + i, midZ, seek)) {
				if (call != null)
					call.onBlockFailure(world, midX, midY - i, midZ, this.casing(seek));
				return false;
			}
		}

		int dx = midX + left.getStepX();
		int dz = midZ + left.getStepZ();
		if (!this.isPart(world, dx, midY + 1, dz, FlywheelPart.SPOKE)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 1, dz, this.casing(FlywheelPart.SPOKE));
			return false;
		}
		if (!this.isPart(world, dx, midY - 1, dz, FlywheelPart.SPOKE)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 1, dz, this.casing(FlywheelPart.SPOKE));
			return false;
		}
		dx = midX - left.getStepX();
		dz = midZ - left.getStepZ();
		if (!this.isPart(world, dx, midY + 1, dz, FlywheelPart.SPOKE)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 1, dz, this.casing(FlywheelPart.SPOKE));
			return false;
		}
		if (!this.isPart(world, dx, midY - 1, dz, FlywheelPart.SPOKE)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 1, dz, this.casing(FlywheelPart.SPOKE));
			return false;
		}

		dx = midX + left.getStepX();
		dz = midZ + left.getStepZ();
		if (!this.isPart(world, dx, midY + 2, dz, FlywheelPart.RIM)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 2, dz, this.casing(FlywheelPart.RIM));
			return false;
		}
		if (!this.isPart(world, dx, midY - 2, dz, FlywheelPart.RIM)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 2, dz, this.casing(FlywheelPart.RIM));
			return false;
		}
		dx = midX - left.getStepX();
		dz = midZ - left.getStepZ();
		if (!this.isPart(world, dx, midY + 2, dz, FlywheelPart.RIM)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 2, dz, this.casing(FlywheelPart.RIM));
			return false;
		}
		if (!this.isPart(world, dx, midY - 2, dz, FlywheelPart.RIM)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 2, dz, this.casing(FlywheelPart.RIM));
			return false;
		}

		dx = midX + left.getStepX() * 2;
		dz = midZ + left.getStepZ() * 2;
		if (!this.isPart(world, dx, midY + 1, dz, FlywheelPart.RIM)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 1, dz, this.casing(FlywheelPart.RIM));
			return false;
		}
		if (!this.isPart(world, dx, midY - 1, dz, FlywheelPart.RIM)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 1, dz, this.casing(FlywheelPart.RIM));
			return false;
		}
		dx = midX - left.getStepX() * 2;
		dz = midZ - left.getStepZ() * 2;
		if (!this.isPart(world, dx, midY + 1, dz, FlywheelPart.RIM)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 1, dz, this.casing(FlywheelPart.RIM));
			return false;
		}
		if (!this.isPart(world, dx, midY - 1, dz, FlywheelPart.RIM)) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 1, dz, this.casing(FlywheelPart.RIM));
			return false;
		}

		return true;
	}

	@Override
	public void breakMultiBlock(Level world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		blocks.recursiveAddWithBoundsRanged(world, x + 1, y, z, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		blocks.recursiveAddWithBoundsRanged(world, x - 1, y, z, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y + 1, z, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y - 1, z, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z + 1, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z - 1, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		for (int i = 0; i < blocks.getSize(); i++) {
			BlockPos c = blocks.getNthBlock(i);
			BlockState cs = world.getBlockState(c);
			if (cs.is(this) && cs.getValue(FORMED))
				world.setBlock(c, cs.setValue(FORMED, false), 3);
		}
		int midX = blocks.getMidX();
		int midY = blocks.getMidY();
		int midZ = blocks.getMidZ();
		if (ReactorTiles.getTE(world, new BlockPos(midX, midY, midZ)) == ReactorTiles.FLYWHEEL) {
			TileEntityReactorFlywheel te = (TileEntityReactorFlywheel) world.getBlockEntity(new BlockPos(midX, midY, midZ));
			te.setHasMultiBlock(false);
		}
	}

	@Override
	protected void onCreateFullMultiBlock(Level world, int x, int y, int z, Boolean complete) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		for (int i = 0; i < blocks.getSize(); i++) {
			BlockPos c = blocks.getNthBlock(i);
			BlockState cs = world.getBlockState(c);
			if (cs.is(this) && !cs.getValue(FORMED))
				world.setBlock(c, cs.setValue(FORMED, true), 3);
		}
		int midX = blocks.getMidX();
		int midY = blocks.getMidY();
		int midZ = blocks.getMidZ();
		if (ReactorTiles.getTE(world, new BlockPos(midX, midY, midZ)) == ReactorTiles.FLYWHEEL) {
			TileEntityReactorFlywheel te = (TileEntityReactorFlywheel) world.getBlockEntity(new BlockPos(midX, midY, midZ));
			te.setHasMultiBlock(true);
		}
	}

	@Override
	public boolean canTriggerMultiBlockCheck(Level world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	protected BlockEntity getTileEntityForPosition(Level world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x - 6, y - 6, z - 6, x + 6, y + 6, z + 6, 1);
		int midX = blocks.getMinX() + blocks.getSizeX() / 2;
		int midY = blocks.getMinY() + blocks.getSizeY() / 2;
		int midZ = blocks.getMinZ() + blocks.getSizeZ() / 2;
		if (ReactorTiles.getTE(world, new BlockPos(midX, midY, midZ)) != ReactorTiles.FLYWHEEL)
			return null;
		return world.getBlockEntity(new BlockPos(midX, midY, midZ));
	}

}
