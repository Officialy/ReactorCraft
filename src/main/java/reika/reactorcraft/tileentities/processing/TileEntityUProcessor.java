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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids./*FLUIDCONTAINER-PORT*/ FluidContainerRegistry;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.FluidTankInfo;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import reika.dragonapi.ModList;
import reika.dragonapi.asm.apistripper.Strippable;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.ParallelTicker;
import reika.dragonapi.instantiable.data.KeyedItemStack;
import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.modinteract.itemhandlers.IC2Handler;
import reika.reactorcraft.auxiliary.ReactorStacks;
import reika.reactorcraft.base.TileEntityInventoriedReactorBase;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityUProcessor extends TileEntityInventoriedReactorBase implements IFluidHandler, PipeConnector {
	public TileEntityUProcessor(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.PROCESSOR.get(), pos, state);
	}


	private final HybridTank output = new HybridTank("uprocout", 3000);
	private final HybridTank intermediate = new HybridTank("uprocmid", 3000);
	private final HybridTank input = new HybridTank("uprocin", 3000);

	public int intermediate_timer;
	public int output_timer;

	private Direction facing;

	private ParallelTicker timer = new ParallelTicker().addTicker("intermediate", 0).addTicker("output", 0);

	public static enum Processes {
		UF6("water", "rc hydrofluoric acid", "rc uranium hexafluoride", 250, 1000, 250, 125, 80, 400, "ingotUranium"),
		LiFBe("rc lithium", "rc hydrofluoric acid", "rc lifbe", 100, 500, 250, 1500, 120, 600, ReactorStacks.emeralddust);

		public final int intermediateTime;
		public final int ouputTime;

		public final Fluid inputFluid;
		public final Fluid intermediateFluid;
		public final Fluid outputFluid;

		public final int inputFluidConsumed;
		public final int outputFluidProduced;

		public final int intermediateFluidProduced;
		public final int intermediateFluidConsumed;

		private final HashSet<KeyedItemStack> inputItem = new HashSet();

		private static final HashMap<String, Processes> processMap = new HashMap();
		private static final HashMap<String, Processes> processOutputMap = new HashMap();
		public static final Processes[] list = values();

		private Processes(String f, String f1, String f2, int incons, int outprod, int prod, int cons, int t1, int t2, String in) {
			this(f, f1, f2, incons, outprod, prod, cons, t1, t2, new ArrayList(OreDictionary.getOres(in)));
		}

		private Processes(String f, String f1, String f2, int incons, int outprod, int prod, int cons, int t1, int t2, ItemStack in) {
			this(f, f1, f2, incons, outprod, prod, cons, t1, t2, ReikaJavaLibrary.makeListFrom(in));
		}

		private Processes(String f, String f1, String f2, int incons, int outprod, int prod, int cons, int t1, int t2, Collection<ItemStack> in) {
			inputFluid = ReactorFluids.getLegacyFluid(f);
			intermediateFluid = ReactorFluids.getLegacyFluid(f1);
			outputFluid = ReactorFluids.getLegacyFluid(f2);

			intermediateTime = t1;
			ouputTime = t2;

			inputFluidConsumed = incons;
			outputFluidProduced = outprod;

			intermediateFluidProduced = prod;
			intermediateFluidConsumed = cons;

			if (f2.equals("rc uranium hexafluoride")) {
				if (ModList.IC2.isLoaded()) {
					ItemStack is = IC2Handler.IC2Stacks.PURECRUSHEDU.getItem();
					if (is != null)
						in.add(is);
				}
			}
			for (ItemStack is : in) {
				inputItem.add(new KeyedItemStack(is).setSimpleHash(true));
			}
		}

		public boolean hasIntermediate() {
			return intermediateFluid != null && intermediateFluidProduced > 0;
		}

		static {
			for (int i = 0; i < list.length; i++) {
				Processes p = list[i];
				processMap.put(p.inputFluid.getName(), p);
				processOutputMap.put(p.outputFluid.getName(), p);
			}
		}

		public boolean isValidItem(ItemStack is) {
			return inputItem.contains(new KeyedItemStack(is).setSimpleHash(true));
		}

		public List<ItemStack> getInputItemList() {
			ArrayList li = new ArrayList();
			for (KeyedItemStack ks : inputItem) {
				li.add(ks.getItemStack());
			}
			return li;
		}

	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.PROCESSOR;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		this.getFacing(meta);
		this.getFluidContainers();
		Processes p = this.getProcess();
		if (p == null)
			return;
		timer.setCap("intermediate", p.intermediateTime);
		timer.setCap("output", p.ouputTime);
		if (p.hasIntermediate() && this.canRunIntermediate(p)) {
			timer.updateTicker("intermediate");
			if (timer.checkCap("intermediate"))
				this.runIntermediate(p);
		}
		else {
			timer.resetTicker("intermediate");
		}

		if (this.canRunOutput(p)) {
			timer.updateTicker("output");
			if (timer.checkCap("output"))
				this.runOutput(p);
		}
		else {
			timer.resetTicker("output");
		}

		if (!world.isClientSide()) {
			intermediate_timer = timer.getTickOf("intermediate");
			output_timer = timer.getTickOf("output");
		}
	}

	private Processes getProcess() {
		Fluid f = input.getActualFluid();
		if (f == null)
			return null;
		Processes p = Processes.processMap.get(f.getName());
		if (p == null)
			return null;
		if (p.hasIntermediate() && !this.hasFluorite())
			;//return null;
		if (!this.hasInputItem(p))
			;//return null;
		return p;
	}

	public boolean canRunOutput(Processes p) {
		return this.hasInputItem(p) && (!p.hasIntermediate() || this.getIntermediate() >= p.intermediateFluidConsumed) && this.canAcceptMoreOutput(p.outputFluidProduced);
	}

	private boolean hasInputItem(Processes p) {
		if (itemHandler.getStackInSlot(2) == null)
			return false;
		return p.isValidItem(itemHandler.getStackInSlot(2));
	}

	private boolean hasFluorite() {
		if (itemHandler.getStackInSlot(0) == null)
			return false;
		return this.isFluorite(itemHandler.getStackInSlot(0));
	}

	private boolean isFluorite(ItemStack is) {
		return ReactorItems.FLUORITE.matchWith(is) || ReikaItemHelper.isInOreTag(is, "gemFluorite");
	}

	public boolean canRunIntermediate(Processes p) {
		return this.getInput() > 0 && this.canAcceptMoreIntermediate(p.intermediateFluidProduced) && this.hasFluorite();
	}

	private void runIntermediate(Processes p) {
		ReikaInventoryHelper.decrStack(0, inv);
		this.addIntermediate(p.intermediateFluidProduced, p.intermediateFluid);
		input.drain(p.inputFluidConsumed, true);
	}

	private void runOutput(Processes p) {
		ReikaInventoryHelper.decrStack(2, inv);
		if (!p.hasIntermediate()) {
			ReikaInventoryHelper.decrStack(0, inv);
			input.drain(p.inputFluidConsumed, true);
		}
		output.fill(new FluidStack(p.outputFluid, p.outputFluidProduced), true);
		intermediate.drain(p.intermediateFluidConsumed, true);
		if (p == Processes.UF6) {
			ReactorAchievements.UF6.triggerAchievement(this.getPlacer());
		}
	}

	public int getIntermediateTimerScaled(int p) {
		return (int)(p*timer.getPortionOfCap("intermediate"));
	}

	public int getOutputTimerScaled(int p) {
		return (int)(p*timer.getPortionOfCap("output"));
	}

	public int getInputScaled(int p) {
		return p*this.getInput()/input.getCapacity();
	}

	public int getIntermediateScaled(int p) {
		return p*this.getIntermediate()/intermediate.getCapacity();
	}

	public int getOutputScaled(int p) {
		return p*this.getOutput()/output.getCapacity();
	}

	public int getCapacity() {
		return input.getCapacity();
	}

	public int getInput() {
		return input.getFluidLevel();
	}

	public int getIntermediate() {
		return intermediate.getFluidLevel();
	}

	public int getOutput() {
		return output.getFluidLevel();
	}

	public Fluid getInputFluid() {
		return input.getActualFluid();
	}

	public Fluid getIntermediateFluid() {
		return intermediate.getActualFluid();
	}

	public Fluid getOutputFluid() {
		return output.getActualFluid();
	}

	private void getFluidContainers() {
		if (itemHandler.getStackInSlot(1) != null) {
			FluidStack fs = ReikaFluidHelper.getFluidForItem(itemHandler.getStackInSlot(1));
			if (fs != null && Processes.processMap.get(fs.getFluid().getName()) != null && this.canAcceptMoreInput(fs.amount)) {
				input.fill(fs.copy(), true);
				itemHandler.getStackInSlot(1) = /*FLUIDCONTAINER-PORT*/ FluidContainerRegistry.drainFluidContainer(itemHandler.getStackInSlot(1));
			}
		}
	}

	public boolean canAcceptMoreInput(int amt) {
		return input.getFluid() == null || input.getFluid().amount+amt <= input.getCapacity();
	}

	public boolean canAcceptMoreIntermediate(int amt) {
		return intermediate.getFluid() == null || intermediate.getFluid().amount+amt <= intermediate.getCapacity();
	}

	public boolean canAcceptMoreOutput(int amt) {
		return output.getFluid() == null || output.getFluid().amount+amt <= output.getCapacity();
	}

	@Override
	public boolean canRemoveItem(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public int getContainerSize() {
		return 3;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack is) {
		switch (i) {
			case 0:
				return this.isFluorite(is);
			case 1:
				return this.getProcessByFluidItem(is) != ItemStack.EMPTY;
			case 2:
				return this.getProcessByMainItem(is) != ItemStack.EMPTY;
		}
		return false;
	}

	public static Processes getProcessByMainItem(ItemStack is) {
		for (int i = 0; i < Processes.list.length; i++) {
			Processes p = Processes.list[i];
			if (p.isValidItem(is)) {
				return p;
			}
		}
		return null;
	}

	public static Processes getProcessByFluidItem(ItemStack is) {
		FluidStack fs = ReikaFluidHelper.getFluidForItem(is);
		if (fs == null)
			return null;
		return Processes.processMap.get(fs.getFluid().getName());
	}

	public static Processes getProcessByFluidOutputItem(ItemStack is) {
		FluidStack fs = ReikaFluidHelper.getFluidForItem(is);
		if (fs == null)
			return null;
		return Processes.processOutputMap.get(fs.getFluid().getName());
	}

	public static Processes getProcessByInput(Fluid f) {
		return Processes.processMap.get(f.getName());
	}

	public static Processes getProcessByOutput(Fluid f) {
		return Processes.processOutputMap.get(f.getName());
	}

	@Override
	public int fill(Direction from, FluidStack resource, boolean doFill) {
		if (!this.canFill(from, resource.getFluid()))
			return 0;
		return input.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		return output.drain(maxDrain, doDrain);
	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return this.canDrain(from, resource.getFluid()) ? output.drain(resource.amount, doDrain) : null;
	}

	@Override
	public boolean canFill(Direction from, Fluid fluid) {
		for (int i = 0; i < Processes.list.length; i++) {
			if (Processes.list[i].inputFluid.equals(fluid))
				return true;
			if (Processes.list[i].intermediateFluid != null && Processes.list[i].intermediateFluid.equals(fluid))
				return true;
		}
		return false;
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		for (int i = 0; i < Processes.list.length; i++) {
			if (Processes.list[i].outputFluid.equals(fluid))
				return true;
		}
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(Direction from) {
		return new FluidTankInfo[]{input.getInfo(), intermediate.getInfo(), output.getInfo()};
	}

	public void addIntermediate(int amt, Fluid f) {
		int a = intermediate.fill(new FluidStack(f, amt), true);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		output_timer = NBT.getIntOr("uf6", 0);
		intermediate_timer = NBT.getIntOr("hf", 0);

		input.readFromNBT(NBT);
		intermediate.readFromNBT(NBT);
		output.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		NBT.putInt("uf6", output_timer);
		NBT.putInt("hf", intermediate_timer);

		input.writeToNBT(NBT);
		intermediate.writeToNBT(NBT);
		output.writeToNBT(NBT);
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
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p);
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side == facing ? Flow.OUTPUT : Flow.INPUT;
	}

	private void getFacing(int meta) {
		switch(meta) {
			case 0:
				facing = Direction.WEST;
				break;
			case 1:
				facing = Direction.EAST;
				break;
			case 2:
				facing = Direction.NORTH;
				break;
			case 3:
				facing = Direction.SOUTH;
				break;
		}
	}

	@Override
	public boolean hasWork() {
		Processes p = this.getProcess();
		return p != null && output.canTakeIn(p.outputFluid, p.outputFluidProduced);
	}

}
