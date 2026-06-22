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
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.FluidTankInfo;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.DragonAPICore;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.ReactorCoreTE;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityTritizer extends TileEntityReactorBase implements ReactorCoreTE, PipeConnector, IFluidHandler {
	public TileEntityTritizer(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.TRITIZER.get(), pos, state);
	}


	public static final int CAPACITY = 1000;

	private final HybridTank input = new HybridTank("tritizerin", CAPACITY);
	private final HybridTank output = new HybridTank("tritizerout", CAPACITY);

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.TRITIZER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (DragonAPICore.debugtest) {
			input.addLiquid(100, ReactorCraft.H2);
			if (output.getFluidLevel() > CAPACITY/2)
				output.empty();
		}
		//this.onNeutron(null, world, x, y, z);

		if (!world.isClientSide()) {
			this.feed();
		}

		thermalTicker.update();
		if (thermalTicker.checkCap()) {
			this.updateTemperature(world, x, y, z);
		}
	}

	private void feed() {
		Level world = level;
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		BlockEntity tile = this.getAdjacentTileEntity(Direction.DOWN);
		if (tile instanceof TileEntityTritizer) {
			int amt = ((TileEntityTritizer)tile).feedIn(input.getFluid(), false);
			if (amt > 0) {
				input.removeLiquid(amt);
			}

			amt = ((TileEntityTritizer)tile).feedIn(output.getFluid(), true);
			if (amt > 0) {
				output.removeLiquid(amt);
			}
		}
	}

	private int feedIn(FluidStack is, boolean out) {
		if (is == null)
			return 0;
		HybridTank tank = out ? output : input;
		Fluid f = is.getFluid();
		if (!out && Reactions.getReactionFrom(f) == null)
			return 0;
		else if (tank.getActualFluid() != null && tank.getActualFluid() != f)
			return 0;
		else {
			int add = Math.min(tank.getRemainingSpace(), is.amount);
			tank.addLiquid(add, f);
			return add;
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		input.writeToNBT(NBT);
		output.writeToNBT(NBT);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		input.readFromNBT(NBT);
		output.readFromNBT(NBT);
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		if (input.isEmpty())
			return false;
		NeutronType type = e.getType();
		if (type.canIrradiateMaterials()) {
			Reactions r = Reactions.getReactionFrom(input.getActualFluid());
			if (!world.isClientSide() && this.canMake(r) && ReikaRandomHelper.doWithChance(r.chance)) {
				this.make(r);
				return true;
			}
		}
		return false;
	}

	private void make(Reactions r) {
		int amt = r.amount;
		input.removeLiquid(amt);
		output.addLiquid(amt, r.output);
	}

	private boolean canMake(Reactions r) {
		int amt = r.amount;
		return input.getFluidLevel() >= amt && output.canTakeIn(amt) && input.getActualFluid().equals(r.input);
	}

	public static enum Reactions {
		TRITIUM("rc deuterium", "rc tritium", 75, 25),
		D20("water", "rc heavy water", 25, 100);

		public final Fluid input;
		public final Fluid output;
		public final int chance;
		public final int amount;

		private static Reactions[] reactionList = values();

		private Reactions(String in, String out, int chance, int amt) {
			this(ReactorFluids.getLegacyFluid(in), ReactorFluids.getLegacyFluid(out), chance, amt);
		}

		private Reactions(Fluid in, Fluid out, int chance, int amt) {
			this.chance = chance;
			amount = amt;
			input = in;
			output = out;
		}

		private void onPerform(TileEntityTritizer te) {
			switch(this) {
				case D20:
					ReactorAchievements.HEAVYWATER.triggerAchievement(te.getPlacer());
					break;
				default:
					break;
			}
		}

		public static Reactions getReactionFrom(Fluid in) {
			for (int i = 0; i < reactionList.length; i++) {
				Reactions r = reactionList[i];
				if (r.input.equals(in))
					return r;
			}
			return null;
		}
	}

	public static void addRecipe(String name, Fluid in, Fluid out, int chance, int amt) {
		Class[] types = new Class[]{Fluid.class, Fluid.class, int.class, int.class};
		Object[] args = new Object[]{in, out, chance, amt};
		Reactions c = EnumHelper.addEnum(Reactions.class, name.toUpperCase(), types, args);
		Reactions.reactionList = Reactions.values();
	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return this.canDrain(from, resource.getFluid()) ? output.drain(resource.amount, doDrain) : null;
	}

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		if (!this.canDrain(from, null))
			return null;
		return output.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(Direction from, Fluid fluid) {
		return from == Direction.UP && Reactions.getReactionFrom(fluid) != ItemStack.EMPTY;
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return from == Direction.DOWN && ReikaFluidHelper.isFluidDrainableFromTank(fluid, output);
	}

	@Override
	public FluidTankInfo[] getTankInfo(Direction from) {
		return new FluidTankInfo[]{input.getInfo(), output.getInfo()};
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p) && side.offsetY != 0;
	}

	@Override
	public int fill(Direction from, FluidStack resource, boolean doFill) {
		if (!this.canFill(from, resource.getFluid()))
			return 0;
		return input.fill(resource, doFill);
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		if (side == Direction.UP)
			return Flow.INPUT;
		if (side == Direction.DOWN)
			return Flow.OUTPUT;
		return Flow.NONE;
	}

	@Override
	public final int getTextureState(Direction side) {
		if (side.offsetY != 0)
			return 4;
		Level world = level;
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		ReactorTiles src = this.getTile();
		ReactorTiles r = ReactorTiles.getTE(world, x, y-1, z);
		ReactorTiles r2 = ReactorTiles.getTE(world, x, y+1, z);
		if (r2 == src && r == src)
			return 2;
		else if (r2 == src)
			return 1;
		else if (r == src)
			return 3;
		return 0;
	}

}
