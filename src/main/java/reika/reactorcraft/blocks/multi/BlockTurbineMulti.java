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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import reika.dragonapi.instantiable.data.blockstruct.SlicedBlockBlueprint;
import reika.dragonapi.instantiable.data.blockstruct.StructuredBlockArray;
import reika.dragonapi.instantiable.data.blockstruct.filledblockarray.BlockMatchFailCallback;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamInjector;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

public class BlockTurbineMulti extends BlockReCMultiBlock implements EntityBlock {

	/** The named casing parts of the turbine multiblock (legacy variants 0..2 / 't','h','e'). */
	public enum TurbinePart implements StringRepresentable {
		SHELL,   // 0 / 't': the turbine shell
		HOUSING, // 1 / 'h': the outer housing
		ELEMENT; // 2 / 'e': the steam-injector element (hosts the BE)

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

	public static final EnumProperty<TurbinePart> PART = EnumProperty.create("part", TurbinePart.class);

	private final SlicedBlockBlueprint setup;

	public BlockTurbineMulti(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(PART, TurbinePart.SHELL).setValue(FORMED, false));
		setup = new SlicedBlockBlueprint();
		this.initMap();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(PART);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(PART) == TurbinePart.ELEMENT ? new TileEntitySteamInjector(pos, state) : null;
	}

	public int getThickness(int stage) {
		return setup.getHeight(stage)/2;
	}

	private void initMap() {
		setup.addMapping('t', this.defaultBlockState().setValue(PART, TurbinePart.SHELL));
		setup.addMapping('h', this.defaultBlockState().setValue(PART, TurbinePart.HOUSING));
		setup.addMapping('e', this.defaultBlockState().setValue(PART, TurbinePart.ELEMENT));
		setup.addAntiMapping('b', this);

		setup.addSlice(
				"xbbhhhhhbbx",
				"bbhhhthhhbb",
				"bhhttttthhb",
				"hhttttttthh",
				"hhttttttthh",
				"httttxtttth",
				"hhttttttthh",
				"hhttttttthh",
				"bhhttttthhb",
				"bbhhhthhhbb",
				"xbbhhhhhbbx"
				);

		setup.addSlice(
				"xxbbbbbbbxx",
				"xbbhhhhhbbx",
				"bbhhttthhbb",
				"bhhttttthhb",
				"bhttttttthb",
				"bhtttxttthb",
				"bhttttttthb",
				"bhhttttthhb",
				"bbhhttthhbb",
				"xbbhhhhhbbx",
				"xxbbbbbbbxx"
				);

		setup.addSlice(
				"xxxbbbbbxxx",
				"xbbbhhhbbbx",
				"xbhhhhhhhbx",
				"bbhhttthhbb",
				"bhhttttthhb",
				"bhhttxtthhb",
				"bhhttttthhb",
				"bbhhttthhbb",
				"xbhhhhhhhbx",
				"xbbbhhhbbbx",
				"xxxbbbbbxxx"
				);

		setup.addSlice(
				"xxxxxxxxxxx",
				"xxbbbbbbbxx",
				"xbbhhhhhbbx",
				"xbhhttthhbx",
				"xbhttttthbx",
				"xbhttxtthbx",
				"xbhttttthbx",
				"xbhhttthhbx",
				"xbbhhhhhbbx",
				"xxbbbbbbbxx",
				"xxxxxxxxxxx"
				);

		setup.addSlice(
				"xxxxxxxxxxx",
				"xxxbbbbbxxx",
				"xxbbhhhbbxx",
				"xbbhhhhhbbx",
				"xbhhttthhbx",
				"xbhhtxthhbx",
				"xbhhttthhbx",
				"xbbhhhhhbbx",
				"xxbbhhhbbxx",
				"xxxbbbbbxxx",
				"xxxxxxxxxxx"
				);

		setup.addSlice(
				"xxxxxxxxxxx",
				"xxxxxxxxxxx",
				"xxbbbbbbbxx",
				"xxbhhhhhbxx",
				"xxbhhthhbxx",
				"xxbhtxthbxx",
				"xxbhhthhbxx",
				"xxbhhhhhbxx",
				"xxbbbbbbbxx",
				"xxxxxxxxxxx",
				"xxxxxxxxxxx"
				);

		setup.addSlice(
				"xxxxxxxxxxx",
				"xxxxxxxxxxx",
				"xxxbbbbbxxx",
				"xxbbhhhbbxx",
				"xxbhhhhhbxx",
				"xxbhhxhhbxx",
				"xxbhhhhhbxx",
				"xxbbhhhbbxx",
				"xxxbbbbbxxx",
				"xxxxxxxxxxx",
				"xxxxxxxxxxx"
				);

		setup.addSlice(
				"xxxxxxxxxxx",
				"xxxxxxxxxxx",
				"xxxxxxxxxxx",
				"xxxbbbbbxxx",
				"xxxbeeebxxx",
				"xxxbexebxxx",
				"xxxbeeebxxx",
				"xxxbbbbbxxx",
				"xxxxxxxxxxx",
				"xxxxxxxxxxx",
				"xxxxxxxxxxx"
				);
	}

