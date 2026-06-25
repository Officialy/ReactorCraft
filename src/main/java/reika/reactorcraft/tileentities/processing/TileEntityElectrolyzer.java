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
import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.instantiable.data.KeyedItemStack;
import reika.dragonapi.instantiable.ItemMatch;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.base.TileEntityInventoriedReactorBase;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.interfaces.Shockable;
import reika.rotarycraft.api.interfaces.ThermalMachine;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.auxiliary.interfaces.TemperatureTE;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;
import reika.rotarycraft.registry.RotaryItems;

public class TileEntityElectrolyzer extends TileEntityInventoriedReactorBase implements IFluidHandler,
PipeConnector, TemperatureTE, ThermalMachine, Shockable {

	public TileEntityElectrolyzer(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.ELECTROLYZER.get(), pos, state);
	}

	public static final int SODIUM_MELT = 98;

	public static final int SALT_MELT = 801;

	public static final int CAPACITY = 6000;

	public static final int MAXTEMP = 1200;

	private final HybridTank tankL = new HybridTank("lighttank", this.getCapacity());
	private final HybridTank tankH = new HybridTank("heavytank", this.getCapacity());

	private final HybridTank input = new HybridTank("input", this.getCapacity()*2);

	private StepTimer timer = new StepTimer(50);
	private StepTimer tempTimer = new StepTimer(20);

	public int time;

	private int temperature;

	private Electrolysis recipe;

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.ELECTROLYZER;
	}

	public int getCapacity() {
		return CAPACITY;
	}

	public int getHLevel() {
		return tankH.getFluidLevel();
	}

	public int getLLevel() {
		return tankL.getFluidLevel();
	}

	public int getTime() {
		return timer.getTick();
	}

	public int getTimerScaled(int d) {
		return d * time / timer.getCap();
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		tempTimer.update();
		if (tempTimer.checkCap())
			this.updateTemperature(world, pos);
		if (recipe == null)
			recipe = this.findRecipe();
		if (!world.isClientSide()) {
			if (recipe != null && recipe.requirementsMet(this)) {
				if (timer.checkCap())
					recipe.run(this);
			}
			else {
				recipe = null;
				timer.reset();
			}
			time = timer.getTick();
		}
	}

	private Electrolysis findRecipe() {
		for (Electrolysis e : Electrolysis.recipes) {
			if (e.requirementsMet(this))
				return e;
		}
		return null;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p);
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side.getStepY() != 0 ? Flow.OUTPUT : Flow.INPUT;
	}

	// --- NeoForge IFluidHandler (tank 0 = heavy out, 1 = light out, 2 = input) ---
	@Override
	public int getTanks() {
		return 3;
	}

	@Override
	public FluidStack getFluidInTank(int t) {
		return t == 0 ? tankH.getFluid() : t == 1 ? tankL.getFluid() : input.getFluid();
	}

	@Override
	public int getTankCapacity(int t) {
		return t == 2 ? CAPACITY*2 : CAPACITY;
	}

	@Override
	public boolean isFluidValid(int t, FluidStack stack) {
		if (t != 2)
			return false;
		for (Electrolysis e : Electrolysis.recipes) {
			if (e.uses(stack.getFluid()))
				return true;
		}
		return false;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !this.isFluidValid(2, resource))
			return 0;
		return input.fill(resource, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return FluidStack.EMPTY;
		if (resource.getFluid() == tankH.getActualFluid().getFluid())
			return tankH.drain(resource.getAmount(), action);
		if (resource.getFluid() == tankL.getActualFluid().getFluid())
			return tankL.drain(resource.getAmount(), action);
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (!tankH.isEmpty())
			return tankH.drain(maxDrain, action);
		return tankL.drain(maxDrain, action);
	}

	@Override
	public int fillPipe(Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
		return from.getStepY() == 0 && !resource.isEmpty() && this.isFluidValid(2, resource) ? input.fill(resource, action) : 0;
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction doDrain) {
		if (from == Direction.DOWN)
			return tankH.drain(maxDrain, doDrain);
		if (from == Direction.UP)
			return tankL.drain(maxDrain, doDrain);
		return FluidStack.EMPTY;
	}

	@Override
	public boolean canRemoveItem(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player player) {
		return new reika.reactorcraft.container.MenuElectrolyzer(id, inv, this);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		for (Electrolysis e : Electrolysis.recipes) {
			if (e.uses(itemstack)) {
				return true;
			}
		}
		return false;
	}

	@Override
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
		return 0;
	}

	@Override
	public void overheat(Level world, BlockPos pos) {
		world.removeBlock(pos, false);
		world.explode(null, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 3F, Level.ExplosionInteraction.BLOCK);
	}

	@Override
	public void onDischarge(int charge, double range) {
		if (recipe != null) {
			int extra = charge-this.getMinDischarge();
			int n = extra > 0 ? (int)Math.sqrt(extra)/16 : 1;
			if (n == 0)
				n = 1;
			for (int i = 0; i < n; i++)
				timer.update();
		}
	}

	@Override
	public int getMinDischarge() {
		return 4096;
	}

	@Override
	public void setTemperature(int T) {
		temperature = T;
	}

	@Override
	public int getMaxTemperature() {
		return 1200;
	}

	@Override
	public void onOverheat(Level world, BlockPos pos) {

	}

	@Override
	public boolean canBeFrictionHeated() {
		return true;
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		tankH.writeToNBT(NBT);
		tankL.writeToNBT(NBT);
		input.writeToNBT(NBT);

		NBT.putInt("temp", temperature);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		tankH.readFromNBT(NBT);
		tankL.readFromNBT(NBT);
		input.readFromNBT(NBT);

		temperature = NBT.getIntOr("temp", 0);
	}

	public boolean addHeavyWater(int amt) {
		if (input.canTakeIn(amt)) {
			input.addLiquid(amt, ReactorFluids.getLegacyFluid("rc heavy water"));
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
		return false;
	}

	@Override
	public BlockPos getAimPos() {
		return this.getBlockPos();
	}

	public int getInputLevel() {
		return input.getFluidLevel();
	}

	@Override
	public boolean allowExternalHeating() {
		return true;
	}

	@Override
	public boolean canDischargeLongRange() {
		return false;
	}

	@Override
	public float getMultiplier() {
		return 0.5F;
	}

	@Override
	public void resetAmbientTemperatureTimer() {
		tempTimer.reset();
	}

	public boolean hasWork() {
		return recipe != null && tankH.canTakeIn(recipe.lowerOutput) && tankL.canTakeIn(recipe.upperOutput);
	}

	@Override
	public boolean hasAnInventory() {
		return true;
	}

	@Override
	public boolean hasATank() {
		return true;
	}

	public static final class Electrolysis {

		public static final ArrayList<Electrolysis> recipes = new ArrayList<>();

		// MOD-PORT: legacy ItemMatch("salt/dustSalt") oredict string -> direct match on the RotaryCraft salt item
		// (no String/tag ItemMatch ctor in 26.2); re-add a salt tag match if cross-mod salt support is wanted.
		public static final Electrolysis SALT = new Electrolysis(new ItemMatch(RotaryItems.SALT.toStack()), false, ReactorFluids.getLegacyFluid("rc chlorine"), 100, ReactorFluids.getLegacyFluid("rc sodium"), 100, SALT_MELT);
		public static final Electrolysis HEAVYWATER = new Electrolysis(ReactorFluids.getLegacyFluid("rc heavy water"), 100, null, false, ReactorFluids.getLegacyFluid("rc deuterium"), 100, ReactorFluids.getLegacyFluid("rc oxygen"), 50);

		public final FluidStack requiredFluid;
		private final ItemMatch requiredItem;
		public final boolean consumeItem;
		public final FluidStack upperOutput;
		public final FluidStack lowerOutput;
		public final int requiredTemperature;

		private Electrolysis(ItemMatch item, boolean cata, Fluid out1, int amt1, Fluid out2, int amt2, int temp) {
			this(null, 0, item, cata, out1, amt1, out2, amt2, temp);
		}

		private Electrolysis(Fluid in, int amt, ItemMatch item, boolean cata, Fluid out1, int amt1, Fluid out2, int amt2) {
			this(in, amt, item, cata, out1, amt1, out2, amt2, 0);
		}

		private Electrolysis(Fluid in, int amt, ItemMatch item, boolean cata, Fluid out1, int amt1, Fluid out2, int amt2, int temp) {
			requiredFluid = in != null ? new FluidStack(in, amt) : FluidStack.EMPTY;
			requiredItem = item != null ? item.copy() : null;
			consumeItem = !cata;
			upperOutput = out1 != null ? new FluidStack(out1, amt1) : FluidStack.EMPTY;
			lowerOutput = out2 != null ? new FluidStack(out2, amt2) : FluidStack.EMPTY;
			requiredTemperature = temp;
			recipes.add(this);
		}

		public boolean requirementsMet(TileEntityElectrolyzer te) {
			if (!requiredFluid.isEmpty()) {
				if (te.input.getActualFluid().getFluid() != requiredFluid.getFluid() || te.input.getFluidLevel() < requiredFluid.getAmount())
					return false;
			}
			if (requiredItem != null) {
				if (te.itemHandler.getStackInSlot(0).isEmpty() || !requiredItem.match(te.itemHandler.getStackInSlot(0)))
					return false;
			}
			if (!upperOutput.isEmpty()) {
				if (te.tankL.isFull() || (!te.tankL.isEmpty() && te.tankL.getActualFluid().getFluid() != upperOutput.getFluid()))
					return false;
			}
			if (!lowerOutput.isEmpty()) {
				if (te.tankH.isFull() || (!te.tankH.isEmpty() && te.tankH.getActualFluid().getFluid() != lowerOutput.getFluid()))
					return false;
			}
			return te.temperature >= requiredTemperature;
		}

		private void run(TileEntityElectrolyzer te) {
			if (!requiredFluid.isEmpty()) {
				te.input.removeLiquid(requiredFluid.getAmount());
			}
			if (requiredItem != null && consumeItem) {
				ReikaInventoryHelper.decrStack(0, te.itemHandler);
			}
			if (!upperOutput.isEmpty()) {
				te.tankL.addLiquid(upperOutput.getAmount(), upperOutput.getFluid());
			}
			if (!lowerOutput.isEmpty()) {
				te.tankH.addLiquid(lowerOutput.getAmount(), lowerOutput.getFluid());
			}
		}

		public boolean makes(Fluid f) {
			return (!upperOutput.isEmpty() && upperOutput.getFluid() == f) || (!lowerOutput.isEmpty() && lowerOutput.getFluid() == f);
		}

		public boolean uses(Fluid f) {
			return !requiredFluid.isEmpty() && requiredFluid.getFluid() == f;
		}

		public boolean uses(ItemStack is) {
			return requiredItem != null && requiredItem.match(is);
		}

		public static Electrolysis[] getRecipes() {
			return recipes.toArray(new Electrolysis[0]);
		}

		public boolean hasItemRequirement() {
			return requiredItem != null;
		}

		public Collection<ItemStack> getItemListForDisplay() {
			Collection<ItemStack> ret = new ArrayList();
			for (KeyedItemStack ks : requiredItem.getItemList()) {
				ret.add(ks.getItemStack());
			}
			return ret;
		}
	}

	public static void addRecipe(String name, Fluid in, int amt, ItemMatch item, boolean cata, Fluid out1, int amt1, Fluid out2, int amt2, int temp) {
		new Electrolysis(in, amt, item, cata, out1, amt1, out2, amt2, temp);
	}

}
