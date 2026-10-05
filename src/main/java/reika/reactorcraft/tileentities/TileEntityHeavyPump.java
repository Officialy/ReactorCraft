/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities;

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.storage.HybridTankResourceHandler;
import reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.level.ReikaBiomeHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.ReactorPowerReceiver;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorOptions;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.power.PowerTransferHelper;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityHeavyPump extends TileEntityReactorBase implements ReactorPowerReceiver, PipeConnector, HasFluidResourceHandler {

	public TileEntityHeavyPump(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.FLUIDEXTRACTOR.get(), pos, state);
	}

	public static final int MINPOWER = 65536;
	public static final int MINTORQUE = 512;
	private int torque;
	private int omega;
	private long power;
	private int iotick;

	private static final HashMap<Fluid, Extraction> extractions = new HashMap();

	static {
		extractions.put(Fluids.WATER, new HeavyWaterExtraction());
		extractions.put(Fluids.LAVA, new MoltenLithiumExtraction());
	}

	private final StepTimer timer = new StepTimer(20);

	private final HybridTank tank = new HybridTank("heavypump", 8000);
	private final ResourceHandler<FluidResource> fluidHandler = new HybridTankResourceHandler(
			new HybridTank[] {tank}, (index, resource) -> false,
			(index, resource) -> true, this::setChanged);

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return side == null || side.getAxis().isHorizontal() ? fluidHandler : null;
	}

	// The legacy world-fluid logic and config key on the old integer dimension ids; map the vanilla
	// dimensions back to those ids so ReactorConfig (still int-keyed) and the surface-Y logic stay faithful.
	private static int dimID(Level world) {
		ResourceKey<Level> d = world.dimension();
		if (d == Level.OVERWORLD)
			return 0;
		if (d == Level.NETHER)
			return -1;
		if (d == Level.END)
			return 1;
		return Integer.MIN_VALUE;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.FLUIDEXTRACTOR;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		if (power >= MINPOWER && torque >= MINTORQUE) {
			phi += 10F;
		}
		iotick -= 8;
	}

	@Override
	public void setOmega(int omega) {
		this.omega = omega;
	}

	@Override
	public void setTorque(int torque) {
		this.torque = torque;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public int getOmega() {
		return omega;
	}

	@Override
	public int getTorque() {
		return torque;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public boolean canReadFrom(Direction dir) {
		return dir.getStepY() != 0;
	}

	@Override
	public boolean isReceiving() {
		return true;
	}

	@Override
	public void noInputMachine() {
		omega = torque = 0;
		power = 0;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (!PowerTransferHelper.checkPowerFrom(this, Direction.DOWN) && !PowerTransferHelper.checkPowerFrom(this, Direction.UP)) {
			this.noInputMachine();
		}

		if (power >= MINPOWER && torque >= MINTORQUE) {
			timer.setCap(Math.max(1, 20-2*(int)ReikaMathLibrary.logbase(omega, 2)));
			timer.update();
			int x = pos.getX(), y = pos.getY(), z = pos.getZ();
			Extraction e = this.getExtraction(world, x, y, z);
			if (e != null) {
				if (timer.checkCap() && e.canPerform(world, x, y, z)) {
					this.harvest(e, world, x, y, z);
				}
			}
			else {
				timer.reset();
			}
		}
	}

	private Extraction getExtraction(Level world, int x, int y, int z) {
		Fluid f = null;
		int c = 0;
		for (int i = 2; i < 6; i++) {
			Direction dir = dirs[i];
			int dx = x+dir.getStepX();
			int dz = z+dir.getStepZ();
			FluidState fs = ReikaWorldHelper.getFluidState(world, dx, y, dz);
			Fluid f2 = fs.isEmpty() ? null : fs.getType();
			if (f2 != null && fs.isSource()) {
				if (f == null || f2 == f) {
					c++;
					f = f2;
				}
				else {
					return null;
				}
			}
		}
		return f != null && c >= 3 ? extractions.get(f) : null;
	}

	private void harvest(Extraction e, Level world, int x, int y, int z) {
		tank.fill(new FluidStack(e.output, e.getExtractedAmount(world, x, y, z)), true);
		e.onHarvest(world, x, y, z, this.getPlacer());
	}








	public boolean hasABucket() {
		return !tank.getFluid().isEmpty() && tank.getFluid().getAmount() >= FluidType.BUCKET_VOLUME;
	}

	public void subtractBucket() {
		tank.drain(FluidType.BUCKET_VOLUME, true);
	}

	public int getTankLevel() {
		return tank.getFluid().getAmount();
	}

	@Override
	public int getIORenderAlpha() {
		return iotick;
	}

	@Override
	public void setIORenderAlpha(int io) {
		iotick = io;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		tank.readFromNBT(NBT);

		omega = NBT.getIntOr("speed", 0);
		torque = NBT.getIntOr("trq", 0);
		power = NBT.getLongOr("pwr", 0L);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		tank.writeToNBT(NBT);

		NBT.putInt("speed", omega);
		NBT.putInt("trq", torque);
		NBT.putLong("pwr", power);
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p) && side.getStepY() == 0;
	}



	@Override
	public Flow getFlowForSide(Direction side) {
		return side.getStepY() == 0 ? Flow.OUTPUT : Flow.NONE;
	}

	@Override
	public int getMinTorque(int available) {
		return MINTORQUE;
	}

	@Override
	public int getMinTorque() {
		return MINTORQUE;
	}

	@Override
	public int getMinSpeed() {
		return 1;
	}

	@Override
	public long getMinPower() {
		return MINPOWER;
	}

	private static abstract class Extraction {

		protected final Fluid output;

		private Extraction(Fluid f) {
			output = f;
		}

		protected void onHarvest(Level world, int x, int y, int z, Player placer) {

		}

		protected abstract boolean canPerform(Level world, int x, int y, int z);

		protected abstract int getExtractedAmount(Level world, int x, int y, int z);

	}

	public static class HeavyWaterExtraction extends Extraction {

		public static final int MAXY = 45;
		public static final int MINDEPTH = 16;

		private HeavyWaterExtraction() {
			super(ReactorFluids.getLegacyFluid("rc heavy water"));
		}

		@Override
		protected boolean canPerform(Level world, int x, int y, int z) {
			return this.isValidWorld(world) && y < MAXY && ReikaBiomeHelper.isOcean(world, new BlockPos(x, y, z)) && this.isOceanFloor(world, x, y, z);
		}

		private boolean isValidWorld(Level world) {
			java.util.Set<Integer> dims = ReactorOptions.getHeavyWaterDimensions();
			return dims.isEmpty() || dims.contains(dimID(world));
		}

		@Override
		protected void onHarvest(Level world, int x, int y, int z, Player placer) {
			ReactorAchievements.HEAVYWATER.triggerAchievement(placer);
		}

		private boolean isOceanFloor(Level world, int x, int y, int z) {
			for (int i = 0; i < MINDEPTH; i++) {
				int dy = y+i;
				for (int a = -1; a <= 1; a += 2) {
					for (int b = -1; b <= 1; b += 2) {
						if (world.getFluidState(new BlockPos(x+a, dy, z+b)).getType() != Fluids.WATER) {
							return false;
						}
					}
				}
				if (i >= 1) {
					if (world.getFluidState(new BlockPos(x, dy, z)).getType() != Fluids.WATER) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		protected int getExtractedAmount(Level world, int x, int y, int z) {
			return 200;
		}

	}

	private static class MoltenLithiumExtraction extends Extraction {

		private MoltenLithiumExtraction() {
			super(ReactorFluids.getLegacyFluid("rc lithium"));
		}

		@Override
		protected boolean canPerform(Level world, int x, int y, int z) {
			return y == this.getSurfaceY(world, x, y, z) && this.isLavaSurface(world, x, y, z);
		}

		private boolean isLavaSurface(Level world, int x, int y, int z) {
			Fluid below = world.getFluidState(new BlockPos(x, y-1, z)).getType();
			Fluid above = world.getFluidState(new BlockPos(x, y+1, z)).getType();
			return (below == Fluids.LAVA || below == Fluids.FLOWING_LAVA) && (above != Fluids.LAVA && above != Fluids.FLOWING_LAVA);
		}

		private int getSurfaceY(Level world, int x, int y, int z) {
			switch(dimID(world)) {
				case 0:
					return 10;
				case -1:
					return 31;
				default:
					return -1;
			}
		}

		@Override
		protected int getExtractedAmount(Level world, int x, int y, int z) {
			return dimID(world) == -1 ? 10+rand.nextInt(21)+rand.nextInt(51) : 10+rand.nextInt(31);
		}

	}

	public Fluid getFluid() {
		return tank.getActualFluid().getFluid();
	}

}
