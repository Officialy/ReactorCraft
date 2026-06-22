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

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import reika.dragonapi.base.BlockMultiBlock;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.instantiable.data.blockstruct.filledblockarray.BlockMatchFailCallback;
import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.dragonapi.libraries.ReikaDirectionHelper;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityReactorGenerator;

public class BlockGeneratorMulti extends BlockReCMultiBlock {

	public BlockGeneratorMulti(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// --- legacy-metadata helpers, now backed by the VARIANT/ACTIVE blockstate on BlockMultiBlock ---
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
		return 13;
	}

	@Override
	public Boolean checkForFullMultiBlock(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		if (!this.checkCore(world, x, y, z, dir, call))
			return false;
		if (!this.checkWindings(world, x, y, z, dir, call))
			return false;
		if (!this.checkHousing(world, x, y, z, dir, call))
			return false;
		if (!this.checkEndCap(world, x, y, z, dir, call))
			return false;
		int l = TileEntityReactorGenerator.getGeneratorLength() - 1;
		return ReactorTiles.getTE(world, new BlockPos(x + dir.getStepX() * l, y, z + dir.getStepZ() * l)) == ReactorTiles.GENERATOR;
	}

	private boolean checkCore(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		int l = TileEntityReactorGenerator.getGeneratorLength() - 1;
		for (int i = 0; i < l; i++) {
			int dx = x + dir.getStepX() * i;
			int dz = z + dir.getStepZ() * i;
			Block b = this.blockAt(world, dx, y, dz);
			int meta = this.metaAt(world, dx, y, dz);
			if (b != this || meta != 0) {
				if (call != null)
					call.onBlockFailure(world, dx, y, dz, this.casing(0));
				return false;
			}
		}
		int dx = x + dir.getStepX() * l;
		int dz = z + dir.getStepZ() * l;
		BlockEntity te = world.getBlockEntity(new BlockPos(dx, y, dz));
		if (te instanceof TileEntityReactorGenerator) {
			return dir == ((TileEntityReactorGenerator) te).getFacing().getOpposite();
		}
		if (call != null)
			call.onBlockFailure(world, dx, y, dz, new BlockKey(ReactorTiles.GENERATOR));
		return false;
	}

