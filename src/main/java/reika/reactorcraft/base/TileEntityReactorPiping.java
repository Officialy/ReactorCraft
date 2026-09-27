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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import reika.dragonapi.instantiable.storage.FilteredFluidResourceHandler;
import reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler;

import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.auxiliary.interfaces.PumpablePipe;
import reika.rotarycraft.auxiliary.interfaces.RenderableDuct;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;

public abstract class TileEntityReactorPiping extends TileEntityReactorBase implements RenderableDuct, PumpablePipe, HasFluidResourceHandler {

	protected Fluid fluid;
	protected int fluidLevel;
	private final ResourceHandler<FluidResource> fluidHandler = new ReactorPipeFluidHandler();
	private final ResourceHandler<FluidResource> inputFluidView = new FilteredFluidResourceHandler(
			fluidHandler, index -> true, (index, resource) -> true, (index, resource) -> false);

	private boolean[] connections = new boolean[6];

	public TileEntityReactorPiping(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return side == null || side.getStepY() == 0 ? fluidHandler : inputFluidView;
	}

	private final class ReactorPipeFluidHandler extends SnapshotJournal<ReactorPipeState>
			implements ResourceHandler<FluidResource> {
		@Override public int size() { return 1; }
		@Override public FluidResource getResource(int index) {
			checkIndex(index);
			return fluid == null || fluidLevel <= 0 ? FluidResource.EMPTY : FluidResource.of(fluid);
		}
		@Override public long getAmountAsLong(int index) {
			checkIndex(index);
			return fluidLevel;
		}
		@Override public long getCapacityAsLong(int index, FluidResource resource) {
			checkIndex(index);
			return resource.isEmpty() || isValid(index, resource) ? Integer.MAX_VALUE : 0;
		}
		@Override public boolean isValid(int index, FluidResource resource) {
			checkIndex(index);
			return !resource.isEmpty() && resource.equals(FluidResource.of(resource.getFluid()))
					&& isValidFluid(resource.getFluid());
		}
		@Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			checkIndex(index);
			TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
			if (amount == 0 || !isValid(index, resource) || !canIntakeFluid(resource.getFluid())) return 0;
			int inserted = Math.min(amount, Integer.MAX_VALUE - fluidLevel);
			if (inserted <= 0) return 0;
			updateSnapshots(transaction);
			setFluid(resource.getFluid());
			addFluid(inserted);
			return inserted;
		}
		@Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
			checkIndex(index);
			TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
			if (amount == 0 || !resource.equals(getResource(index))) return 0;
			int extracted = Math.min(amount, fluidLevel);
			if (extracted <= 0) return 0;
			updateSnapshots(transaction);
			removeLiquid(extracted);
			if (fluidLevel == 0) setFluid(null);
			return extracted;
		}
		@Override protected ReactorPipeState createSnapshot() {
			return new ReactorPipeState(fluid, fluidLevel);
		}
		@Override protected void revertToSnapshot(ReactorPipeState snapshot) {
			setFluid(snapshot.fluid());
			setLevel(snapshot.amount());
		}
		@Override protected void onRootCommit(ReactorPipeState originalState) {
			setChanged();
		}
		private void checkIndex(int index) {
			java.util.Objects.checkIndex(index, 1);
		}
	}

	private record ReactorPipeState(Fluid fluid, int amount) {}

	public abstract boolean isValidFluid(Fluid f);

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		// Build/refresh the connection cache that canInteractWith (and thus intake/dump) gates on.
		// recomputeConnections previously had NO caller, so connections[] stayed all-false and no pipe
		// ever transferred fluid — the "pressure system not working" report. Sync-on-change keeps this cheap.
		this.recomputeConnections(world, pos);
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
			return true; // Capability-only blocks may not have a block entity.
		if (te.getClass() == this.getClass())
			return true;
		if (te instanceof PipeConnector)
			return true;
		String name = te.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
		return !name.contains("conduit") && !name.contains("pipe");
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
		return this.isInteractableTile(te) && (te instanceof PipeConnector
				|| world.getCapability(Capabilities.Fluid.BLOCK, dpos, side.getOpposite()) != null);
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
		// Only sync when a connection actually changed — this runs every tick (from updateEntity), and
		// the old unconditional syncAllData(true) would have been a per-tick packet storm per pipe.
		boolean changed = false;
		for (int i = 0; i < 6; i++) {
			boolean c = this.isConnected(dirs[i]);
			if (c != connections[i]) {
				connections[i] = c;
				changed = true;
			}
		}
		if (changed)
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
		return this.isInteractableTile(tile) && (tile instanceof PipeConnector
				|| level.getCapability(Capabilities.Fluid.BLOCK, dpos, dir.getOpposite()) != null);
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
						ResourceHandler<FluidResource> source = world.getCapability(Capabilities.Fluid.BLOCK,
								pos.relative(dir), dir.getOpposite());
						if (source == null) continue;
						for (int slot = 0; slot < source.size(); slot++) {
							FluidResource resource = source.getResource(slot);
							if (resource.isEmpty() || !this.canIntakeFluid(resource.getFluid())) continue;
							int todrain = this.getPipeIntake(source.getAmountAsInt(slot) - this.getFluidLevel());
							if (todrain <= 0) continue;
							int moved = ResourceHandlerUtil.move(source, fluidHandler,
									candidate -> candidate.equals(resource), todrain, null);
							if (moved > 0) {
								this.onIntake(te);
								break;
							}
						}
					}
				}
				else {
					ResourceHandler<FluidResource> source = world.getCapability(Capabilities.Fluid.BLOCK,
							pos.relative(dir), dir.getOpposite());
					if (source == null) continue;
					for (int slot = 0; slot < source.size(); slot++) {
						FluidResource resource = source.getResource(slot);
						if (resource.isEmpty() || !this.canIntakeFluid(resource.getFluid())) continue;
						int todrain = this.getPipeIntake(source.getAmountAsInt(slot) - this.getFluidLevel());
						if (todrain <= 0) continue;
						int moved = ResourceHandlerUtil.move(source, fluidHandler,
								candidate -> candidate.equals(resource), todrain, null);
						if (moved > 0) {
							this.onIntake(te);
							break;
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
							ResourceHandler<FluidResource> target = world.getCapability(Capabilities.Fluid.BLOCK,
									pos.relative(dir), dir.getOpposite());
							if (target != null) ResourceHandlerUtil.move(fluidHandler, target,
									resource -> resource.getFluid() == f, toadd, null);
						}
					}
				}
				else if (dir.getStepY() == 0) {
					ResourceHandler<FluidResource> target = world.getCapability(Capabilities.Fluid.BLOCK,
							pos.relative(dir), dir.getOpposite());
					if (target == null) continue;
					int toadd = this.getPipeOutput(this.getFluidLevel());
					if (toadd > 0) {
						ResourceHandlerUtil.move(fluidHandler, target,
								resource -> resource.getFluid() == f, toadd, null);
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
