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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.ParallelTicker;
import reika.dragonapi.instantiable.data.KeyedItemStack;
import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.reactorcraft.auxiliary.ReactorStacks;
import reika.reactorcraft.base.TileEntityInventoriedReactorBase;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.container.MenuProcessor;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
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

	private Direction facing = Direction.WEST;

	private ParallelTicker timer = new ParallelTicker().addTicker("intermediate", 0).addTicker("output", 0);

	public static enum Processes {
		UF6("water", "rc hydrofluoric acid", "rc uranium hexafluoride", 250, 1000, 250, 125, 80, 400, ReactorItems.URANIUM_INGOT.toStack()),
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

		private static final HashMap<Fluid, Processes> processMap = new HashMap();
		private static final HashMap<Fluid, Processes> processOutputMap = new HashMap();
		public static final Processes[] list = values();

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

			// MOD-PORT: IC2 PURECRUSHEDU input gated out (IC2 not in 26.2 build); re-add when IC2Handler ports.
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
				processMap.put(p.inputFluid, p);
				processOutputMap.put(p.outputFluid, p);
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
		facing = this.getBlockState().getValue(BlockReactorMachine.FACING);
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
		Fluid f = input.getActualFluid().getFluid();
		if (f == null)
			return null;
		Processes p = Processes.processMap.get(f);
		if (p == null)
			return null;
		return p;
	}

	public boolean canRunOutput(Processes p) {
		return this.hasInputItem(p) && (!p.hasIntermediate() || this.getIntermediate() >= p.intermediateFluidConsumed) && this.canAcceptMoreOutput(p.outputFluidProduced);
	}

	private boolean hasInputItem(Processes p) {
		if (itemHandler.getStackInSlot(2).isEmpty())
			return false;
		return p.isValidItem(itemHandler.getStackInSlot(2));
	}

	private boolean hasFluorite() {
		if (itemHandler.getStackInSlot(0).isEmpty())
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
		ReikaInventoryHelper.decrStack(0, itemHandler);
		this.addIntermediate(p.intermediateFluidProduced, p.intermediateFluid);
		input.drain(p.inputFluidConsumed, IFluidHandler.FluidAction.EXECUTE);
	}

	private void runOutput(Processes p) {
		ReikaInventoryHelper.decrStack(2, itemHandler);
		if (!p.hasIntermediate()) {
			ReikaInventoryHelper.decrStack(0, itemHandler);
			input.drain(p.inputFluidConsumed, IFluidHandler.FluidAction.EXECUTE);
		}
		output.fill(new FluidStack(p.outputFluid, p.outputFluidProduced), IFluidHandler.FluidAction.EXECUTE);
		intermediate.drain(p.intermediateFluidConsumed, IFluidHandler.FluidAction.EXECUTE);
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
		return input.getActualFluid().getFluid();
	}

	public Fluid getIntermediateFluid() {
		return intermediate.getActualFluid().getFluid();
	}

	public Fluid getOutputFluid() {
		return output.getActualFluid().getFluid();
	}

	private void getFluidContainers() {
		ItemStack in = itemHandler.getStackInSlot(1);
		if (in.isEmpty())
			return;
		FluidStack fs = ReikaFluidHelper.getFluidForItem(in);
		if (!fs.isEmpty() && Processes.processMap.get(fs.getFluid()) != null && this.canAcceptMoreInput(fs.getAmount())) {
			FluidActionResult r = FluidUtil.tryEmptyContainer(in, this, fs.getAmount(), null, true);
			if (r.isSuccess())
				itemHandler.setStackInSlot(1, r.getResult());
		}
	}

	public boolean canAcceptMoreInput(int amt) {
		return input.getFluid().isEmpty() || input.getFluid().getAmount()+amt <= input.getCapacity();
	}

	public boolean canAcceptMoreIntermediate(int amt) {
		return intermediate.getFluid().isEmpty() || intermediate.getFluid().getAmount()+amt <= intermediate.getCapacity();
	}

	public boolean canAcceptMoreOutput(int amt) {
		return output.getFluid().isEmpty() || output.getFluid().getAmount()+amt <= output.getCapacity();
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
	public boolean hasGui() {
		return true;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new MenuProcessor(id, inv, this);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack is) {
		switch (i) {
			case 0:
				return this.isFluorite(is);
			case 1:
				return this.getProcessByFluidItem(is) != null;
			case 2:
				return this.getProcessByMainItem(is) != null;
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
		if (fs.isEmpty())
			return null;
		return Processes.processMap.get(fs.getFluid());
	}

	public static Processes getProcessByFluidOutputItem(ItemStack is) {
		FluidStack fs = ReikaFluidHelper.getFluidForItem(is);
		if (fs.isEmpty())
			return null;
		return Processes.processOutputMap.get(fs.getFluid());
	}

	public static Processes getProcessByInput(Fluid f) {
		return Processes.processMap.get(f);
	}

	public static Processes getProcessByOutput(Fluid f) {
		return Processes.processOutputMap.get(f);
	}

	// --- NeoForge IFluidHandler (0=input, 1=intermediate, 2=output) ---
	@Override
	public int getTanks() {
		return 3;
	}

	@Override
	public FluidStack getFluidInTank(int t) {
		return t == 0 ? input.getFluid() : t == 1 ? intermediate.getFluid() : output.getFluid();
	}

	@Override
	public int getTankCapacity(int t) {
		return 3000;
	}

	@Override
	public boolean isFluidValid(int t, FluidStack stack) {
		return t == 0 && Processes.processMap.containsKey(stack.getFluid());
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !Processes.processMap.containsKey(resource.getFluid()))
			return 0;
		return input.fill(resource, action);
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

	public void addIntermediate(int amt, Fluid f) {
		intermediate.fill(new FluidStack(f, amt), IFluidHandler.FluidAction.EXECUTE);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		output_timer = NBT.getIntOr("uf6", 0);
		intermediate_timer = NBT.getIntOr("hf", 0);
		timer.load(NBT, "ptmr"); // ticks + caps, so the client progress getters resolve

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
		timer.saveAdditional(NBT, "ptmr");

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
	public int fillPipe(Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
		return from != facing && !resource.isEmpty() && Processes.processMap.containsKey(resource.getFluid()) ? input.fill(resource, action) : 0;
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction action) {
		return from == facing ? output.drain(maxDrain, action) : FluidStack.EMPTY;
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side == facing ? Flow.OUTPUT : Flow.INPUT;
	}

	public boolean hasWork() {
		Processes p = this.getProcess();
		return p != null && output.canTakeIn(p.outputFluid, p.outputFluidProduced);
	}

}
