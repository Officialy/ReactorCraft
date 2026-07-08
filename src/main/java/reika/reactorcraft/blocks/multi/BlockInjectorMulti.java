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
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.tileentities.fusion.TileEntityFusionInjector;

/**
 * Fusion plasma injector casing. Eight part blocks forming the directional ramp assembly from the
 * InjectorStructure blueprint: a stepped roof (top/upper-corner lines descending toward the muzzle),
 * base and lower-corner floor lines, side panels, the induction-coil column behind the injector, the
 * corner columns, the hysteresis-core interior line, and the magnetic-pipe barrel out the front.
 * {@link #layout(BlockPos, Direction)} (injector machine position + its facing) is the single source
 * of truth. The structure is mirror-symmetric so left/right handedness does not matter.
 */
public class BlockInjectorMulti extends BlockReCMultiBlock {

	public enum InjectorPart implements StringRepresentable {
		BASE("plasma_injector_base"),
		LOWER_CORNER("plasma_injector_lower_corner"),
		SIDE_PANEL("plasma_injector_side_panel"),
		TOP("plasma_injector_top"),
		UPPER_CORNER("plasma_injector_upper_corner"),
		INDUCTION_COIL("plasma_injector_induction_coil"),
		COLUMN("plasma_injector_column"),
		HYSTERESIS_CORE("plasma_injector_hysteresis_core");

		public static final InjectorPart[] list = values();

		private final String id;

