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

import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.instantiable.data.Proportionality;
import reika.reactorcraft.auxiliary.SteamTile;
import reika.reactorcraft.base.TileEntityLine;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorOptions;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.registry.WorkingFluid;
import reika.reactorcraft.tileentities.TileEntitySteamDiffuser;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.rotarycraft.api.interfaces.PressureTile;
import reika.rotarycraft.auxiliary.interfaces.PumpablePipe;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntitySteamLine extends TileEntityLine implements PumpablePipe, SteamTile, PressureTile {

	public TileEntitySteamLine(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.STEAMLINE.get(), pos, state);
	}

	private int steam;

	private WorkingFluid fluid = WorkingFluid.EMPTY;
	// Per-reactor-type contribution breakdown (display only; rebuilds from boiler draws, not persisted).
	private final Proportionality<ReactorType> source = new Proportionality<>();

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.STEAMLINE;
	}

	@Override
	public boolean hasATank() {
		return false;
	}

	@Override
	public boolean hasAnInventory() {
		return false;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		this.drawFromBoiler(world, pos);
		this.getPipeSteam(world, pos);

		if (steam <= 0) {
			fluid = WorkingFluid.EMPTY;
			source.clear();
		}
		else if (this.getPressure() > this.getMaxPressure()) {
			this.delete();
			world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, Level.ExplosionInteraction.BLOCK);
		}
	}

	@Override
	protected boolean canConnectToMachine(Block id, Direction dir, BlockEntity te) {
		if (id == ReactorTiles.BOILER.getBlock() && dir == Direction.DOWN)
			return true;
		if (id == ReactorTiles.GRATE.getBlock())
			return true;
		if (id == ReactorTiles.BIGTURBINE.getBlock())
			return true;
		if (id == ReactorTiles.DIFFUSER.getBlock()) {
			return ((TileEntitySteamDiffuser) this.getAdjacentBlockEntity(dir)).getFacing().getOpposite() == dir;
		}
		// MOD-PORT: RotaryCraft PipePump connection gated out (BlockEntityPipePump/MachineRegistry.PIPEPUMP not ported).
		return false;
	}

	private void drawFromBoiler(Level world, BlockPos pos) {
		if (ReactorTiles.getTE(world, pos.below()) == ReactorTiles.BOILER) {
			TileEntityReactorBoiler te = (TileEntityReactorBoiler) world.getBlockEntity(pos.below());
			if (te.getTicksExisted() > 5 && this.canTakeInWorkingFluid(te.getWorkingFluid())) {
				fluid = te.getWorkingFluid();
				int s = te.removeSteam();
				steam += s;
				for (ReactorType rt : te.getReactorTypeSet()) {
					double f = te.getReactorTypeFraction(rt);
					if (rt == null || rt == ReactorType.NONE)
						rt = te.getReactorType();
					if (rt == null || rt == ReactorType.NONE)
						rt = te.getDefaultReactorType();
					source.addValue(rt, s * f);
				}
			}
		}
	}

	private boolean canTakeInWorkingFluid(WorkingFluid f) {
		if (f == WorkingFluid.EMPTY)
			return false;
		if (fluid == WorkingFluid.EMPTY)
			return true;
		return fluid == f;
	}

	private void getPipeSteam(Level world, BlockPos pos) {
		for (Direction dir : dirs) {
			BlockEntity te = this.getAdjacentBlockEntity(dir);
			if (te instanceof TileEntitySteamLine tile) {
				if (this.canTakeInWorkingFluid(tile.fluid))
					this.readPipe(tile);
			}
			// CHROMA-PORT: ChromatiCraft WorldRift cross-dimension steam transfer gated out (mod not in build).
		}
	}

	private void readPipe(TileEntitySteamLine te) {
		int dS = te.steam - steam;
		if (dS > 0) {
			int amt = dS / 2 + 1;
			float frac = amt / (float) te.steam;
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

	public WorkingFluid getWorkingFluid() {
		return fluid;
	}

	@Override
	public boolean canTransferTo(PumpablePipe p, Direction dir) {
		if (p instanceof TileEntitySteamLine) {
			WorkingFluid f = ((TileEntitySteamLine) p).fluid;
			return f == WorkingFluid.EMPTY || f == fluid;
		}
		return false;
	}

	@Override
	public int getFluidLevel() {
		return this.getSteam();
	}

	@Override
	public void transferFrom(PumpablePipe from, int amt) {
		float frac = (float) amt / ((TileEntitySteamLine) from).steam;
		((TileEntitySteamLine) from).steam -= amt;
		fluid = ((TileEntitySteamLine) from).fluid;
		steam += amt;
		this.addSources((TileEntitySteamLine) from, frac);
	}

	private void addSources(TileEntitySteamLine from, float frac) {
		for (ReactorType r : new ArrayList<ReactorType>(from.source.getElements())) {
			if (r == null)
				continue;
			double val = from.source.getValue(r) * frac;
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
	public Identifier getTexture() {
		return Identifier.fromNamespaceAndPath("reactorcraft", "block/steam_line");
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
