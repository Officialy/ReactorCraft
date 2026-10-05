/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.powergen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.base.BlockMultiBlock;
import reika.dragonapi.instantiable.data.Proportionality;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.instantiable.data.collections.RelativePositionList;
import reika.dragonapi.libraries.ReikaDirectionHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.registry.WorkingFluid;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.rotarycraft.blockentities.storage.BlockEntityReservoir;
import reika.rotarycraft.registry.ConfigRegistry;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityHiPTurbine extends TileEntityTurbineCore implements MultiBlockTile {

	public TileEntityHiPTurbine(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.BIGTURBINE.get(), pos, state);
	}

	public static final int GEN_OMEGA = 131072;
	public static final int FLUID_PER_RESERVOIR = TileEntityReactorBoiler.WATER_PER_STEAM * 131 / 20 / 24 * 6 / 10;

	private WorkingFluid fluid = WorkingFluid.EMPTY;
	private int dripBuffer;

	private RelativePositionList getInjectors() {
		RelativePositionList injectors = new RelativePositionList();
		Direction dir = this.getSteamMovement();
		if (dir.getStepX() == 0) {
			injectors.addPosition(new BlockPos(1, 1, 0));
			injectors.addPosition(new BlockPos(0, 1, 0));
			injectors.addPosition(new BlockPos(-1, 1, 0));

			injectors.addPosition(new BlockPos(1, 0, 0));

			injectors.addPosition(new BlockPos(-1, 0, 0));

			injectors.addPosition(new BlockPos(1, -1, 0));
			injectors.addPosition(new BlockPos(0, -1, 0));
			injectors.addPosition(new BlockPos(-1, -1, 0));
		}
		else if (dir.getStepZ() == 0) {
			injectors.addPosition(new BlockPos(0, 1, 1));
			injectors.addPosition(new BlockPos(0, 1, 0));
			injectors.addPosition(new BlockPos(0, 1, -1));

			injectors.addPosition(new BlockPos(0, 0, 1));

			injectors.addPosition(new BlockPos(0, 0, -1));

			injectors.addPosition(new BlockPos(0, -1, 1));
			injectors.addPosition(new BlockPos(0, -1, 0));
			injectors.addPosition(new BlockPos(0, -1, -1));
		}
		return injectors;
	}

	@Override
	public boolean needsMultiblock() {
		return !DragonAPI.debugtest;
	}

	@Override
	public void setHasMultiBlock(boolean has) {
		if (hasMultiBlock && !has)
			this.testBreakageFailure();
		super.setHasMultiBlock(has);
	}

	private void testBreakageFailure() {
		if (omega > 2048) {
			this.fail(level, this.getBlockPos());
		}
	}

	private void fail(Level world, BlockPos pos) {
		world.removeBlock(pos, false);
		world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, Level.ExplosionInteraction.BLOCK);
	}

	@Override
	protected boolean checkForMultiblock(Level world, BlockPos pos) {
		for (Direction dir : dirs) {
			if (dir != this.getSteamMovement() && dir.getOpposite() != this.getSteamMovement()) {
				BlockState bs = world.getBlockState(pos.relative(dir));
				if (bs.getBlock() != ReactorBlocks.TURBINEMULTI.get())
					return false;
				if (!bs.getValue(BlockMultiBlock.FORMED))
					return false;
			}
		}
		return true;
	}

	@Override
	public int getMaxTorque() {
		return fluid.efficiency > 1 ? 131072 : 65536;
	}

	@Override
	public int getMaxSpeed() {
		return 131072;
	}

	@Override
	protected int getMaxStage() {
		return 6;
	}

	@Override
	protected double getRadius() {
		return 1.5 + this.getStage() / 2.0;
	}

	@Override
	protected void copyDataFrom(TileEntityTurbineCore tile) {
		super.copyDataFrom(tile);
		TileEntityHiPTurbine te = (TileEntityHiPTurbine) tile;
		fluid = te.fluid;
		dripBuffer = te.dripBuffer;
		te.dripBuffer = 0;
	}

	@Override
	protected void dumpSteam(Level world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if (dripBuffer > 0 && this.dumpLiquid(world, pos)) {
			Direction s = this.getSteamMovement();
			Direction dir = ReikaDirectionHelper.getLeftBy90(s);
			int th = (int) (this.getRadius());
			if (!world.isClientSide()) {
				for (int d = 0; d <= 1; d++) {
					for (int dy = 2; dy < 5; dy++) {
						int ty = y - th - dy;
						for (int i = -th; i <= th; i++) {
							int tx = x + dir.getStepX() * i + s.getStepX() * d;
							int tz = z + dir.getStepZ() * i + s.getStepZ() * d;
							BlockPos tpos = new BlockPos(tx, ty, tz);
							MachineRegistry m = MachineRegistry.getMachine(world, tpos);
							if (fluid.getLowPressureFluid() == null)
								continue;
							FluidStack fs = new FluidStack(fluid.getLowPressureFluid(), FLUID_PER_RESERVOIR);
							if (m == MachineRegistry.RESERVOIR) {
								BlockEntity te = world.getBlockEntity(tpos);
								((BlockEntityReservoir) te).addLiquid(fs.getAmount(), fs.getFluid());
								dripBuffer -= fs.getAmount();
								break;
							}
							// MOD-PORT: BuildCraft tank output (BCMachineHandler) dropped — BuildCraft not in build.
							int py = ty + 1 + rand.nextInt(th * 2);
							if (ReikaMathLibrary.py3d(dir.getStepX() * i, py - y, dir.getStepZ() * i) < th)
								ReikaParticleHelper.DRIPWATER.spawnAroundBlock(world, new BlockPos(x + dir.getStepX() * i, py, z + dir.getStepZ() * i), 5);
						}
						if (dripBuffer <= 0)
							break;
					}
					if (dripBuffer <= 0)
						break;
				}
			}
			int n = ConfigRegistry.SPRINKLER.getValue() * 12;
			double ax = s.getStepX() > 0 ? 1.2 : -0.2;
			double az = s.getStepZ() > 0 ? 1.2 : -0.2;
			int d = -s.getStepX() + s.getStepZ();
			for (int i = 0; i < n; i++) {
				double px = x + (-th + rand.nextDouble() * th * 2 + d) * dir.getStepX();
				double pz = z + (-th + rand.nextDouble() * th * 2 + d) * dir.getStepZ();
				ReikaParticleHelper.RAIN.spawnAt(world, px + ax, y - th + 1 + rand.nextInt(th * 2), pz + az);
			}
		}
	}

	private boolean dumpLiquid(Level world, BlockPos pos) {
		// DRAGONAPI-PORT: AtmosphereHandler.isNoAtmo (Galacticraft vacuum check) gone — assume atmosphere.
		return this.getStage() == this.getNumberStagesTotal() - 1;
	}

	@Override
	protected double getEfficiency() {
		switch (this.getNumberStagesTotal()) {
			case 1:
				return 0.0125;
			case 2:
				return 0.025;
			case 3:
				return 0.075;
			case 4:
				return 0.125;
			case 5:
				return 0.25;
			case 6:
				return 0.5;
			case 7:
				return 1;
			default:
				return 0;
		}
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.BIGTURBINE;
	}

	@Override
	protected double getAnimationSpeed() {
		return 0.5F;
	}

	@Override
	protected void intakeLubricant(Level world, BlockPos pos) {
		BlockPos behind = pos.relative(this.getSteamMovement().getOpposite());

		if (this.getStage() == 0) {
			RelativePositionList li = this.getInjectors();
			BlockArray arr = li.getPositionsRelativeTo(behind);
			for (int i = 0; i < arr.getSize(); i++) {
				BlockPos c = arr.getNthBlock(i);
				BlockEntity tile = world.getBlockEntity(c);
				if (tile instanceof TileEntitySteamInjector te) {
                    int lube = te.getLubricant();
					int rem = Math.min(lube, tank.getRemainingSpace());
					if (rem > 0) {
						te.remove(rem);
						tank.addLiquid(rem, ReactorFluids.getLegacyFluid("rc lubricant"));
					}
				}
			}
		}
	}

	@Override
	protected boolean enabled(Level world, BlockPos pos) {
		if (!DragonAPI.debugtest && tank.isEmpty())
			return false;
		if (this.isRedstoned(world, pos))
			return false;
		return super.enabled(world, pos);
	}

	private boolean isRedstoned(Level world, BlockPos pos) {
		BlockPos behind = pos.relative(this.getSteamMovement().getOpposite());
		RelativePositionList li = this.getInjectors();
		BlockArray arr = li.getPositionsRelativeTo(behind);
		for (int i = 0; i < arr.getSize(); i++) {
			BlockPos c = arr.getNthBlock(i);
			if (world.hasNeighborSignal(c))
				return true;
		}
		return false;
	}

	@Override
	protected boolean intakeSteam(Level world, BlockPos pos) {
		Direction dir = this.getSteamMovement().getOpposite();
		BlockPos behind = pos.relative(dir);

		boolean flag = false;

		if (DragonAPI.debugtest) {
			steam = 5000;
			fluid = WorkingFluid.WATER;
			dripBuffer = 5000;
			return true;
		}

		ReactorTiles r = ReactorTiles.getTE(world, behind);
		if (r == ReactorTiles.STEAMLINE) {
			TileEntitySteamLine te = (TileEntitySteamLine) this.getAdjacentBlockEntity(dir);
			int s = te.getSteam();
			if (s > 8 && this.canTakeIn(te.getWorkingFluid())) {
				Proportionality<ReactorType> source = te.getSourceReactorType();
				s = source != null ? this.getEffectiveUsable(s, source) : 0;
				if (s > 0) {
					int rm = s / 8 + 1;
					if (steam < this.getMaxSteam()) {
						int rm2 = Math.min(rm, this.getMaxSteam() - steam);
						steam += rm2;
						fluid = te.getWorkingFluid();
						te.removeSteam(rm2);
						dripBuffer += rm2 * 1000;
					}
					flag = s > rm + 32 && steam >= this.getMaxSteam() / 15;
				}
			}
		}
		else if (r == this.getTile()) {
			TileEntityHiPTurbine te = (TileEntityHiPTurbine) this.getAdjacentBlockEntity(dir);
			fluid = te.fluid;
		}

		if (steam == 0) {
			fluid = WorkingFluid.EMPTY;
		}

		return flag;
	}

	private int getEffectiveUsable(int s, Proportionality<ReactorType> source) {
		float ret = 0;
		for (ReactorType r : source.getElements()) {
			if (r != null)
				ret += source.getFraction(r) * s * r.getHPTurbineMultiplier();
		}
		return (int) ret;
	}

	private int getMaxSteam() {
		return 3250;
	}

	@Override
	protected int getConsumedLubricant() {
		return 100;
	}

	private boolean canTakeIn(WorkingFluid f) {
		return fluid == WorkingFluid.EMPTY || f == fluid;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		fluid = WorkingFluid.getFromNBT(NBT);
		dripBuffer = NBT.getIntOr("dripb", 0);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		fluid.saveToNBT(NBT);
		NBT.putInt("dripb", dripBuffer);
	}

	@Override
	protected float getTorqueFactor() {
		float base = super.getTorqueFactor();
		if (steam < this.getMaxSteam() / 2) {
			float f = steam / (float) this.getMaxSteam();
			base *= ReikaMathLibrary.cosInterpolation(0, 1, f, 0, 1);
		}
		if (fluid.efficiency > 1) {
			base *= 1 + (fluid.efficiency - 1) * 0.25F;
		}
		return base;
	}

	@Override
	public void breakBlock() {
		if (!level.isClientSide()) {
			for (Direction dir : dirs) {
				BlockPos dpos = this.getBlockPos().relative(dir);
				Block b = level.getBlockState(dpos).getBlock();
				if (b instanceof BlockReCMultiBlock) {
					((BlockReCMultiBlock) b).breakMultiBlock(level, dpos.getX(), dpos.getY(), dpos.getZ());
				}
			}
		}
	}

	@Override
	protected int getLubricantCapacity() {
		return 128000;
	}

	@Override
	protected boolean canCollideCheck() {
		return false;
	}

}
