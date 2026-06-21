/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;

import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.reactorcraft.auxiliary.SteamTile;
import reika.reactorcraft.base.TileEntityTankedReactorMachine;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamLine;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntitySteamDiffuser extends TileEntityTankedReactorMachine implements SteamTile {
	public TileEntitySteamDiffuser(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.DIFFUSER.get(), pos, state);
	}


	public static final int RATIO = calculateConversionRatio();

	private int steam;

	//613K at 14MPa to 373K at 0.1MPa
	private static int calculateConversionRatio() { //Moran & Shapiro
		double nuReact = 0.01272;
		double nuSATP = 1.696;
		double efficiency = 0.6;
		return (int)Math.ceil(nuSATP/nuReact*efficiency); //80
	}

	public Direction getFacing() {
		switch(this) {
			case 0:
				return Direction.WEST;
			case 1:
				return Direction.EAST;
			case 2:
				return Direction.NORTH;
			case 3:
				return Direction.SOUTH;
			default:
				return Direction.UNKNOWN;
		}
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.DIFFUSER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		this.getSteam(world, x, y, z);

		this.convertSteam();
	}

	private void convertSteam() {
		if (steam > 0) {
			Fluid f = ReactorFluids.getLegacyFluid("steam");
			if (f != null) {
				int amt = Math.min(1+steam/4, tank.getRemainingSpace()/RATIO);
				tank.addLiquid(amt*RATIO*1000, f);
				steam -= amt;
			}
		}
	}

	private void getSteam(Level world, int x, int y, int z) {
		//for (int i = 0; i < 6; i++) {
		Direction dir = this.getFacing();//dirs[i];
		int dx = x+dir.offsetX;
		int dy = y+dir.offsetY;
		int dz = z+dir.offsetZ;
		ReactorTiles rt = ReactorTiles.getTE(world, dx, dy, dz);
		if (rt == ReactorTiles.STEAMLINE) {
			TileEntitySteamLine te = (TileEntitySteamLine)world.getBlockEntity(dx, dy, dz);
			int ds = te.getSteam()-steam;
			if (ds > 0) {
				int rm = ds/4+1;
				steam += rm*te.getWorkingFluid().efficiency;
				te.removeSteam(rm);
			}
		}
		//}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		steam = NBT.getIntOr("energy", 0);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		NBT.putInt("energy", steam);
	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return this.canDrain(from, resource.getFluid()) ? tank.drain(resource.amount, doDrain) : null;
	}

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		return this.canDrain(from, null) ? tank.drain(maxDrain, doDrain) : null;
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return from == this.getFacing().getOpposite() && ReikaFluidHelper.isFluidDrainableFromTank(fluid, tank);
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p) && side == this.getFacing().getOpposite();
	}

	@Override
	public int getCapacity() {
		return 2500000;
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
	public Flow getFlowForSide(Direction side) {
		return side == this.getFacing().getOpposite() ? Flow.OUTPUT : Flow.NONE;
	}

	@Override
	public int getSteam() {
		return steam;
	}

}
