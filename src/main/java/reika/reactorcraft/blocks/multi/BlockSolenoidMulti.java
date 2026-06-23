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
import reika.reactorcraft.auxiliary.NeutronBlock;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fusion.TileEntitySolenoidMagnet;
import reika.rotarycraft.api.interfaces.Transducerable;

public class BlockSolenoidMulti extends BlockReCMultiBlock implements Transducerable, NeutronBlock {

	/** The named casing parts of the solenoid multiblock (legacy variants 0..5). */
	public enum SolenoidPart implements StringRepresentable {
		FACE,      // 0: top/bottom face, inner band
		EDGE,      // 1: top/bottom face, outer band + corners
		WALL,      // 2: side wall, inner band
		WALL_EDGE, // 3: side wall, outer band
		SPOKE,     // 4: the radial spokes
		CORE;      // 5: the 3x2x3 core shell around the magnet TE

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

	public static final EnumProperty<SolenoidPart> PART = EnumProperty.create("part", SolenoidPart.class);

	public BlockSolenoidMulti(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(PART, SolenoidPart.FACE).setValue(FORMED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(PART);
	}

	private boolean isPart(BlockGetter world, int x, int y, int z, SolenoidPart p) {
		BlockState s = world.getBlockState(new BlockPos(x, y, z));
		return s.is(this) && s.getValue(PART) == p;
	}

	private BlockKey casing(SolenoidPart p) {
		return new BlockKey(this.defaultBlockState().setValue(PART, p));
	}

	@Override
	public Boolean checkForFullMultiBlock(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		int midX = blocks.getMinX() + blocks.getSizeX() / 2;
		int midY = blocks.getMinY() + blocks.getSizeY() / 2;
		int midZ = blocks.getMinZ() + blocks.getSizeZ() / 2;
		if (ReactorTiles.getTE(world, new BlockPos(midX, midY, midZ)) != ReactorTiles.SOLENOID) {
			if (call != null)
				call.onBlockFailure(world, midX, midY, midZ, new BlockKey(ReactorTiles.SOLENOID));
			return false;
		}

		if (!this.checkUpper(world, midX, midY, midZ, call))
			return false;
		if (!this.checkLower(world, midX, midY, midZ, call))
			return false;
		if (!this.checkMiddle(world, midX, midY, midZ, call))
			return false;
		if (!this.checkCorners(world, midX, midY, midZ, call))
			return false;
		if (!this.checkSpokes(world, midX, midY, midZ, call))
			return false;
		if (!this.checkCore(world, midX, midY, midZ, call))
			return false;

		return true;
	}

	private boolean check(Level world, int x, int y, int z, SolenoidPart seek, BlockMatchFailCallback call) {
		if (!this.isPart(world, x, y, z, seek)) {
			if (call != null)
				call.onBlockFailure(world, x, y, z, this.casing(seek));
			return false;
		}
		return true;
	}

	private boolean checkCore(Level world, int midX, int midY, int midZ, BlockMatchFailCallback call) {
		for (int i = -1; i <= 1; i++) {
			for (int j = 0; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					if (i != 0 || j != 0 || k != 0) {
						if (!this.check(world, midX + i, midY + j, midZ + k, SolenoidPart.CORE, call))
							return false;
					}
				}
			}
		}
		return true;
	}

	private boolean checkSpokes(Level world, int midX, int midY, int midZ, BlockMatchFailCallback call) {
		for (int i = 2; i <= 7; i++) {
			if (!this.check(world, midX + i, midY, midZ, SolenoidPart.SPOKE, call))
				return false;
			if (!this.check(world, midX - i, midY, midZ, SolenoidPart.SPOKE, call))
				return false;
			if (!this.check(world, midX, midY, midZ + i, SolenoidPart.SPOKE, call))
				return false;
			if (!this.check(world, midX, midY, midZ - i, SolenoidPart.SPOKE, call))
				return false;

			if (i < 6) {
				if (!this.check(world, midX + i, midY, midZ + i, SolenoidPart.SPOKE, call))
					return false;
				if (!this.check(world, midX - i, midY, midZ + i, SolenoidPart.SPOKE, call))
					return false;
				if (!this.check(world, midX + i, midY, midZ - i, SolenoidPart.SPOKE, call))
					return false;
				if (!this.check(world, midX - i, midY, midZ - i, SolenoidPart.SPOKE, call))
					return false;
			}
		}
		return true;
	}

	private boolean checkCorners(Level world, int midX, int midY, int midZ, BlockMatchFailCallback call) {
		for (int i = 6; i <= 6; i++) {
			if (!this.check(world, midX - i, midY + 1, midZ - i, SolenoidPart.EDGE, call))
				return false;
			if (!this.check(world, midX - i, midY - 1, midZ - i, SolenoidPart.EDGE, call))
				return false;
		}
		return true;
	}

