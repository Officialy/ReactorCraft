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
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.storage.HybridTankResourceHandler;
import reika.reactorcraft.base.TankedReactorPowerReceiver;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityReactorPump extends TankedReactorPowerReceiver {

	public TileEntityReactorPump(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.PUMP.get(), pos, state);
	}

	public static final long MINPOWER = 16384;
	public static final int MINTORQUE = 1024;

	private final HybridTank output = new HybridTank("pumpout", this.getCapacity());
	private final ResourceHandler<FluidResource> outputHandler = new HybridTankResourceHandler(
			new HybridTank[] {output}, (index, resource) -> false,
			(index, resource) -> true, this::setChanged);
	private final ResourceHandler<FluidResource> allFluids = new CombinedResourceHandler<>(
			super.getFluidHandler(null), outputHandler);

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return side == null ? allFluids : side == Direction.UP ? super.getFluidHandler(side)
				: side.getAxis().isHorizontal() ? outputHandler : null;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.PUMP;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		if (this.canConvert())
			this.convertFluids();
		if (!output.isEmpty())
			this.dumpFluids(world, pos);
	}

	private boolean canConvert() {
		if (!this.sufficientPower())
			return false;
		if (tank.isEmpty())
			return false;
		if (output.isEmpty())
			return true;
		if (output.isFull())
			return false;
		if (tank.getActualFluid().getFluid().equals(ReactorFluids.getLegacyFluid("rc lowpwater")))
			return output.getActualFluid().getFluid().equals(Fluids.WATER);
		if (tank.getActualFluid().getFluid().equals(ReactorFluids.getLegacyFluid("rc lowpammonia")))
			return output.getActualFluid().getFluid().equals(ReactorFluids.getLegacyFluid("rc ammonia"));
		return false;
	}

	private void dumpFluids(Level world, BlockPos pos) {
		for (int i = 2; i < 6; i++) {
			Direction dir = dirs[i];
			ResourceHandler<FluidResource> target = world.getCapability(Capabilities.Fluid.BLOCK,
					pos.relative(dir), dir.getOpposite());
			if (target != null) {
				FluidResource resource = FluidResource.of(output.getFluid());
				ResourceHandlerUtil.move(outputHandler, target, candidate -> candidate.equals(resource),
						output.getFluidLevel(), null);
			}
		}
	}

	private void convertFluids() {
		int amt = Math.min(tank.getFluidLevel(), output.getRemainingSpace());
		if (amt <= 0)
			return;
		if (tank.getActualFluid().getFluid().equals(ReactorFluids.getLegacyFluid("rc lowpwater"))) {
			output.addLiquid(amt, Fluids.WATER);
		}
		else if (tank.getActualFluid().getFluid().equals(ReactorFluids.getLegacyFluid("rc lowpammonia"))) {
			output.addLiquid(amt, ReactorFluids.getLegacyFluid("rc ammonia"));
		}
		tank.removeLiquid(amt);
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		super.animateWithTick(world, pos);
		if (this.getPower() > 0) {
			phi += 15F;
		}
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);
		output.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);
		output.writeToNBT(NBT);
	}


	@Override
	public BlockEntityPiping.Flow getFlowForSide(Direction side) {
		if (this.canReceiveFrom(side))
			return BlockEntityPiping.Flow.INPUT;
		if (side.getAxis().isHorizontal())
			return BlockEntityPiping.Flow.OUTPUT;
		return BlockEntityPiping.Flow.NONE;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return side != Direction.DOWN && this.canConnectToPipe(p);
	}

	@Override
	public int getCapacity() {
		return 12000;
	}

	@Override
	public boolean canReceiveFrom(Direction from) {
		return from == Direction.UP;
	}

	@Override
	public boolean canReadFrom(Direction dir) {
		return dir == Direction.DOWN;
	}

	@Override
	public Fluid getInputFluid() {
		return null;
	}

	@Override
	public boolean isValidFluid(Fluid f) {
		if (f.equals(ReactorFluids.getLegacyFluid("rc lowpwater")))
			return true;
        return f.equals(ReactorFluids.getLegacyFluid("rc lowpammonia"));
    }

	@Override
	public int getMinTorque(int available) {
		return MINTORQUE;
	}

	@Override
	public int getMinTorque() {
		return MINTORQUE;
	}

	@Override
	public int getMinSpeed() {
		return 1;
	}

	@Override
	public long getMinPower() {
		return MINPOWER;
	}
}
