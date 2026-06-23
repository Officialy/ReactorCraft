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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.base.TankedReactorPowerReceiver;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.reactorcraft.tileentities.htgr.TileEntityPebbleBed;
import reika.rotarycraft.auxiliary.interfaces.TemperatureTE;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.ConfigRegistry;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityHeatExchanger extends TankedReactorPowerReceiver implements TemperatureTE {

	public TileEntityHeatExchanger(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.EXCHANGER.get(), pos, state);
	}

	public static final int CAPACITY = 2000;

	public static final int MINTEMP = -140;
	public static final int MAXTEMP = 1500;

	public static final int COOL_AMOUNT = 100;

	public static final int MINPOWER = 8192;
	public static final int MINSPEED = 512;

	private final HybridTank output = new HybridTank("exchangerout", this.getCapacity());

	private StepTimer temp = new StepTimer(20);

	private Exchange currentRecipe;

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.EXCHANGER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		currentRecipe = this.getExchange();
		if (this.canCool())
			this.cool();
		temp.update();
		if (temp.checkCap()) {
			this.distributeHeat(world, pos.getX(), pos.getY(), pos.getZ());
			this.updateTemperature(world, pos);
		}
	}

	public Exchange getCurrentRecipe() {
		return currentRecipe;
	}

	private void distributeHeat(Level world, int x, int y, int z) {
		for (int i = 2; i < 6; i++) {
			Direction dir = dirs[i];
			BlockPos p = new BlockPos(x+dir.getStepX(), y+dir.getStepY(), z+dir.getStepZ());
			ReactorTiles r = ReactorTiles.getTE(world, p);
			if (r == ReactorTiles.BOILER) {
				TileEntityReactorBoiler te = (TileEntityReactorBoiler)world.getBlockEntity(p);
				int dT = temperature - te.getTemperature();
				if (dT > 0) {
					temperature -= dT/4;
					int add = dT/4;
					te.setTemperature(te.getTemperature()+add);
					te.setReactorType(currentRecipe != null ? currentRecipe.type : ReactorType.NONE, add);
				}
			}
		}
	}

	private void cool() {
		tank.removeLiquid(COOL_AMOUNT);
		output.addLiquid(COOL_AMOUNT*currentRecipe.expansionRatio, currentRecipe.coldFluid);
		double eff = Math.min(1, Math.max(0.1, 1-(temperature-100)/(currentRecipe.maxTemperature-100D)));
		temperature += currentRecipe.heatCapacity*COOL_AMOUNT*eff;

		if (temperature > MAXTEMP)
			temperature = MAXTEMP;
		if (temperature < MINTEMP)
			temperature = MINTEMP;
	}

	private Exchange getExchange() {
		for (int i = 0; i < Exchange.list.length; i++) {
			Exchange e = Exchange.list[i];
			Fluid in = e.hotFluid;
			if (in != null && in.equals(tank.getActualFluid().getFluid()))
				return e;
		}
		return null;
	}

	private boolean canCoolFluid(Fluid f) {
		for (int i = 0; i < Exchange.list.length; i++) {
			Fluid fl = Exchange.list[i].hotFluid;
			if (fl.equals(f))
				return true;
		}
		return false;
	}

	private boolean canCool() {
		if (currentRecipe == null)
			return false;
		if (!this.sufficientPower())
			return false;

		return temperature < currentRecipe.maxTemperature && tank.getFluidLevel() >= COOL_AMOUNT && output.getRemainingSpace() >= COOL_AMOUNT*currentRecipe.expansionRatio && this.canCoolFluid(tank.getActualFluid().getFluid());
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return FluidStack.EMPTY;
		FluidStack out = output.getFluid();
		if (out.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, out))
			return FluidStack.EMPTY;
		return output.drain(resource.getAmount(), action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return output.drain(maxDrain, action);
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction action) {
		return from != Direction.DOWN ? output.drain(maxDrain, action) : FluidStack.EMPTY;
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
		return CAPACITY;
	}

	@Override
	public boolean canReceiveFrom(Direction from) {
		return from == Direction.UP;
	}

	@Override
	public Fluid getInputFluid() {
		return null;
	}

	@Override
	public boolean isValidFluid(Fluid f) {
		for (int i = 0; i < Exchange.list.length; i++) {
			Fluid in = Exchange.list[i].hotFluid;
			if (f.equals(in))
				return true;
		}
		return false;
	}

	//Add API to allow others to add fluids
	public static enum Exchange {
		SODIUM(ReactorFluids.HOT_SODIUM.get(), ReactorFluids.SODIUM.get(), ReikaThermoHelper.SODIUM_HEAT, 600, ReactorType.BREEDER),
		CO2("rc hot co2", "rc co2", ReikaThermoHelper.CO2_HEAT, TileEntityPebbleBed.MINTEMP, ReactorType.HTGR),
		LIFBE("rc hot lifbe", "rc lifbe", ReikaThermoHelper.LIFBE_HEAT, 1000, ReactorType.THORIUM),
		OXYGEN("rc liquid oxygen", "rc oxygen", 4, -ReikaThermoHelper.OXYGEN_HEAT-ReikaThermoHelper.OXYGEN_BOIL_ENTHALPY, 500, ReactorType.NONE),
		SOLARSODIUM(ReactorFluids.WARM_SODIUM.get(), ReactorFluids.SODIUM.get(), ReikaThermoHelper.SODIUM_HEAT*0.375F, 400, ReactorType.SOLAR),
		NITROGEN("rc liquid nitrogen", "nitrogen", 12, -ReikaThermoHelper.NITROGEN_HEAT-ReikaThermoHelper.NITROGEN_BOIL_ENTHALPY, 500, ReactorType.NONE);

		public final Fluid hotFluid;
		public final Fluid coldFluid;
		public final double heatCapacity;
		public final int maxTemperature;
		public final int expansionRatio;
		public final ReactorType type;

		public static final Exchange[] list = values();

		private Exchange(String from, String to, double c, int max, ReactorType t) {
			this(from, to, 1, c, max, t);
		}

		private Exchange(Fluid from, Fluid to, double c, int max, ReactorType t) {
			this(from, to, 1, c, max, t);
		}

		private Exchange(String from, String to, int r, double c, int max, ReactorType t) {
			this(ReactorFluids.getLegacyFluid(from), ReactorFluids.getLegacyFluid(to), r, c, max, t);
		}

		private Exchange(Fluid from, Fluid to, int r, double c, int max, ReactorType t) {
			coldFluid = to;
			hotFluid = from;
			heatCapacity = c;
			maxTemperature = max;
			type = t;
			expansionRatio = r;
		}
	}

	@Override
	public void updateTemperature(Level world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, pos);

		Direction waterside = ReikaWorldHelper.checkForAdjMaterial(world, pos, MapColor.WATER);
		if (waterside != null) {
			Tamb /= 2;
		}
		Direction iceside = ReikaWorldHelper.checkForAdjBlock(world, pos, Blocks.ICE);
		if (iceside != null) {
			if (Tamb > 0)
				Tamb /= 4;
			ReikaWorldHelper.changeAdjBlock(world, pos, iceside, Blocks.WATER.defaultBlockState());
		}
		Direction fireside = ReikaWorldHelper.checkForAdjBlock(world, pos, Blocks.FIRE);
		if (fireside != null) {
			Tamb += 200;
			if (temperature < 100)
				ReikaWorldHelper.changeAdjBlock(world, pos, fireside, Blocks.AIR.defaultBlockState());
			else {
				world.removeBlock(pos, false);
				world.explode(null, x+0.5, y+0.5, z+0.5, 6, ConfigRegistry.BLOCKDAMAGE.getState() ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
			}
		}
		Direction lavaside = ReikaWorldHelper.checkForAdjMaterial(world, pos, MapColor.FIRE);
		if (lavaside != null) {
			Tamb += 600;
			if (temperature < 100)
				ReikaWorldHelper.changeAdjBlock(world, pos, lavaside, Blocks.STONE.defaultBlockState());
			else {
				world.removeBlock(pos, false);
				world.explode(null, x+0.5, y+0.5, z+0.5, 6, ConfigRegistry.BLOCKDAMAGE.getState() ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
			}
		}
		if (temperature > Tamb)
			temperature--;
		if (temperature > Tamb*2)
			temperature--;
		if (temperature < Tamb)
			temperature++;
		if (temperature*2 < Tamb)
			temperature++;
		if (temperature > MAXTEMP)
			temperature = MAXTEMP;
		if (temperature < MINTEMP)
			temperature = MINTEMP;
		if (temperature > 100) {
			Direction side = ReikaWorldHelper.checkForAdjBlock(world, pos, Blocks.SNOW);
			if (side != null)
				ReikaWorldHelper.changeAdjBlock(world, pos, side, Blocks.AIR.defaultBlockState());
			side = ReikaWorldHelper.checkForAdjBlock(world, pos, Blocks.ICE);
			if (side != null)
				ReikaWorldHelper.changeAdjBlock(world, pos, side, Blocks.WATER.defaultBlockState());
		}
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
		return temperature/250;
	}

	@Override
	public void overheat(Level world, BlockPos pos) {

	}

	@Override
	public Flow getFlowForSide(Direction side) {
		if (side == Direction.DOWN)
			return Flow.NONE;
		return this.canReceiveFrom(side) ? Flow.INPUT : Flow.OUTPUT;
	}

	@Override
	public boolean canReadFrom(Direction dir) {
		return dir == Direction.DOWN;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		output.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		output.writeToNBT(NBT);
	}

	@Override
	public int getMinTorque(int available) {
		return 16;
	}

	@Override
	public int getMinTorque() {
		return 1;
	}

	@Override
	public int getMinSpeed() {
		return MINSPEED;
	}

	@Override
	public long getMinPower() {
		return MINPOWER;
	}

	@Override
	public boolean canBeCooledWithFins() {
		return true;
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
		return MAXTEMP;
	}

	@Override
	public boolean hasATank() {
		return true;
	}

	@Override
	public boolean hasAnInventory() {
		return false;
	}

}
