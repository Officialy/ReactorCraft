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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.auxiliary.ReactorCoreTE;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityTritizer extends TileEntityReactorBase implements ReactorCoreTE, PipeConnector, HasFluidResourceHandler {

	public TileEntityTritizer(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.TRITIZER.get(), pos, state);
	}

	public static final int CAPACITY = 1000;

	private final HybridTank input = new HybridTank("tritizerin", CAPACITY);
	private final HybridTank output = new HybridTank("tritizerout", CAPACITY);
	private final ResourceHandler<FluidResource> fluidHandler = new HybridTankResourceHandler(
			new HybridTank[] {input, output},
			(index, resource) -> index == 0 && Reactions.getReactionFrom(resource.getFluid()) != null,
			(index, resource) -> index == 1, this::setChanged);
	private final ResourceHandler<FluidResource> inputView = new FilteredFluidResourceHandler(
			fluidHandler, index -> index == 0, (index, resource) -> true,
			(index, resource) -> false);
	private final ResourceHandler<FluidResource> outputView = new FilteredFluidResourceHandler(
			fluidHandler, index -> index == 1, (index, resource) -> false,
			(index, resource) -> true);

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return side == null ? fluidHandler : side == Direction.UP ? inputView
				: side == Direction.DOWN ? outputView : null;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.TRITIZER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (DragonAPI.debugtest) {
			input.addLiquid(100, ReactorFluids.DEUTERIUM.get());
			if (output.getFluidLevel() > CAPACITY/2)
				output.empty();
		}

		if (!world.isClientSide()) {
			this.feed();
		}

		thermalTicker.update();
		if (thermalTicker.checkCap()) {
			this.updateTemperature(world, pos);
		}
	}

	private void feed() {
		BlockEntity tile = this.getAdjacentBlockEntity(Direction.DOWN);
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
		if (is.isEmpty())
			return 0;
		HybridTank tank = out ? output : input;
		Fluid f = is.getFluid();
		if (!out && Reactions.getReactionFrom(f) == null)
			return 0;
		else if (!tank.getActualFluid().isEmpty() && tank.getActualFluid().getFluid() != f)
			return 0;
		else {
			int add = Math.min(tank.getRemainingSpace(), is.getAmount());
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
		NeutronType type = e.getNeutronType();
		if (type.canIrradiateMaterials()) {
			Reactions r = Reactions.getReactionFrom(input.getActualFluid().getFluid());
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
		r.onPerform(this);
	}

	private boolean canMake(Reactions r) {
		if (r == null)
			return false;
		int amt = r.amount;
		return input.getFluidLevel() >= amt && output.canTakeIn(amt) && input.getActualFluid().getFluid().equals(r.input);
	}

	public static final class Reactions {

		public static final ArrayList<Reactions> reactionList = new ArrayList<>();

		public static final Reactions TRITIUM = new Reactions("rc deuterium", "rc tritium", 75, 25);
		public static final Reactions D20 = new Reactions("water", "rc heavy water", 25, 100);

		public final Fluid input;
		public final Fluid output;
		public final int chance;
		public final int amount;

		private Reactions(String in, String out, int chance, int amt) {
			this(ReactorFluids.getLegacyFluid(in), ReactorFluids.getLegacyFluid(out), chance, amt);
		}

		private Reactions(Fluid in, Fluid out, int chance, int amt) {
			this.chance = chance;
			amount = amt;
			input = in;
			output = out;
			reactionList.add(this);
		}

		private void onPerform(TileEntityTritizer te) {
			if (this == D20) {
				ReactorAchievements.HEAVYWATER.triggerAchievement(te.getPlacer());
			}
		}

		public static Reactions getReactionFrom(Fluid in) {
			for (Reactions r : reactionList) {
				if (r.input.equals(in))
					return r;
			}
			return null;
		}
	}

	public static void addRecipe(String name, Fluid in, Fluid out, int chance, int amt) {
		new Reactions(in, out, chance, amt);
	}








	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p) && side.getStepY() != 0;
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
		if (side.getStepY() != 0)
			return 4;
		ReactorTiles src = this.getTile();
		ReactorTiles r = ReactorTiles.getTE(level, this.getBlockPos().below());
		ReactorTiles r2 = ReactorTiles.getTE(level, this.getBlockPos().above());
		if (r2 == src && r == src)
			return 2;
		else if (r2 == src)
			return 1;
		else if (r == src)
			return 3;
		return 0;
	}

}
