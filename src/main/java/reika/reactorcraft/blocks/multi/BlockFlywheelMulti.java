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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import reika.dragonapi.base.BlockMultiBlock;
import reika.dragonapi.instantiable.data.blockstruct.StructuredBlockArray;
import reika.dragonapi.instantiable.data.blockstruct.filledblockarray.BlockMatchFailCallback;
import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.dragonapi.libraries.ReikaDirectionHelper;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityReactorFlywheel;

public class BlockFlywheelMulti extends BlockReCMultiBlock {

	public BlockFlywheelMulti(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// --- legacy-metadata helpers, backed by the VARIANT/ACTIVE blockstate on BlockMultiBlock ---
	private Block blockAt(BlockGetter world, int x, int y, int z) {
		return world.getBlockState(new BlockPos(x, y, z)).getBlock();
	}

	private int metaAt(BlockGetter world, int x, int y, int z) {
		return BlockMultiBlock.getLegacyMeta(world, new BlockPos(x, y, z));
	}

	private void setMeta(Level world, BlockPos pos, int meta) {
		world.setBlock(pos, BlockMultiBlock.withLegacyMeta(world.getBlockState(pos), meta), 3);
	}

	private BlockKey casing(int meta) {
		return new BlockKey(BlockMultiBlock.withLegacyMeta(this.defaultBlockState(), meta));
	}

	@Override
	public int getNumberTextures() {
		return 4;
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
			int m = i == 1 ? 0 : 2;
			if (this.blockAt(world, dx, midY, dz) != this || this.metaAt(world, dx, midY, dz) != m) {
				if (call != null)
					call.onBlockFailure(world, dx, midY, dz, this.casing(m));
				return false;
			}
			dx = midX - left.getStepX() * i;
			dz = midZ - left.getStepZ() * i;
			if (this.blockAt(world, dx, midY, dz) != this || this.metaAt(world, dx, midY, dz) != m) {
				if (call != null)
					call.onBlockFailure(world, dx, midY, dz, this.casing(m));
				return false;
			}
			if (this.blockAt(world, midX, midY - i, midZ) != this || this.metaAt(world, midX, midY - i, midZ) != m) {
				if (call != null)
					call.onBlockFailure(world, midX, midY - i, midZ, this.casing(m));
				return false;
			}
			if (this.blockAt(world, midX, midY + i, midZ) != this || this.metaAt(world, midX, midY + i, midZ) != m) {
				if (call != null)
					call.onBlockFailure(world, midX, midY - i, midZ, this.casing(m));
				return false;
			}
		}

		int dx = midX + left.getStepX();
		int dz = midZ + left.getStepZ();
		if (this.blockAt(world, dx, midY + 1, dz) != this || this.metaAt(world, dx, midY + 1, dz) != 1) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 1, dz, this.casing(1));
			return false;
		}
		if (this.blockAt(world, dx, midY - 1, dz) != this || this.metaAt(world, dx, midY - 1, dz) != 1) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 1, dz, this.casing(1));
			return false;
		}
		dx = midX - left.getStepX();
		dz = midZ - left.getStepZ();
		if (this.blockAt(world, dx, midY + 1, dz) != this || this.metaAt(world, dx, midY + 1, dz) != 1) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 1, dz, this.casing(1));
			return false;
		}
		if (this.blockAt(world, dx, midY - 1, dz) != this || this.metaAt(world, dx, midY - 1, dz) != 1) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 1, dz, this.casing(1));
			return false;
		}

		dx = midX + left.getStepX();
		dz = midZ + left.getStepZ();
		if (this.blockAt(world, dx, midY + 2, dz) != this || this.metaAt(world, dx, midY + 2, dz) != 2) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 2, dz, this.casing(2));
			return false;
		}
		if (this.blockAt(world, dx, midY - 2, dz) != this || this.metaAt(world, dx, midY - 2, dz) != 2) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 2, dz, this.casing(2));
			return false;
		}
		dx = midX - left.getStepX();
		dz = midZ - left.getStepZ();
		if (this.blockAt(world, dx, midY + 2, dz) != this || this.metaAt(world, dx, midY + 2, dz) != 2) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 2, dz, this.casing(2));
			return false;
		}
		if (this.blockAt(world, dx, midY - 2, dz) != this || this.metaAt(world, dx, midY - 2, dz) != 2) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 2, dz, this.casing(2));
			return false;
		}

		dx = midX + left.getStepX() * 2;
		dz = midZ + left.getStepZ() * 2;
		if (this.blockAt(world, dx, midY + 1, dz) != this || this.metaAt(world, dx, midY + 1, dz) != 2) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 1, dz, this.casing(2));
			return false;
		}
		if (this.blockAt(world, dx, midY - 1, dz) != this || this.metaAt(world, dx, midY - 1, dz) != 2) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 1, dz, this.casing(2));
			return false;
		}
		dx = midX - left.getStepX() * 2;
		dz = midZ - left.getStepZ() * 2;
		if (this.blockAt(world, dx, midY + 1, dz) != this || this.metaAt(world, dx, midY + 1, dz) != 2) {
			if (call != null)
				call.onBlockFailure(world, dx, midY + 1, dz, this.casing(2));
			return false;
		}
		if (this.blockAt(world, dx, midY - 1, dz) != this || this.metaAt(world, dx, midY - 1, dz) != 2) {
			if (call != null)
				call.onBlockFailure(world, dx, midY - 1, dz, this.casing(2));
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
			int meta = BlockMultiBlock.getLegacyMeta(world, c);
			if (meta >= 8) {
				this.setMeta(world, c, meta - 8);
			}
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
			int meta = BlockMultiBlock.getLegacyMeta(world, c);
			if (meta < 8) {
				this.setMeta(world, c, meta + 8);
			}
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
	public int getNumberVariants() {
		return 3;
	}

	@Override
	protected String getIconBaseName() {
		return "flywheel";
	}

	@Override
	public int getTextureIndex(BlockGetter world, int x, int y, int z, int side, int meta) {
		return meta >= 8 ? 3 : meta;
	}

	@Override
	public int getItemTextureIndex(int meta, int side) {
		return meta & 7;
	}

	@Override
	public boolean canTriggerMultiBlockCheck(Level world, int x, int y, int z, int meta) {
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
