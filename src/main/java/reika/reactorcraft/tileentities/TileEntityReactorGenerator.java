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

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.math.MovingAverage;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.dragonapi.modinteract.power.ReikaRFHelper;
import reika.dragonapi.modregistry.PowerTypes;
import reika.electricraft.api.WrappableWireSource;
import reika.electricraft.network.WireNetwork;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorSounds;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.powergen.TileEntityHiPTurbine;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;
import reika.rotarycraft.api.interfaces.EMPControl;
import reika.rotarycraft.api.interfaces.Screwdriverable;
import reika.rotarycraft.api.power.ShaftMerger;
import reika.rotarycraft.auxiliary.PowerSourceList;
import reika.rotarycraft.auxiliary.interfaces.PowerSourceTracker;

public class TileEntityReactorGenerator extends TileEntityReactorBase implements Screwdriverable, MultiBlockTile,
WrappableWireSource, PowerSourceTracker, EMPControl {

	public TileEntityReactorGenerator(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.GENERATOR.get(), pos, state);
	}

	private Direction facingDir;

	private long power;
	private int torquein;
	private int omegain;

	private int lasttorquein;
	private int lastomegain;

	private Modes mode = Modes.RF;

	private boolean hasMultiblock;

	private final MovingAverage torqueAvg = new MovingAverage(20);
	private double currentAverage;
	private double lastAverage;

	public boolean hasMultiBlock() {
		return hasMultiblock || DragonAPI.debugtest;
	}

	public void setHasMultiBlock(boolean has) {
		if (hasMultiblock && !has)
			this.testBreakageFailure();
		hasMultiblock = has;
	}

	private void testBreakageFailure() {
		if (omegain > 1024) {
			this.fail(level, this.getBlockPos());
		}
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if ((world.getGameTime() & 127) == 0)
			ReikaWorldHelper.causeAdjacentUpdates(world, pos);

		lastomegain = omegain;
		lasttorquein = torquein;

		if (hasMultiblock || DragonAPI.debugtest)
			this.getPower(world, pos);
		else {
			omegain = torquein = 0;
		}
		torqueAvg.addValue(torquein);
		lastAverage = currentAverage;
		currentAverage = torqueAvg.getAverage();
		if (Math.abs(currentAverage - lastAverage) <= Math.min(lastAverage, currentAverage) * 0.05) {
			currentAverage = lastAverage;
		}

		power = (long) omegain * (long) torquein;

		if (power > 0) {
			Direction write = this.getFacing().getOpposite();
			ReactorSounds rs = null;
			int len = 1;
			switch (mode) {
				case RF:
					// CoFH IEnergyReceiver is gone in 26.2 — push energy to the adjacent block's
					// NeoForge transfer-API EnergyHandler capability instead.
					EnergyHandler eh = world.getCapability(Capabilities.Energy.BLOCK, pos.relative(write), this.getFacing());
					if (eh != null) {
						try (Transaction tx = Transaction.openRoot()) {
							eh.insert((int) this.getGenUnits(), tx);
							tx.commit();
						}
					}
					rs = ReactorSounds.GENERATOR_RF;
					len = 129;
					break;
				case ELC: // handled by ElectriCraft wire-network logic
					rs = ReactorSounds.GENERATOR_ELC;
					len = 94;
					break;
			}
			if (rs != null && this.getTicksExisted() % len == 0) {
				rs.playSoundAtBlock(this, 2, 1);
				int l = getGeneratorLength();
				rs.playSoundAtBlock(world, this.getX() + this.getFacing().getStepX() * l, this.getY(), this.getZ() + this.getFacing().getStepZ() * l, 2, 1);
				rs.playSoundAtBlock(world, this.getX() + this.getFacing().getStepX() * l / 2, this.getY(), this.getZ() + this.getFacing().getStepZ() * l / 2, 2, 1);
			}
		}
	}

	private void fail(Level world, BlockPos pos) {
		int l = getGeneratorLength() / 2;
		world.removeBlock(pos, false);
		double dx = pos.getX() + 0.5 + this.getFacing().getStepX() * l;
		double dz = pos.getZ() + 0.5 + this.getFacing().getStepZ() * l;
		world.explode(null, dx, pos.getY() + 0.5, dz, 12F, Level.ExplosionInteraction.BLOCK);
	}

	public static int getGeneratorLength() {
		return 10;
	}

	private void getPower(Level world, BlockPos pos) {
		TileEntityTurbineCore te = this.getTurbine(world, pos);
		if (te != null && te.getSteamMovement() == this.getFacing().getOpposite()) {
			power = te.getPower();
			omegain = te.getOmega();
			torquein = te.getTorque();
		}
		else {
			omegain = torquein = 0;
			power = 0;
		}
	}

	private TileEntityTurbineCore getTurbine(Level world, BlockPos pos) {
		int len = getGeneratorLength();
		BlockPos tpos = pos.relative(this.getFacing(), len);
		ReactorTiles r = ReactorTiles.getTE(world, tpos);
		return r != null && r.isTurbine() ? (TileEntityTurbineCore) world.getBlockEntity(tpos) : null;
	}

	public Direction getFacing() {
		return facingDir != null ? facingDir : Direction.EAST;
	}

	public void setFacing(Direction dir) {
		facingDir = dir;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.GENERATOR;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		if (!this.isInWorld()) {
			phi = 0;
			return;
		}
		phi += 0.5 * ReikaMathLibrary.doubpow(ReikaMathLibrary.logbase(omegain + 1, 2), 1.05);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		facingDir = dirs[NBT.getIntOr("face", 0)];
		hasMultiblock = NBT.getBooleanOr("multi", false);

		power = NBT.getLongOr("pwr", 0L);

		if (NBT.contains("mode"))
			mode = Modes.list[NBT.getIntOr("mode", 0)];
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("face", this.getFacing().ordinal());
		NBT.putBoolean("multi", hasMultiblock);

		NBT.putLong("pwr", power);

		NBT.putInt("mode", mode.ordinal());
	}

	public double getGenUnits() {
		return power * this.getMode().ratio;
	}

	public String getGeneratedOutputForDisplay() {
		return this.getTEName() + " generating " + mode.getDisplay(this);
	}

	// 1.21.5: BlockEntity.getRenderBoundingBox was removed; kept as a helper for the renderer's bounds.
	public AABB getRenderBoundingBox() {
		int l = getGeneratorLength();
		int x1 = this.getX() + 1 + this.getFacing().getStepX() * l;
		int z1 = this.getZ() + 1 + this.getFacing().getStepZ() * l;
		int mx = Math.min(x1, this.getX());
		int mz = Math.min(z1, this.getZ());
		int mx2 = Math.max(x1, this.getX());
		int mz2 = Math.max(z1, this.getZ());
		return new AABB(mx, this.getY() - 2, mz, mx2, this.getY() + 3, mz2).inflate(6, 6, 6);
	}

	public enum Modes {
		RF("Redstone Flux", 1D / ReikaRFHelper.getWattsPerRF(), PowerTypes.RF),
		// IndustrialCraft EU output removed (IC2 not in the 26.2 build; PowerTypes.EU no longer exists).
		ELC("ElectriCraft", 1, PowerTypes.ELECTRICRAFT);

		public final String name;
		private final double ratio;
		public final PowerTypes type;

		private static final Modes[] list = values();

		Modes(String s, double r, PowerTypes p) {
			name = s;
			ratio = r;
			type = p;
		}

		public String getDisplay(TileEntityReactorGenerator te) {
			if (this == ELC) {
				return this.getELCDisplay(te);
			}
			return String.format("%.3f %s/t.", te.power * ratio, this.name());
		}

		private String getELCDisplay(TileEntityReactorGenerator te) {
			return te.getTorque() / WireNetwork.TORQUE_PER_AMP + "A @ " + te.getOmega() * WireNetwork.TORQUE_PER_AMP + "V";
		}

		public boolean exists() {
			return type.isLoaded();
		}
	}

	public Modes stepType() {
		int o = mode.ordinal();
		Modes m = Modes.list[o];
		do {
			if (o < Modes.list.length - 1) {
				o++;
			}
			else {
				o = 0;
			}
			m = Modes.list[o];
		} while (!m.exists());
		mode = m;
		return mode;
	}

	public Modes getMode() {
		return mode;
	}

	@Override
	public boolean onShiftRightClick(Level world, BlockPos pos, Direction side) {
		this.stepType();
		return true;
	}

	@Override
	public boolean onRightClick(Level world, BlockPos pos, Direction side) {
		if (side.getStepY() == 0) {
			this.setFacing(side);
			return true;
		}
		return false;
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
	public boolean canConnectToSide(Direction dir) {
		return dir == this.getFacing().getOpposite();
	}

	@Override
	public boolean isFunctional() {
		return hasMultiblock && this.getMode() == Modes.ELC;
	}

	@Override
	public int getOmega() {
		TileEntityTurbineCore te = this.getTurbine(level, this.getBlockPos());
		int lim = te instanceof TileEntityHiPTurbine ? TileEntityHiPTurbine.GEN_OMEGA : TileEntityTurbineCore.GEN_OMEGA;
		return Math.min(omegain, (int) (lim * 0.995));
	}

	@Override
	public int getTorque() {
		TileEntityTurbineCore te = this.getTurbine(level, this.getBlockPos());
		if (te == null)
			return 0;
		double max = te.getTorque();
		if (te instanceof TileEntityHiPTurbine) {

		}
		else {
			max = te.isAmmonia() ? TileEntityTurbineCore.TORQUE_CAP * 0.95 : TileEntityTurbineCore.TORQUE_CAP * 1.95;
		}
		return (int) Math.min(max, currentAverage);
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public int getIORenderAlpha() {
		return 0;
	}

	@Override
	public void setIORenderAlpha(int io) {

	}

	@Override
	public boolean hasPowerStatusChangedSinceLastTick() {
		return lastomegain != omegain || lasttorquein != torquein;
	}

	@Override
	public PowerSourceList getPowerSources(PowerSourceTracker io, ShaftMerger caller) {
		PowerSourceList p = new PowerSourceList();
		if (power > 0) {
			TileEntityTurbineCore te = this.getTurbine(level, this.getBlockPos());
			if (te != null) {
				p.addSource(te);
			}
		}
		return p;
	}

	@Override
	public void getAllOutputs(Collection<BlockEntity> c, Direction dir) {
		c.add(this.getAdjacentBlockEntity(this.getFacing().getOpposite()));
	}

	public Level getWorld() {
		return level;
	}

	public int getX() {
		return this.getBlockPos().getX();
	}

	public int getY() {
		return this.getBlockPos().getY();
	}

	public int getZ() {
		return this.getBlockPos().getZ();
	}

	@Override
	public BlockPos getIoOffsetPos() {
		return BlockPos.ZERO;
	}

	@Override
	public void onHitWithEMP(BlockEntity te) {
		this.fail(level, this.getBlockPos());
	}

}
