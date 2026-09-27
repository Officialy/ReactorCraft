/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fission.thorium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.instantiable.storage.FilteredFluidResourceHandler;
import reika.reactorcraft.base.TileEntityTankedReactorMachine;
import reika.reactorcraft.blocks.BlockThoriumFuel;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.registry.MachineRegistry;


public class TileEntityFuelDump extends TileEntityTankedReactorMachine {
	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		if (side == Direction.UP || !this.hasTile()) return null;
		ResourceHandler<FluidResource> controller = this.getCore().getFluidHandler(null);
		boolean waste = side != null && this.getAdjacentBlockEntity(side) instanceof
				reika.reactorcraft.tileentities.waste.TileEntityWastePipe;
		return new FilteredFluidResourceHandler(controller,
				index -> side == null ? index == 1 || index == 2 : index == (waste ? 2 : 1),
				(index, resource) -> false, (index, resource) -> true);
	}

	private int fullTicks = 0;

	public TileEntityFuelDump(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.FUELDUMP.get(), pos, state);
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (!world.isClientSide()) {
			BlockEntity te = this.getAdjacentBlockEntity(Direction.UP);
			if (te instanceof TileEntityThoriumCore tc) {
				if (tc.getTemperature() >= TileEntityThoriumCore.FUEL_DUMP_TEMPERATURE && tc.hasFuel()) {
					int rem = tank.getRemainingSpace();
					if (rem > 0) {
						int fuel = tc.dumpFuel(this, rem);
						if (fuel > 0) {
							tank.addLiquid(fuel, ReactorFluids.LIFBE_FUEL.get());
						}
						fullTicks = 0;
					}
					else {
						fullTicks++;
						if (fullTicks > 200) {
							this.overload(world, pos);
						}
					}
				}
			}
			if (tank.getFluidLevel() >= 125 && this.canDumpAt(world, pos.below())) {
				this.dumpFuel(world, pos);
			}
		}
	}

	private void dumpFuel(Level world, BlockPos pos) {
		BlockPos below = pos.below();
		int n1 = Math.min(8, tank.getFluidLevel()/125);
		int n2 = n1-1;
		BlockState bs = world.getBlockState(below);
		if (bs.getBlock() == ReactorBlocks.THORIUM_FUEL.get()) {
			int fmeta = bs.getValue(BlockThoriumFuel.LEVEL);
			n1 = Math.min(n1, 7-fmeta);
			n2 = n1+fmeta;
		}
		tank.removeLiquid(n1*125);
		world.setBlock(below, ReactorBlocks.THORIUM_FUEL.get().defaultBlockState().setValue(BlockThoriumFuel.LEVEL, Math.max(0, Math.min(7, n2))), 3);
		fullTicks = 0;
		ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.FIRE_EXTINGUISH, 1, 1);
	}

	private boolean canDumpAt(Level world, BlockPos pos) {
		BlockState bs = world.getBlockState(pos);
		return BlockThoriumFuel.canOverwrite(world, pos) || (bs.getBlock() == ReactorBlocks.THORIUM_FUEL.get() && bs.getValue(BlockThoriumFuel.LEVEL) < 7);
	}

	private void overload(Level world, BlockPos pos) {
		this.delete();
		world.explode(null, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 3, true, Level.ExplosionInteraction.BLOCK);
		world.setBlockAndUpdate(pos, ReactorBlocks.CORIUMFLOWING.get().defaultBlockState());
	}


	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return this.hasTile() ? this.getCore().canConnectToPipe(m) : false;
	}

	private boolean hasTile() {
		return this.getAdjacentBlockEntity(Direction.UP) instanceof TileEntityThoriumCore;
	}

	private TileEntityThoriumCore getCore() {
		return (TileEntityThoriumCore)this.getAdjacentBlockEntity(Direction.UP);
	}

	@Override
	public int getCapacity() {
		return 2000;
	}

	@Override
	public boolean canReceiveFrom(Direction from) {
		return false;
	}

	@Override
	public Fluid getInputFluid() {
		return null;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.FUELDUMP;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

}
