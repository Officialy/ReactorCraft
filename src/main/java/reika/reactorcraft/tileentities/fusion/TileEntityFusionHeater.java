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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.storage.FilteredFluidResourceHandler;
import reika.dragonapi.instantiable.storage.HybridTankResourceHandler;
import reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler;
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

public class TileEntityFusionHeater extends TileEntityReactorBase implements TemperatureTE, Laserable, PipeConnector, MultiBlockTile, HasFluidResourceHandler {

	public TileEntityFusionHeater(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.HEATER.get(), pos, state);
	}

	public static final int PLASMA_TEMP = 150000000;

	private int temperature;

	private boolean hasMultiBlock = false;

	private final HybridTank tank = new HybridTank("fusionheater", 8000);
	private final HybridTank h2 = new HybridTank("fusionheaterh2", 4000);
	private final HybridTank h3 = new HybridTank("fusionheaterh3", 4000);
	private final ResourceHandler<FluidResource> fluidHandler = new HybridTankResourceHandler(
			new HybridTank[] {h2, h3, tank},
			(index, resource) -> index == 0
					? resource.getFluid() == ReactorFluids.getLegacyFluid("rc deuterium")
					: index == 1 && resource.getFluid() == ReactorFluids.getLegacyFluid("rc tritium"),
			(index, resource) -> index == 2, this::setChanged);
	private final ResourceHandler<FluidResource> inputView = new FilteredFluidResourceHandler(
			fluidHandler, index -> index < 2, (index, resource) -> true,
			(index, resource) -> false);
	private final ResourceHandler<FluidResource> outputView = new FilteredFluidResourceHandler(
			fluidHandler, index -> index == 2, (index, resource) -> false,
			(index, resource) -> true);

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return side == null ? fluidHandler : side == Direction.UP
				? this.getAdjacentBlockEntity(side) instanceof TileEntityMagneticPipe ? outputView : null
				: inputView;
	}

	private boolean exposedToAir() {
		BlockPos p = this.getBlockPos();
		return ReikaWorldHelper.isExposedToAir(level, p.getX(), p.getY(), p.getZ());
	}

	public boolean hasMultiBlock() {
		return hasMultiBlock && !this.exposedToAir();
	}

	@Override
	protected void onFirstTick(Level world, BlockPos pos) {
		if (!world.isClientSide())
			this.setHasMultiBlock(reika.reactorcraft.blocks.multi.BlockHeaterMulti.isComplete(world, pos));
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
        return f.equals(ReactorFluids.getLegacyFluid("rc tritium"));
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
