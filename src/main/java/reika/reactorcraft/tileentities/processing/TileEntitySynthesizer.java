/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.processing;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.instantiable.recipe.FlexibleIngredient;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.ReactorStacks;
import reika.reactorcraft.base.TileEntityInventoriedReactorBase;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.interfaces.ThermalMachine;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntitySynthesizer extends TileEntityInventoriedReactorBase implements IFluidHandler, ThermalMachine, PipeConnector {

	public TileEntitySynthesizer(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.SYNTHESIZER.get(), pos, state);
	}

	private static final int WATER_PER_AMMONIA = 250;
	private static final int AMMONIA_PER_STEP = 1000;
	public static final int AMMONIATEMP = 220;
	private static final int MAXTEMP = 1000;

	private static final HashMap<Fluid, FluidSynthesis> fluidMap = new HashMap();

	public int timer;
	private FluidSynthesis recipe;

	private final HybridTank tank = new HybridTank("synthout", 24000);
	private final HybridTank water = new HybridTank("synthwater", 24000);

	private StepTimer steptimer = new StepTimer(1800);
	private StepTimer tempTimer = new StepTimer(20);

	public static final class FluidSynthesis {

		public static final ArrayList<FluidSynthesis> list = new ArrayList<>();

		public static final FluidSynthesis AMMONIA = new FluidSynthesis(Fluids.WATER, ReactorFluids.AMMONIA.get(), WATER_PER_AMMONIA, AMMONIA_PER_STEP, AMMONIATEMP, 50, 0, constructItemMatch("dustQuicklime", ReactorStacks.lime, 1), constructItemMatch("dustAmmonium", ReactorStacks.ammonium, 1));
		public static final FluidSynthesis HOTLIFBE = new FluidSynthesis(ReactorFluids.LIFBE_FUEL.get(), ReactorFluids.LIFBE_FUEL_PREHEAT.get(), 50, 50, 350, 100, 5);

		public final Fluid input;
		public final Fluid output;
		public final int fluidConsumed;
		public final int fluidProduced;
		public final int minTemp;
		public final int baseDuration;
		private final int temperatureSpeedCurve;
		private final FlexibleIngredient itemA;
		private final FlexibleIngredient itemB;

		private FluidSynthesis(Fluid in, Fluid out, int amtin, int amtout, int temp, int time, int tc) {
			this(in, out, amtin, amtout, temp, time, tc, null);
		}

		private FluidSynthesis(Fluid in, Fluid out, int amtin, int amtout, int temp, int time, int tc, FlexibleIngredient is) {
			this(in, out, amtin, amtout, temp, time, tc, is, null);
		}

		private FluidSynthesis(Fluid in, Fluid out, int amtin, int amtout, int temp, int time, int tc, FlexibleIngredient a, FlexibleIngredient b) {
			input = in;
			output = out;
			fluidConsumed = amtin;
			fluidProduced = amtout;
			minTemp = temp;
			baseDuration = time;
			temperatureSpeedCurve = tc;
			itemA = a;
			itemB = b;
			if (fluidMap.containsKey(input))
				throw new IllegalArgumentException("Fluid "+input+" already mapped to a recipe!");
			fluidMap.put(input, this);
			list.add(this);
		}

		public ItemStack getAForDisplay() {
			return itemA != null ? itemA.getItemForDisplay(true) : ItemStack.EMPTY;
		}

		public ItemStack getBForDisplay() {
			return itemB != null ? itemB.getItemForDisplay(true) : ItemStack.EMPTY;
		}

		public boolean usesItem(ItemStack item) {
			return (itemA != null && itemA.match(item)) || (itemB != null && itemB.match(item));
		}

		private static FlexibleIngredient constructItemMatch(ItemStack is) {
			return new FlexibleIngredient(is, 100, is.getCount());
		}

		private static FlexibleIngredient constructItemMatch(String ore, int amt) {
			return constructItemMatch(ore, null, amt);
		}

		private static FlexibleIngredient constructItemMatch(String ore, ItemStack is, int amt) {
			FlexibleIngredient ret = new FlexibleIngredient(ore, 100, amt);
			if (is != null)
				ret.addItem(is);
			return ret;
		}

		public int getDuration(int temperature) {
			return Math.max(5, baseDuration-temperatureSpeedCurve*(temperature-minTemp)/100);
		}
	}

	public static void addRecipe(String name, Fluid in, Fluid out, int amtin, int amtout, int temp, int time, int curve, FlexibleIngredient a, FlexibleIngredient b) {
		new FluidSynthesis(in, out, amtin, amtout, temp, time, curve, a, b);
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.SYNTHESIZER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		this.getWaterBuckets();
		recipe = this.getRecipe();
		if (recipe != null)
			steptimer.setCap(recipe.getDuration(temperature));
		if (recipe != null && water.getFluidLevel() >= recipe.fluidConsumed && temperature >= recipe.minTemp && tank.canTakeIn(recipe.output, recipe.fluidProduced)) {
			steptimer.update();
			if (steptimer.checkCap())
				this.make();
		}
		else
			steptimer.reset();
		timer = steptimer.getTick();
		tempTimer.update();
		if (tempTimer.checkCap()) {
			this.updateTemperature(world, pos);
		}
	}

	private FluidSynthesis getRecipe() {
		if (water.isEmpty())
			return null;
		FluidSynthesis fr = fluidMap.get(water.getActualFluid().getFluid());
		if (fr == null)
			return null;
		if (fr.itemA != null && !fr.itemA.match(itemHandler.getStackInSlot(1)))
			return null;
		if (fr.itemB != null && !fr.itemB.match(itemHandler.getStackInSlot(2)))
			return null;
		return fr;
	}

	public void updateTemperature(Level world, BlockPos pos) {
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
		}
		Direction lavaside = ReikaWorldHelper.checkForAdjMaterial(world, pos, MapColor.FIRE);
		if (lavaside != null) {
			Tamb += 600;
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
		if (temperature > 100) {
			Direction side = ReikaWorldHelper.checkForAdjBlock(world, pos, Blocks.SNOW);
			if (side != null)
				ReikaWorldHelper.changeAdjBlock(world, pos, side, Blocks.AIR.defaultBlockState());
			side = ReikaWorldHelper.checkForAdjBlock(world, pos, Blocks.ICE);
			if (side != null)
				ReikaWorldHelper.changeAdjBlock(world, pos, side, Blocks.WATER.defaultBlockState());
		}
	}

	private void make() {
		if (recipe.itemA != null)
			ReikaInventoryHelper.decrStack(1, itemHandler);
		if (recipe.itemB != null)
			ReikaInventoryHelper.decrStack(2, itemHandler);
		water.removeLiquid(recipe.fluidConsumed);
		tank.addLiquid(recipe.fluidProduced, recipe.output);
	}

	public int getWaterScaled(int px) {
		return water.getFluidLevel()*px/water.getCapacity();
	}

	public int getAmmoniaScaled(int px) {
		return tank.getFluidLevel() * px / tank.getCapacity();
	}

	public int getTimerScaled(int px) {
		return steptimer.getTick() * px / steptimer.getCap();
	}

	private void getWaterBuckets() {
		ItemStack in = itemHandler.getStackInSlot(0);
		if (!in.isEmpty() && in.getCount() == 1 && in.getItem() == Items.WATER_BUCKET && water.canTakeIn(Fluids.WATER, FluidType.BUCKET_VOLUME)) {
			water.addLiquid(FluidType.BUCKET_VOLUME, Fluids.WATER);
			itemHandler.setStackInSlot(0, new ItemStack(Items.BUCKET));
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	// --- NeoForge IFluidHandler (0=output product, 1=water input) ---
	@Override
	public int getTanks() {
		return 2;
	}

	@Override
	public FluidStack getFluidInTank(int t) {
		return t == 0 ? tank.getFluid() : water.getFluid();
	}

	@Override
	public int getTankCapacity(int t) {
		return 24000;
	}

	@Override
	public boolean isFluidValid(int t, FluidStack stack) {
		return t == 1 && fluidMap.containsKey(stack.getFluid());
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !fluidMap.containsKey(resource.getFluid()))
			return 0;
		return water.fill(resource, action);
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
	public boolean canRemoveItem(int i, ItemStack itemstack) {
		return itemstack.getItem() == Items.BUCKET;
	}

	@Override
	public int getContainerSize() {
		return 3;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack is) {
		if (i == 0)
			return is.getItem() == Items.WATER_BUCKET;
		for (FluidSynthesis rec : FluidSynthesis.list) {
			if (rec.itemA != null && rec.itemA.match(is))
				return i == 1;
			else if (rec.itemB != null && rec.itemB.match(is))
				return i == 2;
		}
		return false;
	}

	public String getInputFluid() {
		return water.isEmpty() ? null : water.getActualFluid().getHoverName().getString();
	}

	public String getOutputFluid() {
		return tank.isEmpty() ? null : tank.getActualFluid().getHoverName().getString();
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		timer = NBT.getIntOr("time", 0);

		water.readFromNBT(NBT);
		tank.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		NBT.putInt("time", timer);

		water.writeToNBT(NBT);
		tank.writeToNBT(NBT);
	}

	@Override
	public int getTemperature() {
		return temperature;
	}

	@Override
	public void setTemperature(int T) {
		temperature = T;
	}

	@Override
	public void addTemperature(int T) {
		temperature += T;
	}

	@Override
	public int getMaxTemperature() {
		return MAXTEMP;
	}

	@Override
	public void onOverheat(Level world, BlockPos pos) {

	}

	@Override
	public boolean canBeFrictionHeated() {
		return true;
	}

	public boolean addWater(int amt) {
		if (water.canTakeIn(amt)) {
			water.addLiquid(amt, Fluids.WATER);
			return true;
		}
		return false;
	}

	@Override
	public boolean canItemEnterFromSide(Direction dir) {
		return true;
	}

	@Override
	public boolean canItemExitToSide(Direction dir) {
		return true;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe() || m == MachineRegistry.FUELLINE;
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry m, Direction side) {
		return this.canConnectToPipe(m);
	}

	@Override
	public int fillPipe(Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
		return from.getStepY() == 0 && !resource.isEmpty() && fluidMap.containsKey(resource.getFluid()) ? water.fill(resource, action) : 0;
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction action) {
		return from.getStepY() != 0 ? tank.drain(maxDrain, action) : FluidStack.EMPTY;
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side.getStepY() == 0 ? Flow.INPUT : Flow.OUTPUT;
	}

	@Override
	public float getMultiplier() {
		return 1;
	}

	@Override
	public void resetAmbientTemperatureTimer() {
		tempTimer.reset();
	}

	public boolean hasWork() {
		return recipe != null && tank.canTakeIn(recipe.output, recipe.fluidProduced);
	}

	@Override
	public boolean hasATank() {
		return true;
	}

	@Override
	public boolean hasAnInventory() {
		return true;
	}

}