	private boolean checkMiddle(Level world, int midX, int midY, int midZ, BlockMatchFailCallback call) {
		for (int i = -5; i <= 5; i++) {
			int d = Math.abs(i) >= 4 ? 7 : 8;
			int dy = midY;
			SolenoidPart part = Math.abs(i) >= 3 ? SolenoidPart.WALL_EDGE : SolenoidPart.WALL;

			if (!this.check(world, midX - d, dy, midZ + i, part, call))
				return false;
			if (!this.check(world, midX + d, dy, midZ + i, part, call))
				return false;
			if (!this.check(world, midX + i, dy, midZ + d, part, call))
				return false;
			if (!this.check(world, midX + i, dy, midZ - d, part, call))
				return false;
		}
		return true;
	}

	private boolean checkLower(Level world, int midX, int midY, int midZ, BlockMatchFailCallback call) {
		for (int i = -5; i <= 5; i++) {
			int d = Math.abs(i) >= 4 ? 7 : 8;
			int dy = midY - 1;
			SolenoidPart part = Math.abs(i) >= 3 ? SolenoidPart.EDGE : SolenoidPart.FACE;

			if (!this.check(world, midX - d, dy, midZ + i, part, call))
				return false;
			if (!this.check(world, midX + d, dy, midZ + i, part, call))
				return false;
			if (!this.check(world, midX + i, dy, midZ + d, part, call))
				return false;
			if (!this.check(world, midX + i, dy, midZ - d, part, call))
				return false;
		}
		return true;
	}

	private boolean checkUpper(Level world, int midX, int midY, int midZ, BlockMatchFailCallback call) {
		for (int i = -5; i <= 5; i++) {
			int d = Math.abs(i) >= 4 ? 7 : 8;
			int dy = midY + 1;
			SolenoidPart part = Math.abs(i) >= 3 ? SolenoidPart.EDGE : SolenoidPart.FACE;

			if (!this.check(world, midX - d, dy, midZ + i, part, call))
				return false;
			if (!this.check(world, midX + d, dy, midZ + i, part, call))
				return false;
			if (!this.check(world, midX + i, dy, midZ + d, part, call))
				return false;
			if (!this.check(world, midX + i, dy, midZ - d, part, call))
				return false;
		}
		return true;
	}

	@Override
	public void breakMultiBlock(Level world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		blocks.recursiveAddWithBoundsRanged(world, x + 1, y, z, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		blocks.recursiveAddWithBoundsRanged(world, x - 1, y, z, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y + 1, z, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y - 1, z, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z + 1, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z - 1, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		for (int i = 0; i < blocks.getSize(); i++) {
			BlockPos c = blocks.getNthBlock(i);
			BlockState cs = world.getBlockState(c);
			if (cs.is(this) && cs.getValue(FORMED))
				world.setBlock(c, cs.setValue(FORMED, false), 3);
		}
		int midX = blocks.getMidX();
		int midY = blocks.getMidY();
		int midZ = blocks.getMidZ();
		if (ReactorTiles.getTE(world, new BlockPos(midX, midY, midZ)) == ReactorTiles.SOLENOID) {
			TileEntitySolenoidMagnet te = (TileEntitySolenoidMagnet) world.getBlockEntity(new BlockPos(midX, midY, midZ));
			te.setHasMultiBlock(false);
		}
	}

	@Override
	protected void onCreateFullMultiBlock(Level world, int x, int y, int z, Boolean complete) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		for (int i = 0; i < blocks.getSize(); i++) {
			BlockPos c = blocks.getNthBlock(i);
			BlockState cs = world.getBlockState(c);
			if (cs.is(this) && !cs.getValue(FORMED))
				world.setBlock(c, cs.setValue(FORMED, true), 3);
		}
		int midX = blocks.getMidX();
		int midY = blocks.getMidY();
		int midZ = blocks.getMidZ();
		if (ReactorTiles.getTE(world, new BlockPos(midX, midY, midZ)) == ReactorTiles.SOLENOID) {
			TileEntitySolenoidMagnet te = (TileEntitySolenoidMagnet) world.getBlockEntity(new BlockPos(midX, midY, midZ));
			te.setHasMultiBlock(true);
		}
	}

	@Override
	public boolean canTriggerMultiBlockCheck(Level world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

	@Override
	protected BlockEntity getTileEntityForPosition(Level world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x - 20, y - 3, z - 20, x + 20, y + 3, z + 20, 1);
		int midX = blocks.getMinX() + blocks.getSizeX() / 2;
		int midY = blocks.getMinY() + blocks.getSizeY() / 2;
		int midZ = blocks.getMinZ() + blocks.getSizeZ() / 2;
		if (ReactorTiles.getTE(world, new BlockPos(midX, midY, midZ)) != ReactorTiles.SOLENOID)
			return null;
		return world.getBlockEntity(new BlockPos(midX, midY, midZ));
	}

}
