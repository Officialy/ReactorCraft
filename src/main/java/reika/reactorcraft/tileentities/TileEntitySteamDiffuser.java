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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.reactorcraft.auxiliary.SteamTile;
import reika.reactorcraft.base.TileEntityTankedReactorMachine;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamLine;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
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
		return (int) Math.ceil(nuSATP / nuReact * efficiency); //80
	}

	public Direction getFacing() {
		return this.getBlockState().getValue(BlockReactorMachine.FACING);
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.DIFFUSER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		this.getSteam(world, pos);
		this.convertSteam();
	}

	private void convertSteam() {
		if (steam > 0) {
			Fluid f = ReactorFluids.getLegacyFluid("steam");
			if (f != null) {
				int amt = Math.min(1 + steam / 4, tank.getRemainingSpace() / RATIO);
				tank.addLiquid(amt * RATIO * 1000, f);
				steam -= amt;
			}
		}
	}

	private void getSteam(Level world, BlockPos pos) {
		BlockPos npos = pos.relative(this.getFacing());
		if (ReactorTiles.getTE(world, npos) == ReactorTiles.STEAMLINE) {
			TileEntitySteamLine te = (TileEntitySteamLine) world.getBlockEntity(npos);
			int ds = te.getSteam() - steam;
			if (ds > 0) {
				int rm = ds / 4 + 1;
				steam += rm * te.getWorkingFluid().efficiency;
				te.removeSteam(rm);
			}
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);
		steam = NBT.getIntOr("energy", 0);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);
		NBT.putInt("energy", steam);
	}

	// The condensed working fluid is drained out of the face opposite the steam intake.
	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction doDrain) {
		return from == this.getFacing().getOpposite() ? tank.drain(maxDrain, doDrain) : FluidStack.EMPTY;
	}

	@Override
	public BlockEntityPiping.Flow getFlowForSide(Direction side) {
		return side == this.getFacing().getOpposite() ? BlockEntityPiping.Flow.OUTPUT : BlockEntityPiping.Flow.NONE;
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
	public int getSteam() {
		return steam;
	}

}
