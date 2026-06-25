/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import java.util.Arrays;
import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.auxiliary.interfaces.PumpablePipe;
import reika.rotarycraft.auxiliary.interfaces.RenderableDuct;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;

public abstract class TileEntityReactorPiping extends TileEntityReactorBase implements RenderableDuct, PumpablePipe {

	protected Fluid fluid;
	protected int fluidLevel;

	private boolean[] connections = new boolean[6];

	public TileEntityReactorPiping(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public abstract boolean isValidFluid(Fluid f);

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		Fluid f = this.getFluidType();
		this.intakeFluid(world, pos);
		if (this.getFluidLevel() <= 0) {
			this.setLevel(0);
			this.setFluid(null);
		}
		else {
			this.dumpContents(world, pos);
		}
		Fluid f2 = this.getFluidType();
		if (f != f2 && !world.isClientSide()) {
			this.setChanged();
			BlockState s = this.getBlockState();
			world.setBlocksDirty(pos, s, s);
		}
	}

	public boolean isConnectedDirectly(Direction dir) {
		return connections[dir.ordinal()];
	}

	protected boolean isInteractableTile(BlockEntity te) {
		if (te == null)
			return false;
		if (te.getClass() == this.getClass())
			return true;
		if (te instanceof IFluidHandler) {
			String name = te.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
			return !name.contains("conduit") && !name.contains("pipe");
		}
		return false;
	}

	protected final boolean canInteractWith(Level world, BlockPos pos, Direction side) {
		if (!connections[side.ordinal()])
			return false;
		BlockPos dpos = pos.relative(side);
		Block id = world.getBlockState(dpos).getBlock();
		if (id == Blocks.AIR)
			return false;
		ReactorTiles m = ReactorTiles.getTE(world, dpos);
		if (m == this.getTile())
			return true;
		BlockEntity te = world.getBlockEntity(dpos);
		return (te instanceof PipeConnector || te instanceof IFluidHandler) && this.isInteractableTile(te);
	}

	@Override
	public final void animateWithTick(Level world, BlockPos pos) {

	}

	public final Fluid getFluidType() {
		return fluid;
	}

	@Override
	public final Fluid getAttributes() {
		return fluid;
	}

	@Override
	public final int getFluidLevel() {
		return fluidLevel;
	}

	/** Direction is relative to the piping block (so DOWN means the block is below the pipe) */
	@Override
	public final boolean isConnectionValidForSide(Direction dir) {
		return connections[dir.ordinal()];
	}

	@Override
	public final Block getPipeBlockType() {
		return this.getTile().getBlockState().getBlock();
	}

	@Override
	public boolean isConnectedToNonSelf(Direction dir) {
		if (!this.isConnectionValidForSide(dir))
			return false;
		BlockPos npos = getBlockPos().relative(dir);
		return level.getBlockState(npos).getBlock() != this.getTile().getBlockState().getBlock();
	}

	// 1.21.5: BlockEntity.getRenderBoundingBox was removed; renderers compute their own bounds.
	public final AABB getRenderBoundingBox() {
		return new AABB(getBlockPos());
	}

	public final void recomputeConnections(Level world, BlockPos pos) {
		for (int i = 0; i < 6; i++) {
			connections[i] = this.isConnected(dirs[i]);
		}
		this.syncAllData(true);
	}