		InjectorPart(String s) {
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

	private static final int SCAN = 9;

	private final InjectorPart part;

	public BlockInjectorMulti(BlockBehaviour.Properties properties, InjectorPart part) {
		super(properties);
		this.part = part;
	}

	public InjectorPart getPart() {
		return part;
	}

	public static Block blockFor(InjectorPart p) {
		return ReactorBlocks.INJECTOR_CASINGS.get(p).get();
	}

	/**
	 * Every non-machine position of an injector assembly whose INJECTOR machine sits at
	 * {@code injector} firing toward {@code dir}. Blueprint anchor = injector - dir*2; later writes
	 * win, matching the blueprint's overwrites.
	 */
	public static Map<BlockPos, Block> layout(BlockPos injector, Direction dir) {
		BlockPos o = injector.relative(dir, -2);
		Direction left = dir.getCounterClockWise();
		Map<BlockPos, Block> map = new LinkedHashMap<>();

		// Stepped roof side lines (upper corners) and floor side lines (lower corners).
		for (int i = 0; i <= 4; i++) {
			map.put(o.relative(dir, i).relative(left, 1).above(3), blockFor(InjectorPart.UPPER_CORNER));
			map.put(o.relative(dir, i).relative(left, -1).above(3), blockFor(InjectorPart.UPPER_CORNER));
		}
		for (int i = 5; i <= 6; i++) {
			map.put(o.relative(dir, i).relative(left, 1).above(2), blockFor(InjectorPart.UPPER_CORNER));
			map.put(o.relative(dir, i).relative(left, -1).above(2), blockFor(InjectorPart.UPPER_CORNER));
		}
		for (int i = 7; i <= 8; i++) {
			map.put(o.relative(dir, i).relative(left, 1).above(1), blockFor(InjectorPart.UPPER_CORNER));
			map.put(o.relative(dir, i).relative(left, -1).above(1), blockFor(InjectorPart.UPPER_CORNER));
		}
		for (int i = 0; i <= 8; i++) {
			map.put(o.relative(dir, i).relative(left, 1).below(1), blockFor(InjectorPart.LOWER_CORNER));
			map.put(o.relative(dir, i).relative(left, -1).below(1), blockFor(InjectorPart.LOWER_CORNER));
		}
		// Rear corner columns + muzzle columns.
		for (int k = 0; k <= 2; k++) {
			map.put(o.relative(left, 1).above(k), blockFor(InjectorPart.COLUMN));
			map.put(o.relative(left, -1).above(k), blockFor(InjectorPart.COLUMN));
		}
		map.put(o.relative(dir, 8).relative(left, 1), blockFor(InjectorPart.COLUMN));
		map.put(o.relative(dir, 8).relative(left, -1), blockFor(InjectorPart.COLUMN));
		// Stepped roof centre line (tops) and floor centre line (bases).
		for (int i = 0; i <= 4; i++)
			map.put(o.relative(dir, i).above(3), blockFor(InjectorPart.TOP));
		for (int i = 5; i <= 6; i++)
			map.put(o.relative(dir, i).above(2), blockFor(InjectorPart.TOP));
		for (int i = 7; i <= 8; i++)
			map.put(o.relative(dir, i).above(1), blockFor(InjectorPart.TOP));
		for (int i = 0; i <= 8; i++)
			map.put(o.relative(dir, i).below(1), blockFor(InjectorPart.BASE));
		// Side panels.
		map.put(o.relative(dir, 1).relative(left, 1), blockFor(InjectorPart.SIDE_PANEL));
		map.put(o.relative(dir, 1).relative(left, -1), blockFor(InjectorPart.SIDE_PANEL));
		for (int i = 3; i <= 7; i++) {
			map.put(o.relative(dir, i).relative(left, 1), blockFor(InjectorPart.SIDE_PANEL));
			map.put(o.relative(dir, i).relative(left, -1), blockFor(InjectorPart.SIDE_PANEL));
		}
		for (int i = 1; i <= 6; i++) {
			map.put(o.relative(dir, i).relative(left, 1).above(1), blockFor(InjectorPart.SIDE_PANEL));
			map.put(o.relative(dir, i).relative(left, -1).above(1), blockFor(InjectorPart.SIDE_PANEL));
		}
		for (int i = 1; i <= 4; i++) {
			map.put(o.relative(dir, i).relative(left, 1).above(2), blockFor(InjectorPart.SIDE_PANEL));
			map.put(o.relative(dir, i).relative(left, -1).above(2), blockFor(InjectorPart.SIDE_PANEL));
		}
		// Induction coil column (rear centre) + hysteresis core interior line.
		for (int k = 0; k <= 2; k++)
			map.put(o.above(k), blockFor(InjectorPart.INDUCTION_COIL));
		map.put(o.relative(dir, 1), blockFor(InjectorPart.HYSTERESIS_CORE));
		for (int i = 1; i <= 6; i++)
			map.put(o.relative(dir, i).above(1), blockFor(InjectorPart.HYSTERESIS_CORE));
		for (int i = 1; i <= 4; i++)
			map.put(o.relative(dir, i).above(2), blockFor(InjectorPart.HYSTERESIS_CORE));
		// Magnetic-pipe barrel out the muzzle.
		for (int i = 3; i <= 8; i++)
			map.put(o.relative(dir, i), ReactorBlocks.MAGNETPIPE.get());
		// The injector machine itself sits at o + dir*2.
		map.remove(o.relative(dir, 2));
		return map;
	}

	/** Scan around {@code near} for the injector machine block; null if none. */
	public static BlockPos findInjector(Level world, BlockPos near) {
		Block injector = ReactorBlocks.INJECTOR.get();
		for (BlockPos p : BlockPos.betweenClosed(near.offset(-SCAN, -SCAN, -SCAN), near.offset(SCAN, SCAN, SCAN))) {
			if (world.getBlockState(p).is(injector))
				return p.immutable();
		}
		return null;
	}

	private static Direction facingOf(Level world, BlockPos injector) {
		BlockState state = world.getBlockState(injector);
		Direction d = state.hasProperty(BlockReactorMachine.FACING) ? state.getValue(BlockReactorMachine.FACING) : Direction.NORTH;
		return d.getAxis().isHorizontal() ? d : Direction.NORTH;
	}

	public static boolean isComplete(Level world, BlockPos injector) {
		Direction dir = facingOf(world, injector);
		for (Map.Entry<BlockPos, Block> e : layout(injector, dir).entrySet()) {
			if (world.getBlockState(e.getKey()).getBlock() != e.getValue())
				return false;
		}
		return true;
	}

	@Override
	public Boolean checkForFullMultiBlock(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		BlockPos injector = findInjector(world, new BlockPos(x, y, z));
		if (injector == null) {
			if (call != null)
				call.onBlockFailure(world, x, y, z, new BlockKey(ReactorBlocks.INJECTOR.get()));
			return false;
		}
		Direction facing = facingOf(world, injector);
		for (Map.Entry<BlockPos, Block> e : layout(injector, facing).entrySet()) {
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
		BlockPos injector = findInjector(world, near);
		if (injector != null && world.getBlockEntity(injector) instanceof TileEntityFusionInjector te)
			te.setHasMultiBlock(formed && isComplete(world, injector));
	}

	@Override
	public boolean canTriggerMultiBlockCheck(Level world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	protected BlockEntity getTileEntityForPosition(Level world, int x, int y, int z) {
		BlockPos injector = findInjector(world, new BlockPos(x, y, z));
		return injector == null ? null : world.getBlockEntity(injector);
	}
}
