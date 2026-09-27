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
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.storage.FilteredFluidResourceHandler;
import reika.dragonapi.instantiable.storage.HybridTankResourceHandler;
import reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler;
import reika.dragonapi.instantiable.ParallelTicker;
import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.reactorcraft.auxiliary.recipe.ProcessorRecipe;
import reika.reactorcraft.base.TileEntityInventoriedReactorBase;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.container.MenuProcessor;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorRecipeTypes;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityUProcessor extends TileEntityInventoriedReactorBase implements PipeConnector, HasFluidResourceHandler {

	public TileEntityUProcessor(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.PROCESSOR.get(), pos, state);
	}

	private final HybridTank output = new HybridTank("uprocout", 3000);
	private final HybridTank intermediate = new HybridTank("uprocmid", 3000);
	private final HybridTank input = new HybridTank("uprocin", 3000);
	private final ResourceHandler<FluidResource> fluidHandler = new HybridTankResourceHandler(
			new HybridTank[] {input, intermediate, output},
			(index, resource) -> index == 0 && this.getProcessByInput(resource.getFluid()) != null
					|| index == 1 && resource.getFluid() == ReactorFluids.HF.get(),
			(index, resource) -> index == 2, this::setChanged);
	private final ResourceHandler<FluidResource> inputView = new FilteredFluidResourceHandler(
			fluidHandler, index -> index == 0, (index, resource) -> true,
			(index, resource) -> false);
	private final ResourceHandler<FluidResource> outputView = new FilteredFluidResourceHandler(
			fluidHandler, index -> index == 2, (index, resource) -> false,
			(index, resource) -> true);

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return side == null ? fluidHandler : side == facing ? outputView : inputView;
	}

	public int intermediate_timer;
	public int output_timer;

	private Direction facing = Direction.WEST;

	private ParallelTicker timer = new ParallelTicker().addTicker("intermediate", 0).addTicker("output", 0);

	private java.util.List<ProcessorRecipe> getRecipes() {
		if (level == null || level.getServer() == null)
			return java.util.List.of();
		return level.getServer().getRecipeManager().recipeMap()
				.byType(ReactorRecipeTypes.PROCESSOR.get()).stream()
				.map(holder -> holder.value()).toList();
	}

	public ProcessorRecipe getProcessByInput(Fluid fluid) {
		if (fluid == null)
			return null;
		return this.getRecipes().stream()
				.filter(recipe -> recipe.matches(new ProcessorRecipe.FluidInput(new FluidStack(fluid, 1)), level))
				.findFirst().orElse(null);
	}

	public ProcessorRecipe getProcessByOutput(Fluid fluid) {
		return this.getRecipes().stream()
				.filter(recipe -> recipe.getOutputFluid().getFluid().isSame(fluid))
				.findFirst().orElse(null);
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
		ProcessorRecipe p = this.getProcess();
		if (p == null)
			return;
		timer.setCap("intermediate", p.getIntermediateTime());
		timer.setCap("output", p.getOutputTime());
		if (!p.getIntermediateFluid().isEmpty() && this.canRunIntermediate(p)) {
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

	private ProcessorRecipe getProcess() {
		Fluid f = input.getActualFluid().getFluid();
		if (f == null)
			return null;
		return this.getProcessByInput(f);
	}

	public boolean canRunOutput(ProcessorRecipe p) {
		return this.hasInputItem(p) && (p.getIntermediateFluid().isEmpty()
				|| this.getIntermediate() >= p.getIntermediateConsumed())
				&& output.canTakeIn(p.getOutputFluid());
	}

	private boolean hasInputItem(ProcessorRecipe p) {
		if (itemHandler.getStackInSlot(2).isEmpty())
			return false;
		return p.getInputItem().test(itemHandler.getStackInSlot(2));
	}

	private boolean hasCatalyst(ProcessorRecipe p) {
		if (itemHandler.getStackInSlot(0).isEmpty())
			return false;
		ItemStack catalyst = itemHandler.getStackInSlot(0);
		return p.getCatalyst().test(catalyst) || ReikaItemHelper.isInOreTag(catalyst, "gemFluorite");
	}

	private boolean isFluorite(ItemStack is) {
		return ReactorItems.FLUORITE.matchWith(is) || ReikaItemHelper.isInOreTag(is, "gemFluorite");
	}

	public boolean canRunIntermediate(ProcessorRecipe p) {
		return this.getInput() >= p.getInputFluid().getAmount()
				&& intermediate.canTakeIn(p.getIntermediateFluid())
				&& this.hasCatalyst(p);
	}

	private void runIntermediate(ProcessorRecipe p) {
		ReikaInventoryHelper.decrStack(0, itemHandler);
		FluidStack intermediateStack = p.getIntermediateFluid();
		this.addIntermediate(intermediateStack.getAmount(), intermediateStack.getFluid());
		input.drain(p.getInputFluid().getAmount(), true);
	}

	private void runOutput(ProcessorRecipe p) {
		ReikaInventoryHelper.decrStack(2, itemHandler);
		if (p.getIntermediateFluid().isEmpty()) {
			ReikaInventoryHelper.decrStack(0, itemHandler);
			input.drain(p.getInputFluid().getAmount(), true);
		}
		output.fill(p.getOutputFluid(), true);
		intermediate.drain(p.getIntermediateConsumed(), true);
		if (p.getOutputFluid().getFluid().isSame(ReactorFluids.UF6.get())) {
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
		FluidStack fs = FluidUtil.getFirstStackContained(in);
		if (!fs.isEmpty() && this.getProcessByInput(fs.getFluid()) != null && this.canAcceptMoreInput(fs.getAmount())) {
			ResourceHandler<FluidResource> container = ItemAccess.forHandlerIndex(itemHandler, 1)
					.oneByOne().getCapability(Capabilities.Fluid.ITEM);
			if (container == null) return;
			try (Transaction transaction = Transaction.openRoot()) {
				var moved = ResourceHandlerUtil.moveFirst(container, inputView,
						resource -> this.getProcessByInput(resource.getFluid()) != null,
						fs.getAmount(), transaction);
				if (moved != null && moved.amount() == fs.getAmount()) transaction.commit();
			}
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
				return this.isFluorite(is) || this.getRecipes().stream()
						.anyMatch(recipe -> recipe.getCatalyst().test(is));
			case 1:
				return this.getProcessByFluidItem(is) != null;
			case 2:
				return this.getProcessByMainItem(is) != null;
		}
		return false;
	}

	public ProcessorRecipe getProcessByMainItem(ItemStack is) {
		return this.getRecipes().stream()
				.filter(recipe -> recipe.getInputItem().test(is))
				.findFirst().orElse(null);
	}

	public ProcessorRecipe getProcessByFluidItem(ItemStack is) {
		FluidStack fs = ReikaFluidHelper.getFluidForItem(is);
		if (fs.isEmpty())
			return null;
		return this.getProcessByInput(fs.getFluid());
	}

	public ProcessorRecipe getProcessByFluidOutputItem(ItemStack is) {
		FluidStack fs = ReikaFluidHelper.getFluidForItem(is);
		if (fs.isEmpty())
			return null;
		return this.getProcessByOutput(fs.getFluid());
	}








	public void addIntermediate(int amt, Fluid f) {
		intermediate.fill(new FluidStack(f, amt), true);
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
	public Flow getFlowForSide(Direction side) {
		return side == facing ? Flow.OUTPUT : Flow.INPUT;
	}

	public boolean hasWork() {
		ProcessorRecipe p = this.getProcess();
		return p != null && output.canTakeIn(p.getOutputFluid().getFluid(), p.getOutputFluid().getAmount());
	}

}