	@Override
	public Boolean checkForFullMultiBlock(Level world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBounds(world, x, y, z, this, x-12, y-12, z-12, x+12, y+12, z+12);
		int n = this.checkForTurbines(world, dir, blocks); //only accept steam emitter at last turb stage
		if (n <= 0 || n > 7)
			return false;
		if (!this.checkForShape(world, dir, blocks, n, call))
			return false;
		return true;
	}

	private int checkForTurbines(Level world, Direction dir, StructuredBlockArray blocks) {
		int mx = blocks.getMinX()+blocks.getSizeX()/2;
		int my = blocks.getMinY()+blocks.getSizeY()/2;
		int mz = blocks.getMinZ()+blocks.getSizeZ()/2;
		int sx = dir.getStepX() == 0 ? mx : dir.getStepX() < 0 ? blocks.getMaxX() : blocks.getMinX();
		int sz = dir.getStepZ() == 0 ? mz : dir.getStepZ() < 0 ? blocks.getMaxZ() : blocks.getMinZ();
		int c = 0;
		for (int i = 0; i < setup.getLength(); i++) {
			int dx = sx+i*dir.getStepX();
			int dz = sz+i*dir.getStepZ();
			BlockPos p = new BlockPos(dx, my, dz);
			ReactorTiles r = ReactorTiles.getTE(world, p);
			if (r == ReactorTiles.BIGTURBINE) {
				c++;
				((TileEntityTurbineCore)world.getBlockEntity(p)).markForMulti();
			}
			else
				return c;
		}
		return c;
	}

	private boolean checkForShape(Level world, Direction dir, StructuredBlockArray blocks, int turbines, BlockMatchFailCallback call) {
		int start = setup.getLength()-turbines-1;
		int mx = blocks.getMinX()+blocks.getSizeX()/2;
		int my = blocks.getMinY()+blocks.getSizeY()/2;
		int mz = blocks.getMinZ()+blocks.getSizeZ()/2;
		int sx = dir.getStepX() == 0 ? mx : dir.getStepX() < 0 ? blocks.getMaxX() : blocks.getMinX();
		int sz = dir.getStepZ() == 0 ? mz : dir.getStepZ() < 0 ? blocks.getMaxZ() : blocks.getMinZ();
		for (int i = start; i < setup.getLength(); i++) {
			int d = i-start;
			int dx = sx+d*dir.getStepX();
			int dz = sz+d*dir.getStepZ();
			boolean match = setup.checkAgainst(world, dx, my, dz, 5, 5, dir, i, call);
			if (!match)
				return false;
		}
		return true;
	}

	@Override
	public void breakMultiBlock(Level world, int x, int y, int z) {
		for (BlockPos p : BlockPos.betweenClosed(x-12, y-12, z-12, x+12, y+12, z+12)) {
			BlockState s = world.getBlockState(p);
			if (s.is(this)) {
				if (s.getValue(FORMED))
					world.setBlock(p.immutable(), s.setValue(FORMED, false), 3);
			}
			else if (ReactorTiles.getTE(world, p) == ReactorTiles.BIGTURBINE) {
				((TileEntityTurbineCore)world.getBlockEntity(p)).setHasMultiBlock(false);
			}
		}
	}

	@Override
	protected void onCreateFullMultiBlock(Level world, int x, int y, int z, Boolean complete) {
		for (BlockPos p : BlockPos.betweenClosed(x-12, y-12, z-12, x+12, y+12, z+12)) {
			BlockState s = world.getBlockState(p);
			if (s.is(this)) {
				if (!s.getValue(FORMED))
					world.setBlock(p.immutable(), s.setValue(FORMED, true), 3);
			}
			else if (ReactorTiles.getTE(world, p) == ReactorTiles.BIGTURBINE) {
				((TileEntityTurbineCore)world.getBlockEntity(p)).setHasMultiBlock(true);
			}
		}
	}

	@Override
	public boolean canTriggerMultiBlockCheck(Level world, BlockPos pos, BlockState state) {
		TurbinePart p = state.getValue(PART);
		return p == TurbinePart.SHELL || p == TurbinePart.HOUSING;
	}

	@Override
	protected BlockEntity getTileEntityForPosition(Level world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBounds(world, x, y, z, this, x-12, y-12, z-12, x+12, y+12, z+12);
		int mx = blocks.getMinX()+blocks.getSizeX()/2;
		int my = blocks.getMinY()+blocks.getSizeY()/2;
		int mz = blocks.getMinZ()+blocks.getSizeZ()/2;
		BlockPos p = new BlockPos(mx, my, mz);
		return ReactorTiles.getTE(world, p) == ReactorTiles.BIGTURBINE ? world.getBlockEntity(p) : null;
	}

	public SlicedBlockBlueprint getBlueprint() {
		return setup.copy();
	}

}
