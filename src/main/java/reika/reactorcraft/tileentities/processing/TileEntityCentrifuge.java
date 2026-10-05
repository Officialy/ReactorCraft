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

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.container.MenuCentrifuge;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.storage.FilteredFluidResourceHandler;
import reika.dragonapi.instantiable.storage.HybridTankResourceHandler;
import reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.reactorcraft.auxiliary.recipe.CentrifugeRecipe;
import reika.reactorcraft.auxiliary.ReactorPowerReceiver;
import reika.reactorcraft.base.TileEntityInventoriedReactorBase;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorRecipeTypes;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.power.BasicPowerHandler;
import reika.rotarycraft.api.power.PowerTransferHelper;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityCentrifuge extends TileEntityInventoriedReactorBase implements ReactorPowerReceiver, PipeConnector, HasFluidResourceHandler {
	public TileEntityCentrifuge(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.CENTRIFUGE.get(), pos, state);
	}


	/** "In the range of 100000 rpm" -> 10.5k rad/s <br>
	 * http://science.howstuffworks.com/uranium-centrifuge.htm */
	//public static final int REAL_SPEED = 16384;

	public static final int MINSPEED = 262144; //much faster since doing it in one step

	public static final int UF6_PER_DUST = 50;
	public static final int FUEL_CHANCE = 9;

	private final HybridTank tank = new HybridTank("centri", 12000);
	private final ResourceHandler<FluidResource> fluidHandler = new HybridTankResourceHandler(
			new HybridTank[] {tank},
			(index, resource) -> this.getRecipe(resource.getFluid()) != null,
			(index, resource) -> true, this::setChanged);
	private final ResourceHandler<FluidResource> inputFluidView = new FilteredFluidResourceHandler(
			fluidHandler, index -> true, (index, resource) -> true,
			(index, resource) -> false);

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return side == null ? fluidHandler : side == Direction.UP ? inputFluidView : null;
	}

	private final StepTimer timer = new StepTimer(900);

	private final BasicPowerHandler powerHandler = new BasicPowerHandler();
	public int split; //timer

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.CENTRIFUGE;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		int omega = powerHandler.getOmega();
		if (omega >= 262144) {
			phi += 40;
		}
		else if (omega >= 65536) {
			phi += 30;
		}
		else if (omega >= 16384) {
			phi += 20;
		}
		else if (omega >= 4096) {
			phi += 15;
		}
		if (omega >= 1024) {
			phi += 10;
		}
		if (omega >= 256) {
			phi += 7;
		}
		else if (omega > 0) {
			phi += 5;
		}
		powerHandler.decrementIOTick(8);
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		timer.setCap(this.setTimer());

		if (!PowerTransferHelper.checkPowerFrom(this, Direction.DOWN)) {
			this.noInputMachine();
		}

		if (powerHandler.getPower() > 0 && !tank.isEmpty()) {
			CentrifugeRecipe recipe = this.getRecipe(tank.getActualFluid().getFluid());
			if (recipe != null && powerHandler.getOmega() >= recipe.getMinSpeed()
					&& tank.getFluidLevel() >= recipe.getInput().getAmount() && this.hasInventorySpace(recipe)) {
				timer.update(recipe.getSpeedFactor());
				if (timer.checkCap()) {
					if (!world.isClientSide())
						this.make(recipe);
				}
			}
			else {
				timer.reset();
			}
		}
		else {
			timer.reset();
		}
		if (!world.isClientSide()) {
			split = timer.getTick();
		}
	}

	private int setTimer() {
		int omega = powerHandler.getOmega();
		if (omega >= 67108864) {
			return 8;
		}
		else if (omega >= 33554432) {
			return 20;
		}
		else if (omega >= 16777216) {
			return 50;
		}
		else if (omega >= 8388608) {
			return 100;
		}
		else if (omega >= 4194304) {
			return 240;
		}
		else if (omega >= 2097152) {
			return 400;
		}
		else if (omega >= 1048576) {
			return 600;
		}
		else if (omega >= 524288) {
			return 800;
		}
		else
			return 900;
	}

	private CentrifugeRecipe getRecipe(Fluid fluid) {
		if (level == null || level.getServer() == null || fluid == null)
			return null;
		return level.getServer().getRecipeManager().recipeMap()
				.byType(ReactorRecipeTypes.CENTRIFUGE.get()).stream()
				.map(holder -> holder.value())
				.filter(recipe -> recipe.matches(new CentrifugeRecipe.FluidInput(new FluidStack(fluid, 1)), level))
				.findFirst().orElse(null);
	}

	private void make(CentrifugeRecipe recipe) {
		tank.removeLiquid(recipe.getInput().getAmount());
		if (ReikaRandomHelper.doWithPercentChance(recipe.getChanceOfAOverB())) {
			ReikaInventoryHelper.addOrSetStack(recipe.getOutputA(), itemHandler, 0);
		}
		else if (!recipe.getOutputB().isEmpty()) {
			ReikaInventoryHelper.addOrSetStack(recipe.getOutputB(), itemHandler, 1);
		}
	}

	private boolean hasInventorySpace(CentrifugeRecipe recipe) {
		if (!itemHandler.getStackInSlot(0).isEmpty() && !ReikaItemHelper.matchStacks(itemHandler.getStackInSlot(0), recipe.getOutputA()))
			return false;
		if (!itemHandler.getStackInSlot(1).isEmpty() && !recipe.getOutputB().isEmpty() && !ReikaItemHelper.matchStacks(itemHandler.getStackInSlot(1), recipe.getOutputB()))
			return false;
		if (!itemHandler.getStackInSlot(0).isEmpty() && itemHandler.getStackInSlot(0).getCount() >= itemHandler.getStackInSlot(0).getMaxStackSize())
			return false;
        return itemHandler.getStackInSlot(1).isEmpty() || itemHandler.getStackInSlot(1).getCount() < itemHandler.getStackInSlot(1).getMaxStackSize();
    }

	public int getProcessingScaled(int p) {
		return (int)(p*split/(float)timer.getCap());
	}

	public int getFluidScaled(int p) {
		return p*tank.getFluidLevel()/tank.getCapacity();
	}

	@Override
	public boolean canRemoveItem(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public int getContainerSize() {
		return 2;
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new MenuCentrifuge(id, inv, this);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public int getOmega() {
		return powerHandler.getOmega();
	}

	@Override
	public int getTorque() {
		return powerHandler.getTorque();
	}

	@Override
	public long getPower() {
		return powerHandler.getPower();
	}

	@Override
	public int getIORenderAlpha() {
		return powerHandler.getIORenderAlpha();
	}

	@Override
	public void setIORenderAlpha(int io) {
		powerHandler.setIORenderAlpha(io);
	}

	@Override
	public void setOmega(int omega) {
		powerHandler.setOmega(omega);
	}

	@Override
	public void setTorque(int torque) {
		powerHandler.setTorque(torque);
	}

	@Override
	public void setPower(long power) {
		powerHandler.setPower(power);
	}

	@Override
	public boolean canReadFrom(Direction dir) {
		return dir == Direction.DOWN;
	}

	@Override
	public boolean isReceiving() {
		return true;
	}

	@Override
	public void noInputMachine() {
		powerHandler.noInputMachine();
	}








	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		split = NBT.getIntOr("time", 0);
		timer.setCap(NBT.getIntOr("cap", 1)); // cap is recipe-dependent (server-only) — sync it for the client scale

		powerHandler.load(NBT);

		tank.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("time", split);
		NBT.putInt("cap", timer.getCap());

		powerHandler.saveAdditional(NBT);

		tank.writeToNBT(NBT);
	}

	public boolean canAcceptMoreUF6(int amt) {
		return tank.getFluid().isEmpty() || tank.getFluid().getAmount()+amt <= tank.getCapacity();
	}

	public void addUF6(int amt) {
		tank.addLiquid(amt, ReactorFluids.UF6.get());
	}

	public void removeFluid(int volume) {
		tank.removeLiquid(volume);
	}

	@Override
	public boolean canItemEnterFromSide(Direction dir) {
		return false;
	}

	@Override
	public boolean canItemExitToSide(Direction dir) {
		return dir.getStepY() != 0;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p) && side == Direction.UP;
	}



	@Override
	public Flow getFlowForSide(Direction side) {
		return side == Direction.UP ? Flow.INPUT : Flow.NONE;
	}

	@Override
	public int getMinTorque(int available) {
		return 1;
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
		return 1;
	}

	public int getUF6() {
		return tank.getActualFluid().getFluid() == ReactorFluids.UF6.get() ? tank.getFluidLevel() : 0;
	}

	public Fluid getFluid() {
		return tank.getActualFluid().getFluid();
	}

}
