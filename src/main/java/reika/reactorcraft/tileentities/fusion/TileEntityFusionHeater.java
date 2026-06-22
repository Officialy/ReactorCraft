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
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.FluidTankInfo;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.DragonAPICore;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityMagneticPipe;
import reika.rotarycraft.api.interfaces.Laserable;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.auxiliary.interfaces.TemperatureTE;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityFusionHeater extends TileEntityReactorBase implements TemperatureTE, Laserable, IFluidHandler, PipeConnector, MultiBlockTile {
	public TileEntityFusionHeater(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.HEATER.get(), pos, state);
	}


	public static final int PLASMA_TEMP = 150000000;

	private int temperature;

	private boolean hasMultiBlock = false;

	private final HybridTank tank = new HybridTank("fusionheater", 8000);
	private final HybridTank h2 = new HybridTank("fusionheaterh2", 4000);
	private final HybridTank h3 = new HybridTank("fusionheaterh3", 4000);

	public boolean hasMultiBlock() {
		return hasMultiBlock && !ReikaWorldHelper.isExposedToAir(level, xCoord, yCoord, zCoord);
	}

	public void setHasMultiBlock(boolean has) {
		hasMultiBlock = has && !ReikaWorldHelper.isExposedToAir(level, xCoord, yCoord, zCoord);
	}

	@Override
	public void whenInBeam(Level world, int x, int y, int z, long power, int range) {
		if (this.hasMultiBlock())
			temperature += 640*ReikaMathLibrary.logbase(power, 2);
	}

	public boolean blockBeam(Level world, int x, int y, int z, long power) {
		return this.hasMultiBlock();
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		this.updateTemperature(world, x, y, z, meta);

		if (DragonAPICore.debugtest) {
			temperature = 200000000;
		}

		//ReikaJavaLibrary.pConsole(temperature+": "+((float)temperature/PLASMA_TEMP), Dist.DEDICATED_SERVER);
		//ReikaJavaLibrary.pConsole(h2, Dist.DEDICATED_SERVER);

		if (this.canMake())
			this.make();
	}

	private boolean canMake() {
		return this.hasMultiBlock() && temperature >= PLASMA_TEMP && h2.getFluidLevel() >= 50 && h3.getFluidLevel() >= 50 && tank.canTakeIn(100);
	}

	private void make() {
		ReactorAchievements.PLASMA.triggerAchievement(this.getPlacer());
		h2.removeLiquid(50);
		h3.removeLiquid(50);
		tank.addLiquid(100, ReactorFluids.getLegacyFluid("rc fusion plasma"));
	}

	public void updateTemperature(Level world, int x, int y, int z, int meta) {
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, x, y, z);
		int dT = temperature-Tamb;
		if (dT != 0)
			temperature -= (1+dT/16384D);
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.HEATER;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	private boolean isHydrogen(Fluid f) {
		if (f.equals(ReactorFluids.getLegacyFluid("rc deuterium")))
			return true;
		if (f.equals(ReactorFluids.getLegacyFluid("rc tritium")))
			return true;
		return false;
	}

	@Override
	public void addTemperature(int temp) {
		temperature += temp;
	}

	@Override
	public int getTemperature() {
		return temperature;
	}

	@Override
	public int getThermalDamage() {
		return temperature/1000;
	}

	@Override
	public void overheat(Level world, int x, int y, int z) {

	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("temp", temperature);

		tank.writeToNBT(NBT);
		h2.writeToNBT(NBT);
		h3.writeToNBT(NBT);

		NBT.putBoolean("multi", hasMultiBlock);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		temperature = NBT.getIntOr("temp", 0);

		tank.readFromNBT(NBT);
		h2.readFromNBT(NBT);
		h3.readFromNBT(NBT);

		hasMultiBlock = NBT.getBooleanOr("multi", false);
	}

	@Override
	public int fill(Direction from, FluidStack resource, boolean doFill) {
		if (!this.canFill(from, resource.getFluid()))
			return 0;
		if (resource.getFluid().equals(ReactorFluids.getLegacyFluid("rc deuterium")))
			return h2.fill(resource, doFill);
		if (resource.getFluid().equals(ReactorFluids.getLegacyFluid("rc tritium")))
			return h3.fill(resource, doFill);
		return 0;
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
	public boolean canFill(Direction from, Fluid fluid) {
		return this.isHydrogen(fluid);
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return from == Direction.UP && this.getAdjacentTileEntity(from) instanceof TileEntityMagneticPipe && ReikaFluidHelper.isFluidDrainableFromTank(fluid, tank);
	}

	@Override
	public FluidTankInfo[] getTankInfo(Direction from) {
		return new FluidTankInfo[]{h2.getInfo(), h3.getInfo(), tank.getInfo()};
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return p.isStandardPipe() && side != Direction.UP;
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side == Direction.UP ? Flow.OUTPUT : Flow.INPUT;
	}

	@Override
	public int getTextureState(Direction side) {
		return 0;
	}

	@Override
	public boolean allowExternalHeating() {
		return false;
	}

	public void setTemperature(int temp) {
		temperature = temp;
	}

	@Override
	public int getMaxTemperature() {
		return Integer.MAX_VALUE;
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

}