	private boolean checkWindings(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		int l = TileEntityReactorGenerator.getGeneratorLength() - 1;
		Direction left = ReikaDirectionHelper.getLeftBy90(dir);
		for (int i = 0; i < l; i++) {
			int seekmeta = i < 2 ? 3 : 1;
			int dx = x + dir.getStepX() * i;
			int dz = z + dir.getStepZ() * i;
			int ddx = dx + left.getStepX();
			int ddx2 = dx - left.getStepX();
			int ddz = dz + left.getStepZ();
			int ddz2 = dz - left.getStepZ();
			for (int k = -1; k <= 1; k++) {
				int dy = y + k;
				Block id = this.blockAt(world, ddx, dy, ddz);
				int meta = this.metaAt(world, ddx, dy, ddz);
				Block id2 = this.blockAt(world, ddx2, dy, ddz2);
				int meta2 = this.metaAt(world, ddx2, dy, ddz2);
				Block id3 = this.blockAt(world, dx, dy, dz);
				int meta3 = this.metaAt(world, dx, dy, dz);
				if (id != this || meta != seekmeta) {
					if (call != null)
						call.onBlockFailure(world, ddx, dy, ddz, this.casing(seekmeta));
					return false;
				}
				if (id2 != this || meta2 != seekmeta) {
					if (call != null)
						call.onBlockFailure(world, ddx2, dy, ddz2, this.casing(seekmeta));
					return false;
				}
				if (k != 0) {
					if (id3 != this || meta3 != seekmeta) {
						if (call != null)
							call.onBlockFailure(world, dx, dy, dz, this.casing(seekmeta));
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean checkHousing(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		int l = TileEntityReactorGenerator.getGeneratorLength() - 1;
		Direction left = ReikaDirectionHelper.getLeftBy90(dir);

		for (int i = 0; i < l; i++) {
			int dx = x + dir.getStepX() * i;
			int dz = z + dir.getStepZ() * i;
			int ddx = dx + left.getStepX();
			int ddx2 = dx - left.getStepX();
			int ddz = dz + left.getStepZ();
			int ddz2 = dz - left.getStepZ();
			int seekmeta = 2;
			for (int k = -2; k <= 2; k += 4) {
				int dy = y + k;
				Block id = this.blockAt(world, ddx, dy, ddz);
				int meta = this.metaAt(world, ddx, dy, ddz);
				Block id2 = this.blockAt(world, ddx2, dy, ddz2);
				int meta2 = this.metaAt(world, ddx2, dy, ddz2);
				Block id3 = this.blockAt(world, dx, dy, dz);
				int meta3 = this.metaAt(world, dx, dy, dz);
				if (i == 1 && k == 2)
					seekmeta = 3;
				if (id != this || meta != 2) {
					if (call != null)
						call.onBlockFailure(world, ddx, dy, ddz, this.casing(2));
					return false;
				}
				if (id2 != this || meta2 != 2) {
					if (call != null)
						call.onBlockFailure(world, ddx2, dy, ddz2, this.casing(2));
					return false;
				}
				if (id3 != this || meta3 != seekmeta) {
					if (call != null)
						call.onBlockFailure(world, dx, dy, dz, this.casing(seekmeta));
					return false;
				}
			}

			ddx = dx + left.getStepX() * 2;
			ddx2 = dx - left.getStepX() * 2;
			ddz = dz + left.getStepZ() * 2;
			ddz2 = dz - left.getStepZ() * 2;

			for (int k = -1; k <= 1; k++) {
				int dy = y + k;
				Block id = this.blockAt(world, ddx, dy, ddz);
				int meta = this.metaAt(world, ddx, dy, ddz);
				Block id2 = this.blockAt(world, ddx2, dy, ddz2);
				int meta2 = this.metaAt(world, ddx2, dy, ddz2);
				if (id != this || meta != 2) {
					if (call != null)
						call.onBlockFailure(world, ddx, dy, ddz, this.casing(2));
					return false;
				}
				if (id2 != this || meta2 != 2) {
					if (call != null)
						call.onBlockFailure(world, ddx2, dy, ddz2, this.casing(2));
					return false;
				}
			}
		}

		return true;
	}

	private boolean checkEndCap(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		int l = TileEntityReactorGenerator.getGeneratorLength() - 1;
		Direction left = ReikaDirectionHelper.getLeftBy90(dir);
		int dx = x + dir.getStepX() * l;
		int dz = z + dir.getStepZ() * l;
		for (int k = -2; k <= 2; k++) {
			int dy = y + k;
			for (int m = -2; m <= 2; m++) {
				if ((Math.abs(k) != 2 || Math.abs(m) != 2) && (k != 0 || m != 0)) {
					int ddx = dx + left.getStepX() * m;
					int ddz = dz + left.getStepZ() * m;
					Block id = this.blockAt(world, ddx, dy, ddz);
					int meta = this.metaAt(world, ddx, dy, ddz);
					if (id != this || meta != 2) {
						if (call != null)
							call.onBlockFailure(world, ddx, dy, ddz, this.casing(2));
						return false;
					}
				}
			}
		}
		for (int i = 0; i < 2; i++) {
			dx = x + dir.getStepX() * i;
			dz = z + dir.getStepZ() * i;

			int ddx = dx + left.getStepX() * 2;
			int ddz = dz + left.getStepZ() * 2;
			int ddx2 = dx - left.getStepX() * 2;
			int ddz2 = dz - left.getStepZ() * 2;
			Block id = this.blockAt(world, ddx, y + 2, ddz);
			int meta = this.metaAt(world, ddx, y + 2, ddz);
			Block id2 = this.blockAt(world, ddx2, y + 2, ddz2);
			int meta2 = this.metaAt(world, ddx2, y + 2, ddz2);
			if (id != this || meta != 2) {
				if (call != null)
					call.onBlockFailure(world, ddx, y + 2, ddz, this.casing(2));
				return false;
			}
			if (id2 != this || meta2 != 2) {
				if (call != null)
					call.onBlockFailure(world, ddx2, y + 2, ddz2, this.casing(2));
				return false;
			}

			id = this.blockAt(world, ddx, y - 2, ddz);
			meta = this.metaAt(world, ddx, y - 2, ddz);
			id2 = this.blockAt(world, ddx2, y - 2, ddz2);
			meta2 = this.metaAt(world, ddx2, y - 2, ddz2);
			if (id != this || meta != 2) {
				if (call != null)
					call.onBlockFailure(world, ddx, y - 2, ddz, this.casing(2));
				return false;
			}
			if (id2 != this || meta2 != 2) {
				if (call != null)
					call.onBlockFailure(world, ddx2, y - 2, ddz2, this.casing(2));
				return false;
			}
		}
		return true;
	}

	@Override
	public void breakMultiBlock(Level world, int x, int y, int z) {
		BlockArray blocks = new BlockArray();
		Set<BlockKey> set = ReikaJavaLibrary.getSet(new BlockKey(this), new BlockKey(ReactorTiles.GENERATOR));
		blocks.recursiveAddMultipleWithBounds(world, x, y, z, set, x - 12, y - 4, z - 12, x + 12, y + 4, z + 12);
		for (int i = 0; i < blocks.getSize(); i++) {
			BlockPos c = blocks.getNthBlock(i);
			int meta = BlockMultiBlock.getLegacyMeta(world, c);
			if (ReactorTiles.getTE(world, c) == ReactorTiles.GENERATOR) {
				TileEntityReactorGenerator te = (TileEntityReactorGenerator) world.getBlockEntity(c);
				te.setHasMultiBlock(false);
			}
			else if (meta >= 8) {
				this.setMeta(world, c, meta - 8);
			}
		}
	}

	@Override
	protected void onCreateFullMultiBlock(Level world, int x, int y, int z, Boolean complete) {
		BlockArray blocks = new BlockArray();
		Set<BlockKey> set = ReikaJavaLibrary.getSet(new BlockKey(this), new BlockKey(ReactorTiles.GENERATOR));
		blocks.recursiveAddMultipleWithBounds(world, x, y, z, set, x - 12, y - 4, z - 12, x + 12, y + 4, z + 12);
		for (int i = 0; i < blocks.getSize(); i++) {
			BlockPos c = blocks.getNthBlock(i);
			int meta = BlockMultiBlock.getLegacyMeta(world, c);
			if (ReactorTiles.getTE(world, c) == ReactorTiles.GENERATOR) {
				TileEntityReactorGenerator te = (TileEntityReactorGenerator) world.getBlockEntity(c);
				te.setHasMultiBlock(true);
			}
			else if (meta < 8) {
				this.setMeta(world, c, meta + 8);
			}
		}
	}

	@Override
	public int getNumberVariants() {
		return 4;
	}

	@Override
	protected String getIconBaseName() {
		return "generator";
	}

	@Override
	public int getTextureIndex(BlockGetter world, int x, int y, int z, int side, int meta) {
		if (meta >= 8)
			return 9;
		if (meta == 3)
			return 5;
		if (meta == 4)
			return 2;
		return meta;
	}

	@Override
	public int getItemTextureIndex(int meta, int side) {
		if (meta < 0)
			return 9 - meta;
		if (meta >= 8)
			return 9;
		if (meta == 3)
			return 5;
		return meta;
	}

	@Override
	public boolean canTriggerMultiBlockCheck(Level world, int x, int y, int z, int meta) {
		return meta == 0;
	}

	@Override
	protected BlockEntity getTileEntityForPosition(Level world, int x, int y, int z) {
		BlockArray blocks = new BlockArray();
		blocks.recursiveAddWithBounds(world, x, y, z, this, x - 12, y - 4, z - 12, x + 12, y + 4, z + 12);
		for (int i = 0; i < blocks.getSize(); i++) {
			BlockPos c = blocks.getNthBlock(i);
			if (ReactorTiles.getTE(world, c.above()) == ReactorTiles.GENERATOR) {
				return world.getBlockEntity(c.above());
			}
		}
		return null;
	}

}