	public final void deleteFromAdjacentConnections(Level world, BlockPos pos) {
		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			BlockPos dpos = pos.relative(dir);
			ReactorTiles m = ReactorTiles.getTE(world, dpos);
			if (m == this.getTile()) {
				TileEntityReactorPiping te = (TileEntityReactorPiping)world.getBlockEntity(dpos);
				te.connections[dir.getOpposite().ordinal()] = false;
				te.syncAllData(false);
			}
		}
	}

	public final void addToAdjacentConnections(Level world, BlockPos pos) {
		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			BlockPos dpos = pos.relative(dir);
			ReactorTiles m = ReactorTiles.getTE(world, dpos);
			if (m == this.getTile()) {
				TileEntityReactorPiping te = (TileEntityReactorPiping)world.getBlockEntity(dpos);
				te.connections[dir.getOpposite().ordinal()] = true;
				te.syncAllData(false);
			}
		}
	}

	private boolean isConnected(Direction dir) {
		BlockPos dpos = getBlockPos().relative(dir);
		ReactorTiles m = this.getTile();
		ReactorTiles m2 = ReactorTiles.getTE(level, dpos);
		if (m == m2) {
			return true;
		}
		BlockEntity tile = level.getBlockEntity(dpos);
		return tile instanceof IFluidHandler && this.isInteractableTile(tile);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		for (int i = 0; i < 6; i++) {
			NBT.putBoolean("conn"+i, connections[i]);
		}

		Fluid f = this.getFluidType();
		if (f != null) {
			Identifier id = BuiltInRegistries.FLUID.getKey(f);
			if (id != null)
				NBT.putString("fluid_id", id.toString());
		}
		NBT.putInt("level", this.getFluidLevel());
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		boolean[] old = new boolean[connections.length];
		System.arraycopy(connections, 0, old, 0, old.length);
		for (int i = 0; i < 6; i++) {
			connections[i] = NBT.getBooleanOr("conn"+i, false);
		}
		boolean update = !Arrays.equals(old, connections);

		String fluidIdStr = NBT.getStringOr("fluid_id", "");
		Fluid f;
		if (fluidIdStr.isEmpty()) {
			f = null;
		}
		else {
			Identifier id = Identifier.tryParse(fluidIdStr);
			f = id == null ? null : BuiltInRegistries.FLUID.getValue(id);
			if (f == Fluids.EMPTY)
				f = null;
		}
		update = update || f != this.getFluidType();
		this.setFluid(f);
		this.setLevel(NBT.getIntOr("level", 0));

		if (level != null && update) {
			BlockState s = this.getBlockState();
			level.setBlocksDirty(getBlockPos(), s, s);
		}
	}

	public final int removeLiquid(int max) {
		int has = this.getFluidLevel();
		int rem = Math.min(max, has);
		this.setLevel(has-rem);
		return rem;
	}

	public final void addFluid(int toadd) {
		this.setLevel(this.getFluidLevel()+toadd);
	}

	public final boolean addFluid(Fluid f, int toadd) {
		Fluid has = this.getFluidType();
		if (has != null && has != f)
			return false;
		this.setFluid(f);
		this.addFluid(toadd);
		return true;
	}

	private void intakeFluid(Level world, BlockPos pos) {
		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			if (this.canInteractWith(world, pos, dir)) {
				BlockEntity te = world.getBlockEntity(pos.relative(dir));

				if (!this.isInteractableTile(te))
					continue;

				if (te instanceof TileEntityReactorPiping tp) {
					Fluid f = tp.getFluidType();
					if (f != null) {
						int amt = tp.getFluidLevel();
						int dL = amt-this.getFluidLevel();
						int todrain = this.getPipeIntake(dL);
						if (todrain > 0 && this.canIntakeFluid(f)) {
							this.setFluid(f);
							this.addFluid(todrain);
							tp.removeLiquid(todrain);
							this.onIntake(te);
						}
					}
				}
				else if (te instanceof PipeConnector pc) {
					BlockEntityPiping.Flow flow = pc.getFlowForSide(dir.getOpposite());
					if (flow.canOutput) {
						FluidStack fs = pc.drainPipe(dir.getOpposite(), Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
						if (fs != null && !fs.isEmpty()) {
							int level = this.getFluidLevel();
							int todrain = this.getPipeIntake(fs.getAmount()-level);
							if (todrain > 0 && this.canIntakeFluid(fs.getFluid())) {
								this.addFluid(todrain);
								this.setFluid(fs.getFluid());
								pc.drainPipe(dir.getOpposite(), todrain, IFluidHandler.FluidAction.EXECUTE);
								this.onIntake(te);
							}
						}
					}
				}
				else if (te instanceof IFluidHandler fl) {
					FluidStack fs = fl.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
					if (fs != null && !fs.isEmpty()) {
						int level = this.getFluidLevel();
						int todrain = this.getPipeIntake(fs.getAmount()-level);
						if (todrain > 0 && this.canIntakeFluid(fs.getFluid())) {
							fl.drain(todrain, IFluidHandler.FluidAction.EXECUTE);
							this.addFluid(todrain);
							this.setFluid(fs.getFluid());
							this.onIntake(te);
						}
					}
				}
			}
		}
	}

	private void dumpContents(Level world, BlockPos pos) {
		Fluid f = this.getFluidType();
		if (this.getFluidLevel() <= 0 || f == null)
			return;
		for (int i = 0; i < 6; i++) {
			int level = this.getFluidLevel();
			if (level <= 0) {
				this.setFluid(null);
				return;
			}
			Direction dir = dirs[i];
			if (this.canInteractWith(world, pos, dir)) {
				BlockEntity te = world.getBlockEntity(pos.relative(dir));

				if (!this.isInteractableTile(te))
					continue;

				if (te instanceof TileEntityReactorPiping tp) {
					if (tp.canIntakeFluid(f)) {
						int otherlevel = tp.getFluidLevel();
						int dL = level-otherlevel;
						int toadd = this.getPipeOutput(dL);
						if (toadd > 0) {
							tp.addFluid(toadd);
							this.removeLiquid(toadd);
						}
					}
				}
				else if (te instanceof PipeConnector pc) {
					BlockEntityPiping.Flow flow = pc.getFlowForSide(dir.getOpposite());
					if (flow.canIntake) {
						int toadd = this.getPipeOutput(this.getFluidLevel());
						if (toadd > 0) {
							FluidStack fs = new FluidStack(f, toadd);
							int added = pc.fillPipe(dir.getOpposite(), fs, IFluidHandler.FluidAction.EXECUTE);
							if (added > 0) {
								this.removeLiquid(added);
							}
						}
					}
				}
				else if (te instanceof IFluidHandler fl && dir.getStepY() == 0) {
					int toadd = this.getPipeOutput(this.getFluidLevel());
					if (toadd > 0) {
						int added = fl.fill(new FluidStack(f, toadd), IFluidHandler.FluidAction.EXECUTE);
						if (added > 0) {
							this.removeLiquid(added);
						}
					}
				}
			}
		}
	}

	private boolean canIntakeFluid(Fluid f) {
		return this.isValidFluid(f) && (fluid == null || f.equals(fluid));
	}

	public final int getPipeIntake(int otherlevel) {
		return BlockEntityPiping.TransferAmount.FORCEDQUARTER.getTransferred(otherlevel);
	}

	public final int getPipeOutput(int max) {
		return Math.min(BlockEntityPiping.TransferAmount.FORCEDQUARTER.getTransferred(max), this.getFluidLevel()-5);
	}

	private void setFluid(Fluid f) {
		fluid = f;
	}

	private void setLevel(int amt) {
		fluidLevel = amt;
	}

	protected abstract void onIntake(BlockEntity te);

	@Override
	public final boolean isFluidPipe() {
		return true;
	}

	@Override
	public final boolean canTransferTo(PumpablePipe p, Direction dir) {
		return p instanceof TileEntityReactorPiping && this.getTile() == ((TileEntityReactorPiping)p).getTile();
	}

	@Override
	public final void transferFrom(PumpablePipe from, int amt) {
		TileEntityReactorPiping te = (TileEntityReactorPiping)from;
		this.setLevel(this.getFluidLevel()+amt);
		this.setFluid(te.getFluidType());
		te.setLevel(te.getFluidLevel()-amt);
		if (te.getFluidLevel() == 0)
			te.setFluid(null);
	}

}
