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
import java.util.Locale;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.dragonapi.instantiable.data.blockstruct.filledblockarray.BlockMatchFailCallback;
import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.reactorcraft.auxiliary.NeutronBlock;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.tileentities.fusion.TileEntitySolenoidMagnet;
import reika.rotarycraft.api.interfaces.Transducerable;

/**
 * One of the six named solenoid-casing blocks (the multiblock shell around the central
 * {@link TileEntitySolenoidMagnet}). Legacy ReactorCraft modelled these as a single block with six
 * metadata variants; the 26.2 port splits them into distinct blocks (face / edge / wall / wall_edge /
 * spoke / shell) so each is individually named and placeable.
 *
 * <p>The multiblock validation no longer flood-fills. The structure is fully deterministic relative to
 * the central magnet, so {@link #layout(BlockPos)} enumerates every casing position + expected block
 * from the centre, and the validator simply (a) scans a small region around the triggering block for
 * the {@code SOLENOID} core and (b) checks every laid-out position holds the right block. That single
 * layout method is the one source of truth shared by validation, forming, breaking and the
 * {@code /solenoiddebug} command.</p>
 */
public class BlockSolenoidCasing extends BlockReCMultiBlock implements Transducerable, NeutronBlock {

	/** The named casing parts (was BlockSolenoidMulti's metadata 0..5). */
	public enum SolenoidPart implements StringRepresentable {
		FACE,      // top/bottom face, inner band
		EDGE,      // top/bottom face, outer band + corners
		WALL,      // side wall, inner band
		WALL_EDGE, // side wall, outer band
		SPOKE,     // the radial spokes
		SHELL;     // the 3x2x3 shell around the magnet TE (legacy "CORE")

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

	/** A casing position's expected part. The spoke rod is rotationally symmetric, so no orientation. */
	public record CasingSpec(SolenoidPart part) {}

	// The core sits at most 8 blocks away horizontally (walls at distance 8) and within 1 vertically.
	private static final int SCAN_H = 9;
	private static final int SCAN_V = 2;

	// Flags for toggling the FORMED flag: normal update + client sync, but SKIP onPlace. Without
	// UPDATE_SKIP_ON_PLACE, onPlace fires on every setBlockState (not just block changes), so setting
	// FORMED would re-run the assembly check -- and during a break the block being removed is still
	// present, so the structure re-validates and instantly re-forms, leaving casings stuck invisible.
	private static final int FORMED_FLAGS = Block.UPDATE_ALL | Block.UPDATE_SKIP_ON_PLACE;

	private final SolenoidPart part;

	public BlockSolenoidCasing(BlockBehaviour.Properties properties, SolenoidPart part) {
		super(properties);
		this.part = part;
		this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
	}

	public SolenoidPart getPart() {
		return part;
	}

	// --- layout: single source of truth (mirrors the legacy check* loops exactly) ---

	/** Maps registry part -> its concrete block. */
	public static Block blockFor(SolenoidPart p) {
		return switch (p) {
			case FACE -> ReactorBlocks.FERROMAGNETIC_BASE.get();
			case EDGE -> ReactorBlocks.MAGNETIC_LINKAGE.get();
			case WALL -> ReactorBlocks.CENTRAL_MAGNET.get();
			case WALL_EDGE -> ReactorBlocks.AUXILIARY_MAGNET.get();
			case SPOKE -> ReactorBlocks.HYSTERESIS_ROD.get();
			case SHELL -> ReactorBlocks.SOLENOID_HUB.get();
		};
	}

	/**
	 * Every casing position of a solenoid centred on {@code mid} (the magnet block), mapped to the part
	 * and — for spokes — the horizontal axis it runs along. Insertion order is stable so callers that
	 * want to "skip the last" get a deterministic block.
	 */
	public static Map<BlockPos, CasingSpec> layout(BlockPos mid) {
		int midX = mid.getX(), midY = mid.getY(), midZ = mid.getZ();
		Map<BlockPos, CasingSpec> map = new LinkedHashMap<>();

		// SHELL: 3x2x3 around the magnet (centre excluded -- that's the SOLENOID block).
		for (int i = -1; i <= 1; i++)
			for (int j = 0; j <= 1; j++)
				for (int k = -1; k <= 1; k++)
					if (i != 0 || j != 0 || k != 0)
						map.put(new BlockPos(midX + i, midY + j, midZ + k), new CasingSpec(SolenoidPart.SHELL));

		// SPOKES: radial arms out to 7, diagonals out to 5.
		for (int i = 2; i <= 7; i++) {
			map.put(new BlockPos(midX + i, midY, midZ), new CasingSpec(SolenoidPart.SPOKE));
			map.put(new BlockPos(midX - i, midY, midZ), new CasingSpec(SolenoidPart.SPOKE));
			map.put(new BlockPos(midX, midY, midZ + i), new CasingSpec(SolenoidPart.SPOKE));
			map.put(new BlockPos(midX, midY, midZ - i), new CasingSpec(SolenoidPart.SPOKE));
			if (i < 6) {
				map.put(new BlockPos(midX + i, midY, midZ + i), new CasingSpec(SolenoidPart.SPOKE));
				map.put(new BlockPos(midX - i, midY, midZ + i), new CasingSpec(SolenoidPart.SPOKE));
				map.put(new BlockPos(midX + i, midY, midZ - i), new CasingSpec(SolenoidPart.SPOKE));
				map.put(new BlockPos(midX - i, midY, midZ - i), new CasingSpec(SolenoidPart.SPOKE));
			}
		}

		// CORNER POSTS: a 3-tall stack (EDGE / WALL_EDGE / EDGE) at each of the four (+/-6, +/-6)
		// diagonal corners, just beyond the diagonal spoke ends. NOTE the legacy validator
		// (BlockSolenoidMulti.checkCorners) only checked TWO of these twelve blocks -- so it assembled
		// with the corner posts largely absent. The authoritative structure (SolenoidStructure) places
		// all four posts; we validate the full set so assembly requires the complete build.
		for (int sx : new int[]{-6, 6}) {
			for (int sz : new int[]{-6, 6}) {
				map.put(new BlockPos(midX + sx, midY + 1, midZ + sz), new CasingSpec(SolenoidPart.EDGE));
				map.put(new BlockPos(midX + sx, midY, midZ + sz), new CasingSpec(SolenoidPart.WALL_EDGE));
				map.put(new BlockPos(midX + sx, midY - 1, midZ + sz), new CasingSpec(SolenoidPart.EDGE));
			}
		}

		// The three ring layers (middle wall + lower/upper faces).
		addRing(map, midX, midY, midZ, SolenoidPart.WALL, SolenoidPart.WALL_EDGE);
		addRing(map, midX, midY - 1, midZ, SolenoidPart.FACE, SolenoidPart.EDGE);
		addRing(map, midX, midY + 1, midZ, SolenoidPart.FACE, SolenoidPart.EDGE);

		return map;
	}

