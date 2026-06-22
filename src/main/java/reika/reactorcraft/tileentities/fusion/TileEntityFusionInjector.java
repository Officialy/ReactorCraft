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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.FluidTankInfo;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.rendering.StructureRenderer;
import reika.dragonapi.interfaces.blockentity.ToggleTile;
import reika.reactorcraft.auxiliary.FusionReactorToroidPart;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityPlasma;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityMagneticPipe;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;


public class TileEntityFusionInjector extends TileEntityReactorBase implements IFluidHandler, PipeConnector, MultiBlockTile, FusionReactorToroidPart,
ToggleTile, NeutronTile {
	public TileEntityFusionInjector(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.INJECTOR.get(), pos, state);
	}


	private final HybridTank tank = new HybridTank("injector", 8000);

	private Direction facing;

	private boolean hasMultiBlock;

	private boolean enabled = true;

	public boolean hasMultiBlock() {
		return hasMultiBlock;
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
			this.make(world, x, y, z);
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

	private void make(Level world, int x, int y, int z) {
		this.createPlasma(world, x, y, z);
		tank.removeLiquid(2);
	}

	private void createPlasma(Level world, int x, int y, int z) {
		EntityPlasma e = new EntityPlasma(world, x, y, z, placer);
		e.setTarget(x+this.getFacing().offsetX, z+this.getFacing().offsetZ);
		if (!world.isClientSide())
			world.spawnEntityInWorld(e);
	}

	public int[] getTarget() {
		int dx = xCoord+this.getFacing().offsetX;
		int dz = zCoord+this.getFacing().offsetZ;
		return new int[]{dx, yCoord, dz};
	}

	public FusionReactorToroidPart getNextPart(Level world, int x, int y, int z) {
		int dx = xCoord+this.getFacing().offsetX*2;
		int dz = zCoord+this.getFacing().offsetZ*2;
		BlockEntity te = world.getBlockEntity(dx, y, dz);
		return te instanceof FusionReactorToroidPart ? (FusionReactorToroidPart)te : null;
	}

	public Direction getFacing() {
		if (FMLEnvironment.dist == Dist.CLIENT && this.shouldFlip())
			return System.currentTimeMillis()%4000 >= 2000 ? Direction.NORTH : Direction.SOUTH;
			return facing != null ? facing : Direction.EAST;
	}

	@SideOnly(Dist.CLIENT)
	private boolean shouldFlip() {
		return StructureRenderer.isRenderingTiles();
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

	@Override
	public int fill(Direction from, FluidStack resource, boolean doFill) {
		return this.canFill(from, resource.getFluid()) ? tank.fill(resource, doFill) : 0;
	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(Direction from, int amount, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(Direction from, Fluid fluid) {
		return fluid.equals(ReactorFluids.getLegacyFluid("rc fusion plasma")) && this.getAdjacentTileEntity(from) instanceof TileEntityMagneticPipe;
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(Direction from) {
		return new FluidTankInfo[]{tank.getInfo()};
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

		facing = dirs[NBT.getIntOr("face", 0)];

		hasMultiBlock = NBT.getBooleanOr("multi", false);

		if (NBT.hasKey("t_enable"))
			enabled = NBT.getBooleanOr("t_enable", false);
	}

	@Override
	public int getTextureState(Direction side) {
		return side == this.getFacing() ? 0 : side.offsetY != 0 ? 2 : 2;
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
		if (!level.isRemote) {
			for (int i = 0; i < 6; i++) {
				Direction dir = dirs[i];
				int dx = xCoord+dir.offsetX;
				int dy = yCoord+dir.offsetY;
				int dz = zCoord+dir.offsetZ;
				Block b = level.getBlock(dx, dy, dz);
				if (b instanceof BlockReCMultiBlock) {
					((BlockReCMultiBlock)b).breakMultiBlock(level, dx, dy, dz);
				}
			}
		}
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

}
