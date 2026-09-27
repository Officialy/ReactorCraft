/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fusion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.storage.HybridTankResourceHandler;
import reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler;
import reika.dragonapi.interfaces.blockentity.ToggleTile;
import reika.reactorcraft.auxiliary.FusionReactorToroidPart;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityPlasma;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityMagneticPipe;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityFusionInjector extends TileEntityReactorBase implements PipeConnector, MultiBlockTile, FusionReactorToroidPart,
ToggleTile, NeutronTile, HasFluidResourceHandler {

	public TileEntityFusionInjector(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.INJECTOR.get(), pos, state);
	}

	private final HybridTank tank = new HybridTank("injector", 8000);
	private final ResourceHandler<FluidResource> fluidHandler = new HybridTankResourceHandler(
			new HybridTank[] {tank},
			(index, resource) -> resource.getFluid() == ReactorFluids.getLegacyFluid("rc fusion plasma"),
			(index, resource) -> false, this::setChanged);

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return side == null || this.getAdjacentBlockEntity(side) instanceof TileEntityMagneticPipe
				? fluidHandler : null;
	}

	private Direction facing;

	private boolean hasMultiBlock;

	private boolean enabled = true;

	public boolean hasMultiBlock() {
		return hasMultiBlock;
	}

	@Override
	protected void onFirstTick(Level world, BlockPos pos) {
		if (!world.isClientSide())
			this.setHasMultiBlock(reika.reactorcraft.blocks.multi.BlockInjectorMulti.isComplete(world, pos));
	}

	public void setHasMultiBlock(boolean has) {
		hasMultiBlock = has;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (DragonAPI.debugtest) {
			tank.addLiquid(1000, ReactorFluids.getLegacyFluid("rc fusion plasma"));
			hasMultiBlock = true;
		}

		if (this.canMake())
			this.make(world, pos);
	}

	public void setFacing(Direction dir) {
		facing = dir;
	}

	private boolean canMake() {
		if (!hasMultiBlock)
			return false;
		if (tank.isEmpty())
			return false;
		if (!enabled)
			return false;
		if (this.hasRedstoneSignal())
			return false;
		return true;
	}

	private void make(Level world, BlockPos pos) {
		this.createPlasma(world, pos);
		tank.removeLiquid(2);
	}

	private void createPlasma(Level world, BlockPos pos) {
		EntityPlasma e = new EntityPlasma(world, pos.getX(), pos.getY(), pos.getZ(), this.getPlacerName());
		e.setTarget(pos.getX()+this.getFacing().getStepX(), pos.getZ()+this.getFacing().getStepZ());
		if (!world.isClientSide())
			world.addFreshEntity(e);
	}

	public int[] getTarget() {
		BlockPos pos = this.getBlockPos();
		int dx = pos.getX()+this.getFacing().getStepX();
		int dz = pos.getZ()+this.getFacing().getStepZ();
		return new int[]{dx, pos.getY(), dz};
	}

	public FusionReactorToroidPart getNextPart(Level world, int x, int y, int z) {
		int dx = this.getBlockPos().getX()+this.getFacing().getStepX()*2;
		int dz = this.getBlockPos().getZ()+this.getFacing().getStepZ()*2;
		BlockEntity te = world.getBlockEntity(new BlockPos(dx, y, dz));
		return te instanceof FusionReactorToroidPart ? (FusionReactorToroidPart)te : null;
	}

	public Direction getFacing() {
		return facing != null ? facing : Direction.EAST;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return true;
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return Flow.INPUT;
	}

	private boolean isPlasma(FluidStack fs) {
		return !fs.isEmpty() && fs.getFluid().equals(ReactorFluids.getLegacyFluid("rc fusion plasma"));
	}










	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.INJECTOR;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		tank.writeToNBT(NBT);

		NBT.putInt("face", this.getFacing().ordinal());

		NBT.putBoolean("multi", hasMultiBlock);

		NBT.putBoolean("t_enable", enabled);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		tank.readFromNBT(NBT);

		facing = Direction.values()[NBT.getIntOr("face", Direction.EAST.ordinal())];

		hasMultiBlock = NBT.getBooleanOr("multi", false);

		enabled = NBT.getBooleanOr("t_enable", true);
	}

	@Override
	public int getTextureState(Direction side) {
		return side == this.getFacing() ? 0 : 2;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enable) {
		enabled = enable;
		this.syncAllData(false);
	}

	@Override
	public void breakBlock() {
		if (!level.isClientSide()) {
			BlockPos pos = this.getBlockPos();
			for (int i = 0; i < 6; i++) {
				Direction dir = dirs[i];
				BlockPos p = pos.relative(dir);
				Block b = level.getBlockState(p).getBlock();
				if (b instanceof BlockReCMultiBlock) {
					((BlockReCMultiBlock)b).breakMultiBlock(level, p.getX(), p.getY(), p.getZ());
				}
			}
		}
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

}
