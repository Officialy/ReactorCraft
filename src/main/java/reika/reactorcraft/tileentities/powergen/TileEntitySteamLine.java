/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.powergen;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import java.util.ArrayList;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

// CHROMA-PORT: import reika.chromaticraft.api.interfaces.WorldRift;
import reika.dragonapi.instantiable.data.Proportionality;
import reika.dragonapi.libraries.ReikaNBTHelper;
import reika.dragonapi.libraries.reikanbthelper.NBTIO;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.SteamTile;
import reika.reactorcraft.base.TileEntityLine;
import reika.reactorcraft.registry.ReactorOptions;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.registry.WorkingFluid;
import reika.reactorcraft.tileentities.TileEntitySteamDiffuser;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.rotarycraft.api.interfaces.PressureTile;
import reika.rotarycraft.auxiliary.interfaces.PumpablePipe;
import reika.rotarycraft.registry.MachineRegistry;
import reika.rotarycraft.tileentities.auxiliary.TileEntityPipePump;

public class TileEntitySteamLine extends TileEntityLine implements PumpablePipe, SteamTile, PressureTile {
	public TileEntitySteamLine(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.STEAMLINE.get(), pos, state);
	}


	private int steam;

	private WorkingFluid fluid = WorkingFluid.EMPTY;
	private Proportionality<ReactorType> source = new Proportionality();

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.STEAMLINE;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		this.drawFromBoiler(world, x, y, z);
		this.getPipeSteam(world, x, y, z);

		if (steam <= 0) {
			fluid = WorkingFluid.EMPTY;
			source.clear();
		}
		else if (this.getPressure() > this.getMaxPressure()) {
			this.delete();
			world.explode(/*PORT*/null, x+0.5, y+0.5, z+0.5, 2, true);
		}
	}

	@Override
	protected boolean canConnectToMachine(Block id, int meta, Direction dir, BlockEntity te) {
		if (id == ReactorTiles.BOILER.getBlock() && dir == Direction.DOWN)
			return true;
		if (id == ReactorTiles.GRATE.getBlock())
			return true;
		if (id == ReactorTiles.BIGTURBINE.getBlock())
			return true;
		if (id == ReactorTiles.DIFFUSER.getBlock()) {
			return ((TileEntitySteamDiffuser)this.getAdjacentTileEntity(dir)).getFacing().getOpposite() == dir;
		}
		if (id == MachineRegistry.PIPEPUMP.getBlock()) {
			return ((TileEntityPipePump)this.getAdjacentTileEntity(dir)).canConnectToPipeOnSide(dir);
		}
		return false;
	}

	private void drawFromBoiler(Level world, int x, int y, int z) {
		ReactorTiles r = ReactorTiles.getTE(world, x, y-1, z);
		if (r == ReactorTiles.BOILER) {
			TileEntityReactorBoiler te = (TileEntityReactorBoiler)world.getBlockEntity(x, y-1, z);
			if (te.getTileEntityAge() > 5 && this.canTakeInWorkingFluid(te.getWorkingFluid())) {
				fluid = te.getWorkingFluid();
				int s = te.removeSteam();
				steam += s;
				for (ReactorType rt : te.getReactorTypeSet()) {
					double f = te.getReactorTypeFraction(rt);
					if (rt == null || rt == ReactorType.NONE)
						rt = te.getReactorType();
					if (rt == null || rt == ReactorType.NONE)
						rt = te.getDefaultReactorType();
					source.addValue(rt, s*f);
				}
			}
		}
	}

	private boolean canTakeInWorkingFluid(WorkingFluid f) {
		if (f == WorkingFluid.EMPTY)
			return false;
		if (fluid == WorkingFluid.EMPTY)
			return true;
		if (fluid == f)
			return true;
		return false;
	}

	private void getPipeSteam(Level world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			BlockEntity te = this.getAdjacentTileEntity(dirs[i]);
			if (te instanceof TileEntitySteamLine) {
				TileEntitySteamLine tile = (TileEntitySteamLine)te;
				if (this.canTakeInWorkingFluid(tile.fluid))
					this.readPipe(tile);
			}
			else if (te instanceof WorldRift && !world.isClientSide()) {
				WorldRift wr = (WorldRift)te;
				BlockEntity tile = wr.getTileEntityFrom(dirs[i]);
				if (tile instanceof TileEntitySteamLine) {
					TileEntitySteamLine ts = (TileEntitySteamLine)tile;
					if (this.canTakeInWorkingFluid(ts.fluid))
						this.readPipe(ts);
				}
			}
		}
	}

	private void readPipe(TileEntitySteamLine te) {
		int dS = te.steam-steam;
		if (dS > 0) {
			//ReikaJavaLibrary.pConsole(steam+":"+te.steam);
			int amt = dS/2+1;
			float frac = amt/(float)te.steam;
			steam += amt;
			te.steam -= amt;
			fluid = te.fluid;
			this.addSources(te, frac);
		}
	}

	@Override
	public int getSteam() {
		return steam;
	}

	public void removeSteam(int amt) {
		steam -= amt;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		steam = NBT.getIntOr("energy", 0);

		fluid = WorkingFluid.getFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("energy", steam);

		fluid.saveToNBT(NBT);
	}

	@Override
	public void loadAdditional(/*PORT*/CompoundTag NBT) {
		super.loadAdditional(/*PORT*/NBT);

		source.readFromTag(NBT.getCompoundTag("sources"), (NBTIO<ReactorType>)ReikaNBTHelper.getEnumConverter(ReactorType.class));
		if (source.removeValue(null) > 0) {
			ReactorCraft.LOGGER.logError(this+" loaded null-containing steam type map from NBT: "+NBT);
		}
	}

	@Override
	public void saveAdditional(/*PORT*/CompoundTag NBT) {
		super.saveAdditional(/*PORT*/NBT);

		CompoundTag tag = new CompoundTag();
		source.writeToTag(tag, (NBTIO<ReactorType>)ReikaNBTHelper.getEnumConverter(ReactorType.class));
		NBT.setTag("sources", tag);
	}

	public WorkingFluid getWorkingFluid() {
		return fluid;
	}

	@Override
	public boolean canTransferTo(PumpablePipe p, Direction dir) {
		if (p instanceof TileEntitySteamLine) {
			WorkingFluid f = ((TileEntitySteamLine)p).fluid;
			return f != WorkingFluid.EMPTY ? f == fluid : true;
		}
		return false;
	}

	@Override
	public int getFluidLevel() {
		return this.getSteam();
	}

	@Override
	public void transferFrom(PumpablePipe from, int amt) {
		float frac = (float)amt/((TileEntitySteamLine)from).steam;
		((TileEntitySteamLine)from).steam -= amt;
		fluid = ((TileEntitySteamLine)from).fluid;
		steam += amt;
		this.addSources((TileEntitySteamLine)from, frac);
	}

	private void addSources(TileEntitySteamLine from, float frac) {
		for (ReactorType r : new ArrayList<ReactorType>(from.source.getElements())) {
			if (r == null)
				continue;
			double val = from.source.getValue(r)*frac;
			if (Double.isNaN(val) || Double.isInfinite(val))
				continue;
			source.addValue(r, val);
			from.source.addValue(r, -val);
		}
	}

	public Proportionality<ReactorType> getSourceReactorType() {
		return source.copy();
	}

	@Override
	public IIcon getTexture() {
		return Blocks.wool.getIcon(0, this.isInWorld() ? 15 : 7);
	}

	@Override
	public int getPressure() {
		return steam;
	}

	@Override
	public int getMaxPressure() {
		return ReactorOptions.STEAMLINECAP.getValue();
	}
}
