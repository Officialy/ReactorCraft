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
import java.util.Set;

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

import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.instantiable.data.blockstruct.filledblockarray.BlockMatchFailCallback;
import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.dragonapi.libraries.ReikaDirectionHelper;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityReactorGenerator;

public class BlockGeneratorMulti extends BlockReCMultiBlock {

	/** The named casing parts of the generator multiblock (legacy variants 0..3). */
	public enum GeneratorPart implements StringRepresentable {
		CORE,     // 0: the central rotor axis line (placing this triggers the assembly scan)
		WINDING,  // 1: the outer winding rings
		HOUSING,  // 2: the outer shell + end cap
		COIL;     // 3: the inner field coil (the steam-routing face BlockSteam queries)

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

	public static final EnumProperty<GeneratorPart> PART = EnumProperty.create("part", GeneratorPart.class);

	public BlockGeneratorMulti(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(PART, GeneratorPart.CORE).setValue(FORMED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(PART);
	}

	/** True iff the casing at (x,y,z) is this block with the given part. */
	private boolean isPart(BlockGetter world, int x, int y, int z, GeneratorPart p) {
		BlockState s = world.getBlockState(new BlockPos(x, y, z));
		return s.is(this) && s.getValue(PART) == p;
	}

	private BlockKey casing(GeneratorPart p) {
		return new BlockKey(this.defaultBlockState().setValue(PART, p));
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
			if (!this.isPart(world, dx, y, dz, GeneratorPart.CORE)) {
				if (call != null)
					call.onBlockFailure(world, dx, y, dz, this.casing(GeneratorPart.CORE));
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
			GeneratorPart seek = i < 2 ? GeneratorPart.COIL : GeneratorPart.WINDING;
			int dx = x + dir.getStepX() * i;
			int dz = z + dir.getStepZ() * i;
			int ddx = dx + left.getStepX();
			int ddx2 = dx - left.getStepX();
			int ddz = dz + left.getStepZ();
			int ddz2 = dz - left.getStepZ();
			for (int k = -1; k <= 1; k++) {
				int dy = y + k;
				if (!this.isPart(world, ddx, dy, ddz, seek)) {
					if (call != null)
						call.onBlockFailure(world, ddx, dy, ddz, this.casing(seek));
					return false;
				}
				if (!this.isPart(world, ddx2, dy, ddz2, seek)) {
					if (call != null)
						call.onBlockFailure(world, ddx2, dy, ddz2, this.casing(seek));
					return false;
				}
				if (k != 0) {
					if (!this.isPart(world, dx, dy, dz, seek)) {
						if (call != null)
							call.onBlockFailure(world, dx, dy, dz, this.casing(seek));
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
			GeneratorPart seekCenter = GeneratorPart.HOUSING;
			for (int k = -2; k <= 2; k += 4) {
				int dy = y + k;
				if (i == 1 && k == 2)
					seekCenter = GeneratorPart.COIL;
				if (!this.isPart(world, ddx, dy, ddz, GeneratorPart.HOUSING)) {
					if (call != null)
						call.onBlockFailure(world, ddx, dy, ddz, this.casing(GeneratorPart.HOUSING));
					return false;
				}
				if (!this.isPart(world, ddx2, dy, ddz2, GeneratorPart.HOUSING)) {
					if (call != null)
						call.onBlockFailure(world, ddx2, dy, ddz2, this.casing(GeneratorPart.HOUSING));
					return false;
				}
				if (!this.isPart(world, dx, dy, dz, seekCenter)) {
					if (call != null)
						call.onBlockFailure(world, dx, dy, dz, this.casing(seekCenter));
					return false;
				}
			}

			ddx = dx + left.getStepX() * 2;
			ddx2 = dx - left.getStepX() * 2;
			ddz = dz + left.getStepZ() * 2;
			ddz2 = dz - left.getStepZ() * 2;

			for (int k = -1; k <= 1; k++) {
				int dy = y + k;
				if (!this.isPart(world, ddx, dy, ddz, GeneratorPart.HOUSING)) {
					if (call != null)
						call.onBlockFailure(world, ddx, dy, ddz, this.casing(GeneratorPart.HOUSING));
					return false;
				}
				if (!this.isPart(world, ddx2, dy, ddz2, GeneratorPart.HOUSING)) {
					if (call != null)
						call.onBlockFailure(world, ddx2, dy, ddz2, this.casing(GeneratorPart.HOUSING));
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
					if (!this.isPart(world, ddx, dy, ddz, GeneratorPart.HOUSING)) {
						if (call != null)
							call.onBlockFailure(world, ddx, dy, ddz, this.casing(GeneratorPart.HOUSING));
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
			if (!this.isPart(world, ddx, y + 2, ddz, GeneratorPart.HOUSING)) {
				if (call != null)
					call.onBlockFailure(world, ddx, y + 2, ddz, this.casing(GeneratorPart.HOUSING));
				return false;
			}
			if (!this.isPart(world, ddx2, y + 2, ddz2, GeneratorPart.HOUSING)) {
				if (call != null)
					call.onBlockFailure(world, ddx2, y + 2, ddz2, this.casing(GeneratorPart.HOUSING));
				return false;
			}
			if (!this.isPart(world, ddx, y - 2, ddz, GeneratorPart.HOUSING)) {
				if (call != null)
					call.onBlockFailure(world, ddx, y - 2, ddz, this.casing(GeneratorPart.HOUSING));
				return false;
			}
			if (!this.isPart(world, ddx2, y - 2, ddz2, GeneratorPart.HOUSING)) {
				if (call != null)
					call.onBlockFailure(world, ddx2, y - 2, ddz2, this.casing(GeneratorPart.HOUSING));
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
			if (ReactorTiles.getTE(world, c) == ReactorTiles.GENERATOR) {
				TileEntityReactorGenerator te = (TileEntityReactorGenerator) world.getBlockEntity(c);
				te.setHasMultiBlock(false);
			}
			else {
				BlockState cs = world.getBlockState(c);
				if (cs.is(this) && cs.getValue(FORMED))
					world.setBlock(c, cs.setValue(FORMED, false), 3);
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
			if (ReactorTiles.getTE(world, c) == ReactorTiles.GENERATOR) {
				TileEntityReactorGenerator te = (TileEntityReactorGenerator) world.getBlockEntity(c);
				te.setHasMultiBlock(true);
			}
			else {
				BlockState cs = world.getBlockState(c);
				if (cs.is(this) && !cs.getValue(FORMED))
					world.setBlock(c, cs.setValue(FORMED, true), 3);
			}
		}
	}

	@Override
	public boolean canTriggerMultiBlockCheck(Level world, BlockPos pos, BlockState state) {
		return state.getValue(PART) == GeneratorPart.CORE && !state.getValue(FORMED);
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
