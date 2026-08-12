/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fission.thorium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.interfaces.blockentity.InertIInv;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.reactorcraft.auxiliary.TemperaturedReactorTyped;
import reika.reactorcraft.base.TileEntityNuclearCore;
import reika.reactorcraft.container.MenuThoriumCore;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;
import reika.reactorcraft.tileentities.waste.TileEntityWastePipe;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

//Liquid Fueled
//Secondary Loop
//Does not spontaneously emit neutrons
//Cannot overheat (negative void coefficient)
//If gets over some temp, dumps fuel on ground
//Liquid waste
public class TileEntityThoriumCore extends TileEntityNuclearCore implements InertIInv, IFluidHandler, PipeConnector {

	public TileEntityThoriumCore(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.THORIUM.get(), pos, state);
	}

	private static final int CYCLE_AMOUNT = 100;
	public static final int FUEL_DUMP_TEMPERATURE = 1100;

	private final HybridTank fuelTank = new HybridTank("thoriumfuel", 4000);
	private final HybridTank fuelTankOut = new HybridTank("thoriumfuelout", 4000);
	private final HybridTank wasteTank = new HybridTank("thoriumwaste", 1000);

	private StepTimer timer2 = new StepTimer(5);

	private boolean isFuel(Fluid f) {
		return f == ReactorFluids.LIFBE_FUEL.get() || f == ReactorFluids.LIFBE_FUEL_PREHEAT.get();
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		if (DragonAPI.debugtest) {
			ReikaInventoryHelper.clearInventory(this);
			fuelTank.addLiquid(100, ReactorFluids.LIFBE_FUEL.get());
			if (fuelTankOut.getFluidLevel() >= fuelTankOut.getCapacity()/2)
				fuelTankOut.empty();
			wasteTank.empty();
		}

		if (!world.isClientSide()) {

			timer2.update();
			if (timer2.checkCap()) {
				for (int i = 2; i < 6; i++) {
					Direction dir = dirs[i];
					BlockPos p = pos.relative(dir);
					ReactorTiles r = ReactorTiles.getTE(world, p);
					if (r == this.getTile()) {
						this.balanceLiquidsWith((TileEntityThoriumCore)this.getAdjacentBlockEntity(dir));
					}
				}
			}
		}

		if (!world.isClientSide())
			this.feedFluid();
	}

	@Override
	protected int getRestingTemperature(Level world, BlockPos pos) {
		return fuelTank.getActualFluid().getFluid() == ReactorFluids.LIFBE_FUEL_PREHEAT.get() ? 250 : super.getRestingTemperature(world, pos);
	}

	private void balanceLiquidsWith(TileEntityThoriumCore te) {
		this.balanceTanks(wasteTank, te.wasteTank);
		this.balanceTanks(fuelTank, te.fuelTank);
		this.balanceTanks(fuelTankOut, te.fuelTankOut);
	}

	private void balanceTanks(HybridTank from, HybridTank to) {
		if (!to.getActualFluid().isEmpty() && to.getActualFluid().getFluid() != from.getActualFluid().getFluid())
			return;
		int dl = from.getFluidLevel()-to.getFluidLevel();
		if (dl > 1) {
			int amt = Math.min(from.getFluidLevel()/4, Math.max(1, dl/8+1));
			if (amt > 0) {
				to.addLiquid(amt, from.getActualFluid().getFluid());
				from.removeLiquid(amt);
			}
		}
	}

	@Override
	protected int getDecayNeutronChance() {
		return 30;
	}

	@Override
	protected int getWarningTemperature() {
		return 900;
	}

	@Override
	protected int getAmbientHeatLossFactor(Level world, BlockPos pos, int base, int Tamb) {
		return Tamb < temperature ? base*4 : base/2;
	}

	@Override
	protected float getHeatConductionThroughput(TemperaturedReactorTyped other) {
		if (other.getReactorType() != ReactorType.THORIUM)
			return 0.25F;
		return super.getHeatConductionThroughput(other);
	}

	private int getSameCoreHeatConductionFraction() {
		return 4;
	}

	@Override
	protected int getHeatConductionFraction(TemperaturedReactorTyped other) {
		return other.getReactorType() == ReactorType.FISSION ? 2 : super.getHeatConductionFraction(other);
	}

	@Override
	protected float getHeatConductionEfficiency(TemperaturedReactorTyped other) {
		boolean rest = temperature-this.getRestingTemperature(level, this.getBlockPos()) < 50;
		switch(other.getTile()) {
			case BOILER:
				return rest ? 0.125F : 0.75F;
			case FUEL:
				return rest ? 0.2F : 1F;
			default:
				return super.getHeatConductionEfficiency(other);
		}
	}

	private void feedFluid() {
		BlockEntity tile = this.getAdjacentBlockEntity(Direction.DOWN);
		if (tile instanceof TileEntityThoriumCore) {
			int amt = ((TileEntityThoriumCore)tile).feedFluidIn(fuelTank.getFluid(), 0);
			if (amt > 0) {
				fuelTank.removeLiquid(amt);
			}

			amt = ((TileEntityThoriumCore)tile).feedFluidIn(fuelTankOut.getFluid(), 1);
			if (amt > 0) {
				fuelTankOut.removeLiquid(amt);
			}

			amt = ((TileEntityThoriumCore)tile).feedFluidIn(wasteTank.getFluid(), 2);
			if (amt > 0) {
				wasteTank.removeLiquid(amt);
			}
		}
	}

	private int feedFluidIn(FluidStack is, int tankType) {
		if (is.isEmpty())
			return 0;
		HybridTank tank = null;
		switch(tankType) {
			case 0:
				tank = fuelTank;
				break;
			case 1:
				tank = fuelTankOut;
				break;
			case 2:
				tank = wasteTank;
				break;
		}
		if (tank == null)
			return 0;
		Fluid f = is.getFluid();
		if (!tank.getActualFluid().isEmpty() && tank.getActualFluid().getFluid() != f)
			return 0;
		else {
			int add = Math.min(tank.getRemainingSpace(), is.getAmount());
			tank.addLiquid(add, f);
			return add;
		}
	}

	int dumpFuel(TileEntityFuelDump te, int max) {
		int amt = Math.min(max, fuelTank.getFluidLevel());
		if (amt > 0) {
			fuelTank.removeLiquid(amt);
			ReactorAchievements.THORIUMDUMP.triggerAchievement(this.getPlacer());
		}
		return amt;
	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return liq == LiquidStates.LITHIUM;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack is) {
		return false;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		super.onNeutron(e, world, pos);
		if (!world.isClientSide()) {
			if (e.getNeutronType().canTriggerFission() && ReikaRandomHelper.doWithChance(e.getNeutronSpeed().getInteractionMultiplier()) && e.getNeutronType() != NeutronType.BREEDER && ReikaRandomHelper.doWithChance(this.getNeutronInteractionChance())) {
				if (this.checkPoisonedChance())
					return true;
				if (ReikaRandomHelper.doWithChance(this.getNeutronChance()) && this.hasFuel()) {
					fuelTank.removeLiquid(CYCLE_AMOUNT);
					fuelTankOut.addLiquid(CYCLE_AMOUNT, ReactorFluids.getLegacyFluid("rc hot lifbe"));
					temperature += 50;
					this.spawnNeutronBurst(world, pos);

					if (ReikaRandomHelper.doWithChance(5)) {
						this.addWaste();
					}
				}

				return true;
			}
		}
		return false;
	}

	private double getNeutronInteractionChance() {
		int midT = (this.getMinTemperature()+this.getMaxTemperature())/2;
		double f = temperature <= midT ? 0.5 : 0.75;
		return (1-f)+f*ReikaMathLibrary.cosInterpolation(this.getMinTemperature(), this.getMaxTemperature(), temperature);
	}

	private double getNeutronChance() {
		return 50-40*Math.sqrt((temperature-this.getMinTemperature())/(double)(this.getMaxTemperature()-this.getMinTemperature()));
	}

	public boolean hasFuel() {
		return fuelTank.getFluidLevel() >= CYCLE_AMOUNT;
	}

	@Override
	protected boolean checkPoisonedChance() {
		return ReikaRandomHelper.doWithChance(0.875*Math.pow(wasteTank.getFluidLevel()/(double)wasteTank.getCapacity(), 1.6));
	}

	@Override
	protected void addWaste() {
		wasteTank.addLiquid(CYCLE_AMOUNT/2, ReactorFluids.getLegacyFluid("rc nuclear waste"));
	}

	@Override
	public boolean isFissile() {
		return false;
	}

	private int getMinTemperature() {
		return 400;
	}

	@Override
	public int getMaxTemperature() {
		return 1200;
	}

	@Override
	public boolean canRemoveItem(int slot, ItemStack is) {
		return false;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.THORIUM;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	// --- NeoForge IFluidHandler (0=fuel in, 1=fuel out, 2=waste) ---
	@Override
	public int getTanks() {
		return 3;
	}

	@Override
	public FluidStack getFluidInTank(int t) {
		return t == 0 ? fuelTank.getFluid() : t == 1 ? fuelTankOut.getFluid() : wasteTank.getFluid();
	}

	@Override
	public int getTankCapacity(int t) {
		return t == 2 ? 1000 : 4000;
	}

	@Override
	public boolean isFluidValid(int t, FluidStack stack) {
		return t == 0 && this.isFuel(stack.getFluid());
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !this.isFuel(resource.getFluid()))
			return 0;
		return fuelTank.fill(resource, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return FluidStack.EMPTY;
		FluidStack out = fuelTankOut.getFluid();
		if (!out.isEmpty() && FluidStack.isSameFluidSameComponents(resource, out))
			return fuelTankOut.drain(resource.getAmount(), action);
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return fuelTankOut.drain(maxDrain, action);
	}

	private boolean isWastePipe(Direction from) {
		return this.getAdjacentBlockEntity(from) instanceof TileEntityWastePipe;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe() || m == MachineRegistry.FUELLINE;
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry m, Direction side) {
		return side == Direction.UP ? m == MachineRegistry.FUELLINE : m.isStandardPipe();
	}

	@Override
	public int fillPipe(Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
		return from == Direction.UP && !resource.isEmpty() && this.isFuel(resource.getFluid()) ? fuelTank.fill(resource, action) : 0;
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction action) {
		if (from == Direction.UP)
			return FluidStack.EMPTY;
		if (this.isWastePipe(from))
			return wasteTank.drain(maxDrain, action);
		return fuelTankOut.drain(maxDrain, action);
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side == Direction.UP ? Flow.INPUT : Flow.OUTPUT;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		fuelTank.readFromNBT(NBT);
		fuelTankOut.readFromNBT(NBT);
		wasteTank.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		fuelTank.writeToNBT(NBT);
		fuelTankOut.writeToNBT(NBT);
		wasteTank.writeToNBT(NBT);
	}

	// Override the inherited NuclearCore menu (hasGui() stays true from the parent).
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new MenuThoriumCore(id, inv, this);
	}

	@Override
	public ReactorType getReactorType() {
		return ReactorType.THORIUM;
	}

}
