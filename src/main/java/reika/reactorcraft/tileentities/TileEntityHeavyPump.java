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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import java.util.HashMap;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids./*FLUIDCONTAINER-PORT*/ FluidContainerRegistry;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.FluidTankInfo;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.level.ReikaBiomeHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.ReactorPowerReceiver;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.power.PowerTransferHelper;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityHeavyPump extends TileEntityReactorBase implements ReactorPowerReceiver, IFluidHandler, PipeConnector {
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
		extractions.put(FluidRegistry.WATER, new HeavyWaterExtraction());
		extractions.put(FluidRegistry.LAVA, new MoltenLithiumExtraction());
	}

	private StepTimer timer = new StepTimer(20);

	private final HybridTank tank = new HybridTank("heavypump", 8000);

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
		return dir.offsetY != 0;
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
		Fluid f = ItemStack.EMPTY;
		int c = 0;
		for (int i = 2; i < 6; i++) {
			Direction dir = dirs[i];
			int dx = x+dir.offsetX;
			int dz = z+dir.offsetZ;
			Fluid f2 = ReikaWorldHelper.getFluid(world, dx, y, dz);
			if (f2 != null && ReikaWorldHelper.isLiquidSourceBlock(world, dx, y, dz)) {
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

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		if (from.offsetY != 0)
			return null;
		else
			return tank.drain(maxDrain, doDrain);
	}

	@Override
	public int fill(Direction from, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return this.canDrain(from, resource.getFluid()) ? tank.drain(resource.amount, doDrain) : null;
	}

	@Override
	public boolean canFill(Direction from, Fluid fluid) {
		return false;
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return from.offsetY == 0 && ReikaFluidHelper.isFluidDrainableFromTank(fluid, tank);
	}

	@Override
	public FluidTankInfo[] getTankInfo(Direction from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}

	public boolean hasABucket() {
		return tank.getFluid() != null && tank.getFluid().amount >= /*FLUIDCONTAINER-PORT*/ FluidContainerRegistry.BUCKET_VOLUME;
	}

	public void subtractBucket() {
		tank.drain(/*FLUIDCONTAINER-PORT*/ FluidContainerRegistry.BUCKET_VOLUME, true);
	}

	public int getTankLevel() {
		return tank.getFluid() != null ? tank.getFluid().amount : 0;
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

		omega = NBT.getInteger("speed");
		torque = NBT.getInteger("trq");
		power = NBT.getLong("pwr");
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		tank.writeToNBT(NBT);

		NBT.setInteger("speed", omega);
		NBT.setInteger("trq", torque);
		NBT.setLong("pwr", power);
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p) && side.offsetY == 0;
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side.offsetY == 0 ? Flow.OUTPUT : Flow.NONE;
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
			return this.isValidWorld(world) && y < MAXY && ReikaBiomeHelper.isOcean(world.getBiomeGenForCoords(x, z)) && this.isOceanFloor(world, x, y, z);
		}

		private boolean isValidWorld(Level world) {
			return ReactorCraft.config.isDimensionValidForHeavyWater(world.provider.dimensionId);
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
						Block id = world.getBlock(x+a, dy, z+b);
						int meta = world.getBlockMetadata(x+a, dy, z+b);
						if ((id != Blocks.flowing_water && id != Blocks.water) || meta != 0) {
							return false;
						}
					}
				}
				if (i >= 1) {
					Block id = world.getBlock(x, dy, z);
					int meta = world.getBlockMetadata(x, dy, z);
					if ((id != Blocks.flowing_water && id != Blocks.water) || meta != 0) {
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
			Block b = world.getBlock(x, y-1, z);
			Block b2 = world.getBlock(x, y+1, z);
			return (b == Blocks.lava || b == Blocks.flowing_lava) && (b2 != Blocks.lava && b2 != Blocks.flowing_lava);
		}

		private int getSurfaceY(Level world, int x, int y, int z) {
			switch(world.provider.dimensionId) {
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
			return world.provider.dimensionId == -1 ? 10+rand.nextInt(21)+rand.nextInt(51) : 10+rand.nextInt(31);
		}

	}

	public Fluid getFluid() {
		return tank.getActualFluid();
	}

}