	private static void addRing(Map<BlockPos, CasingSpec> map, int midX, int dy, int midZ, SolenoidPart inner, SolenoidPart outer) {
		for (int i = -5; i <= 5; i++) {
			int d = Math.abs(i) >= 4 ? 7 : 8;
			SolenoidPart p = Math.abs(i) >= 3 ? outer : inner;
			map.put(new BlockPos(midX - d, dy, midZ + i), new CasingSpec(p));
			map.put(new BlockPos(midX + d, dy, midZ + i), new CasingSpec(p));
			map.put(new BlockPos(midX + i, dy, midZ + d), new CasingSpec(p));
			map.put(new BlockPos(midX + i, dy, midZ - d), new CasingSpec(p));
		}
	}

	/** Scan a small region around {@code near} for the solenoid magnet block; null if none. */
	public static BlockPos findCore(Level world, BlockPos near) {
		Block core = ReactorBlocks.SOLENOID.get();
		for (int dy = -SCAN_V; dy <= SCAN_V; dy++)
			for (int dx = -SCAN_H; dx <= SCAN_H; dx++)
				for (int dz = -SCAN_H; dz <= SCAN_H; dz++) {
					BlockPos p = near.offset(dx, dy, dz);
					if (world.getBlockState(p).is(core))
						return p;
				}
		return null;
	}

	// --- multiblock hooks ---

	/** Public entry point for the magnet TE to (re)assemble on load. */
	public boolean tryAssemble(Level world, int x, int y, int z, BlockMatchFailCallback call) {
		Boolean ret = this.checkForFullMultiBlock(world, x, y, z, null, call);
		if (Boolean.TRUE.equals(ret)) {
			this.onCreateFullMultiBlock(world, x, y, z, ret);
			return true;
		}
		return false;
	}

	@Override
	public Boolean checkForFullMultiBlock(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		BlockPos core = findCore(world, new BlockPos(x, y, z));
		if (core == null) {
			if (call != null)
				call.onBlockFailure(world, x, y, z, new BlockKey(ReactorBlocks.SOLENOID.get()));
			return false;
		}
		for (Map.Entry<BlockPos, CasingSpec> e : layout(core).entrySet()) {
			BlockPos p = e.getKey();
			Block expected = blockFor(e.getValue().part());
			if (!world.getBlockState(p).is(expected)) {
				if (call != null)
					call.onBlockFailure(world, p.getX(), p.getY(), p.getZ(), new BlockKey(expected));
				return false;
			}
		}
		return true;
	}

	@Override
	protected void onCreateFullMultiBlock(Level world, int x, int y, int z, Boolean complete) {
		BlockPos core = findCore(world, new BlockPos(x, y, z));
		if (core == null)
			return;
		for (BlockPos p : layout(core).keySet()) {
			BlockState cs = world.getBlockState(p);
			if (cs.getBlock() instanceof BlockSolenoidCasing && !cs.getValue(FORMED))
				world.setBlock(p, cs.setValue(FORMED, true), FORMED_FLAGS);
		}
		if (world.getBlockEntity(core) instanceof TileEntitySolenoidMagnet te)
			te.setHasMultiBlock(true);
	}

	@Override
	public void breakMultiBlock(Level world, int x, int y, int z) {
		BlockPos core = findCore(world, new BlockPos(x, y, z));
		if (core == null)
			return;
		for (BlockPos p : layout(core).keySet()) {
			BlockState cs = world.getBlockState(p);
			if (cs.getBlock() instanceof BlockSolenoidCasing && cs.getValue(FORMED))
				world.setBlock(p, cs.setValue(FORMED, false), FORMED_FLAGS);
		}
		if (world.getBlockEntity(core) instanceof TileEntitySolenoidMagnet te)
			te.setHasMultiBlock(false);
	}

	@Override
	public boolean canTriggerMultiBlockCheck(Level world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	protected BlockEntity getTileEntityForPosition(Level world, int x, int y, int z) {
		BlockPos core = findCore(world, new BlockPos(x, y, z));
		return core == null ? null : world.getBlockEntity(core);
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}
}
