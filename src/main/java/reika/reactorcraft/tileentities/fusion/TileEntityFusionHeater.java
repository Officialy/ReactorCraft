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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityMagneticPipe;
import reika.rotarycraft.api.interfaces.Laserable;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.auxiliary.interfaces.TemperatureTE;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
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

	private boolean exposedToAir() {
		BlockPos p = this.getBlockPos();
		return ReikaWorldHelper.isExposedToAir(level, p.getX(), p.getY(), p.getZ());
	}

	public boolean hasMultiBlock() {
		return hasMultiBlock && !this.exposedToAir();
	}

	public void setHasMultiBlock(boolean has) {
		hasMultiBlock = has && !this.exposedToAir();
	}

	@Override
	public void whenInBeam(Level world, BlockPos pos, long power, int range) {
		if (this.hasMultiBlock())
			temperature += 640*ReikaMathLibrary.logbase(power, 2);
	}

	@Override
	public boolean blockBeam(Level world, BlockPos pos, long power) {
		return this.hasMultiBlock();
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		this.updateTemperature(world, pos);

		if (DragonAPI.debugtest) {
			temperature = 200000000;
		}

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

	@Override
	public void updateTemperature(Level world, BlockPos pos) {
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, pos);
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
	public void overheat(Level world, BlockPos pos) {

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

	// --- NeoForge IFluidHandler (0=deuterium in, 1=tritium in, 2=plasma out) ---
	@Override
	public int getTanks() {
		return 3;
	}

	@Override
	public FluidStack getFluidInTank(int t) {
		return t == 0 ? h2.getFluid() : t == 1 ? h3.getFluid() : tank.getFluid();
	}

	@Override
	public int getTankCapacity(int t) {
		return t == 2 ? 8000 : 4000;
	}

	@Override
	public boolean isFluidValid(int t, FluidStack stack) {
		return (t == 0 || t == 1) && this.isHydrogen(stack.getFluid());
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return 0;
		Fluid f = resource.getFluid();
		if (f.equals(ReactorFluids.getLegacyFluid("rc deuterium")))
			return h2.fill(resource, action);
		if (f.equals(ReactorFluids.getLegacyFluid("rc tritium")))
			return h3.fill(resource, action);
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return FluidStack.EMPTY;
		FluidStack out = tank.getFluid();
		if (out.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, out))
			return FluidStack.EMPTY;
		return tank.drain(resource.getAmount(), action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return tank.drain(maxDrain, action);
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
	public int fillPipe(Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
		return from != Direction.UP ? this.fill(resource, action) : 0;
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction action) {
		return from == Direction.UP && this.getAdjacentBlockEntity(from) instanceof TileEntityMagneticPipe ? tank.drain(maxDrain, action) : FluidStack.EMPTY;
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
	public boolean hasATank() {
		return true;
	}

	@Override
	public boolean hasAnInventory() {
		return false;
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

}
