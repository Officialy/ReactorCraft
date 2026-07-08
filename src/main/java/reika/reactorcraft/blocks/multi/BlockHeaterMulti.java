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

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.dragonapi.instantiable.data.blockstruct.filledblockarray.BlockMatchFailCallback;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.tileentities.fusion.TileEntityFusionHeater;
import reika.rotarycraft.registry.RotaryBlocks;

/**
 * Fusion preheater casing. The five legacy metadata parts are individual blocks; the assembly is a
 * 5x5x5 shell (corner/edge/face) around a 3x3 thermal-insulation core with the heater at the centre,
 * a 3x3 cap ring above, a magnetic-pipe column out the top, the RotaryCraft water pipe line through
 * the heater, and two laser concentration lenses behind it — transcribed exactly from the
 * authoritative PreheaterStructure blueprint. {@link #layout(BlockPos)} is the single source of truth.
 */
public class BlockHeaterMulti extends BlockReCMultiBlock {

	public enum HeaterPart implements StringRepresentable {
		LENS("laser_concentration_lens"),
		CORE("thermal_insulation_core"),
		CORNER("preheater_housing_corner"),
		EDGE("preheater_housing_edge"),
		FACE("preheater_housing_face");

		public static final HeaterPart[] list = values();

		private final String id;

		HeaterPart(String s) {
			id = s;
		}

		public String id() {
			return id;
		}

		@Override
		public String getSerializedName() {
			return id;
		}
	}

	// How far from a placed casing block to look for the heater machine.
	private static final int SCAN = 6;

	private final HeaterPart part;

	public BlockHeaterMulti(BlockBehaviour.Properties properties, HeaterPart part) {
		super(properties);
		this.part = part;
	}

	public HeaterPart getPart() {
		return part;
	}

	public static Block blockFor(HeaterPart p) {
		return ReactorBlocks.HEATER_CASINGS.get(p).get();
	}

	/**
	 * Every non-machine position of a preheater whose HEATER machine sits at {@code heater}, mapped
	 * to the exact block expected there. Structure origin = heater - (2,2,2); later writes win, like
	 * the blueprint's overwrites.
	 */
	public static Map<BlockPos, Block> layout(BlockPos heater) {
		int x = heater.getX() - 2, y = heater.getY() - 2, z = heater.getZ() - 2;
		Map<BlockPos, Block> map = new LinkedHashMap<>();

		for (int i = 0; i < 5; i++) {
			for (int k = 0; k < 5; k++) {
				for (int h = 0; h < 5; h++) {
					boolean corner = (i == 0 || i == 4) && (k == 0 || k == 4) && (h == 0 || h == 4);
					boolean edge = i == 0 || i == 4 || k == 0 || k == 4;
					HeaterPart p = corner ? HeaterPart.CORNER : edge ? HeaterPart.EDGE : HeaterPart.FACE;
					map.put(new BlockPos(x + i, y + h, z + k), blockFor(p));
					if (h > 0 && !edge) {
						map.put(new BlockPos(x + i, y + h, z + k), blockFor(HeaterPart.CORE));
						if (i == 2 && k == 2 && h >= 2) {
							if (h > 2)
								map.put(new BlockPos(x + i, y + h, z + k), ReactorBlocks.MAGNETPIPE.get());
							else
								map.remove(new BlockPos(x + i, y + h, z + k)); // the heater itself
						}
					}
				}
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 3; k++) {
				boolean corner = (i == 0 || i == 2) && (k == 0 || k == 2);
				boolean edge = i == 0 || i == 2 || k == 0 || k == 2;
				HeaterPart p = corner ? HeaterPart.CORNER : edge ? HeaterPart.EDGE : HeaterPart.FACE;
				map.put(new BlockPos(x + 1 + i, y + 5, z + 1 + k), blockFor(p));
			}
		}
		for (int i = 1; i <= 3; i++) {
			for (int k = 1; k <= 3; k++) {
				map.put(new BlockPos(x + i, y + k, z), blockFor(HeaterPart.FACE));
				map.put(new BlockPos(x + i, y + k, z + 4), blockFor(HeaterPart.FACE));
				map.put(new BlockPos(x, y + k, z + i), blockFor(HeaterPart.FACE));
				map.put(new BlockPos(x + 4, y + k, z + i), blockFor(HeaterPart.FACE));
			}
		}
		map.put(new BlockPos(x + 2, y + 5, z + 2), ReactorBlocks.MAGNETPIPE.get());
		map.put(new BlockPos(x + 2, y + 6, z + 2), ReactorBlocks.MAGNETPIPE.get());
		for (int i = 0; i < 5; i++) {
			if (i != 2)
				map.put(new BlockPos(x + i, y + 2, z + 2), RotaryBlocks.FLUID_PIPE.get());
		}
		map.put(new BlockPos(x + 2, y + 2, z + 3), blockFor(HeaterPart.LENS));
		map.put(new BlockPos(x + 2, y + 2, z + 4), blockFor(HeaterPart.LENS));
		return map;
	}

	/** Scan around {@code near} for the heater machine block; null if none. */
	public static BlockPos findHeater(Level world, BlockPos near) {
		Block heater = ReactorBlocks.HEATER.get();
		for (BlockPos p : BlockPos.betweenClosed(near.offset(-SCAN, -SCAN, -SCAN), near.offset(SCAN, SCAN, SCAN))) {
			if (world.getBlockState(p).is(heater))
				return p.immutable();
		}
		return null;
	}

	public static boolean isComplete(Level world, BlockPos heater) {
		for (Map.Entry<BlockPos, Block> e : layout(heater).entrySet()) {
			if (world.getBlockState(e.getKey()).getBlock() != e.getValue())
				return false;
		}
		return true;
	}

	@Override
	public Boolean checkForFullMultiBlock(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		BlockPos heater = findHeater(world, new BlockPos(x, y, z));
		if (heater == null) {
			if (call != null)
				call.onBlockFailure(world, x, y, z, new BlockKey(ReactorBlocks.HEATER.get()));
			return false;
		}
		for (Map.Entry<BlockPos, Block> e : layout(heater).entrySet()) {
			if (world.getBlockState(e.getKey()).getBlock() != e.getValue()) {
				if (call != null)
					call.onBlockFailure(world, e.getKey().getX(), e.getKey().getY(), e.getKey().getZ(), new BlockKey(e.getValue()));
				return false;
			}
		}
		return true;
	}

	@Override
	protected void onCreateFullMultiBlock(Level world, int x, int y, int z, Boolean ret) {
		this.setFormed(world, new BlockPos(x, y, z), true);
	}

	@Override
	public void breakMultiBlock(Level world, int x, int y, int z) {
		this.setFormed(world, new BlockPos(x, y, z), false);
	}

	private void setFormed(Level world, BlockPos near, boolean formed) {
		BlockPos heater = findHeater(world, near);
		if (heater != null && world.getBlockEntity(heater) instanceof TileEntityFusionHeater te)
			te.setHasMultiBlock(formed && isComplete(world, heater));
	}

	@Override
	public boolean canTriggerMultiBlockCheck(Level world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	protected BlockEntity getTileEntityForPosition(Level world, int x, int y, int z) {
		BlockPos heater = findHeater(world, new BlockPos(x, y, z));
		return heater == null ? null : world.getBlockEntity(heater);
	}
}
