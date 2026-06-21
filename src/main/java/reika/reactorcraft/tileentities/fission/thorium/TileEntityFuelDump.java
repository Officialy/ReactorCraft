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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.TileEntityTankedReactorMachine;
import reika.reactorcraft.blocks.BlockThoriumFuel;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.registry.MachineRegistry;


public class TileEntityFuelDump extends TileEntityTankedReactorMachine {
	public TileEntityFuelDump(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.FUELDUMP.get(), pos, state);
	}


	private int fullTicks = 0;

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (!world.isClientSide()) {
			BlockEntity te = this.getAdjacentTileEntity(Direction.UP);
			if (te instanceof TileEntityThoriumCore) {
				TileEntityThoriumCore tc = (TileEntityThoriumCore)te;
				if (tc.getTemperature() >= tc.FUEL_DUMP_TEMPERATURE && tc.hasFuel()) {
					int rem = tank.getRemainingSpace();
					if (rem > 0) {
						int fuel = ((TileEntityThoriumCore)te).dumpFuel(this, rem);
						if (fuel > 0) {
							tank.addLiquid(fuel, ReactorCraft.LIFBe_fuel);
						}
						fullTicks = 0;
					}
					else {
						fullTicks++;
						if (fullTicks > 200) {
							this.overload(world, x, y, z);
						}
					}
				}
			}
			if (tank.getFluidLevel() >= 125 && this.canDumpAt(world, x, y-1, z)) {
				this.dumpFuel(world, x, y, z);
			}
		}
	}

	private void dumpFuel(Level world, int x, int y, int z) {
		int n1 = Math.min(8, tank.getFluidLevel()/125);
		int n2 = n1-1;
		if (world.getBlock(x, y-1, z) == ReactorBlocks.THORIUM.getBlockInstance()) {
			int fmeta = world.getBlockMetadata(x, y-1, z);
			n1 = Math.min(n1, 7-fmeta);
			n2 = n1+fmeta;
		}
		tank.removeLiquid(n1*125);
		world.setBlock(x, y-1, z, ReactorBlocks.THORIUM.getBlockInstance(), n2, 3);
		fullTicks = 0;
		ReikaSoundHelper.playSoundFromServerAtBlock(world, x, y, z, "random.fizz", 1, 1, true);
	}

	private boolean canDumpAt(Level world, int x, int y, int z) {
		return BlockThoriumFuel.canOverwrite(world, x, y, z) || (world.getBlock(x, y, z) == ReactorBlocks.THORIUM.getBlockInstance() && world.getBlockMetadata(x, y, z) < 7);
	}

	private void overload(Level world, int x, int y, int z) {
		this.delete();
		world.newExplosion(null, x+0.5, y+0.5, z+0.5, 3, true, true);
		world.setBlock(x, y, z, ReactorBlocks.CORIUMFLOWING.getBlockInstance());
	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return this.hasTile() ? this.getCore().drain(from, resource, doDrain) : null;
	}

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		return this.hasTile() ? this.getCore().drain(from, maxDrain, doDrain) : null;
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return this.hasTile() ? this.getCore().canDrain(from, fluid) : false;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return this.hasTile() ? this.getCore().canConnectToPipe(m) : false;
	}

	private boolean hasTile() {
		return this.getAdjacentTileEntity(Direction.UP) instanceof TileEntityThoriumCore;
	}

	private TileEntityThoriumCore getCore() {
		return (TileEntityThoriumCore)this.getAdjacentTileEntity(Direction.UP);
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
