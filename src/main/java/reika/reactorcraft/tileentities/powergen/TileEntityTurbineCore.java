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

import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.interfaces.blockentity.BreakAction;
import reika.dragonapi.interfaces.blockentity.ToggleTile;
import reika.dragonapi.libraries.ReikaEntityHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.blocks.BlockSteam;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorSounds;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.interfaces.Screwdriverable;
import reika.rotarycraft.api.power.ShaftMerger;
import reika.rotarycraft.api.power.ShaftPowerReceiver;
import reika.rotarycraft.auxiliary.PowerSourceList;
import reika.rotarycraft.api.power.ShaftPowerEmitter;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.auxiliary.interfaces.PowerSourceTracker;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
import reika.rotarycraft.registry.DifficultyEffects;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityTurbineCore extends TileEntityReactorBase implements ShaftPowerEmitter, Screwdriverable, IFluidHandler, PipeConnector,
MultiBlockTile, BreakAction, ToggleTile, PowerSourceTracker {

	public TileEntityTurbineCore(BlockPos pos, BlockState state) {
		this(ReactorBlockEntities.TURBINECORE.get(), pos, state);
	}

	protected TileEntityTurbineCore(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	// Orientation comes from the block's FACING blockstate (the direction the steam flows toward).
	private static final Direction[] ORIENT = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN};

	protected int steam;

	protected int omega;
	private int iotick;

	private BlockPos readPos;
	private BlockPos writePos;

	public static final int GEN_OMEGA = 65536;
	public static final int TORQUE_CAP = 32768;

	private int forcedlube = 0;

	private boolean ammonia;

	protected final HybridTank tank = new HybridTank("turbine", this.getLubricantCapacity());

	private Interference inter = null;

	private BlockArray contact = new BlockArray();

	private int damage;

	private boolean enabled = true;

	protected boolean hasMultiBlock = !this.needsMultiblock();
	private boolean readyForMultiBlock = false;

	public final void markForMulti() {
		readyForMultiBlock = true;
	}

	protected int getLubricantCapacity() {
		return 64000;
	}

	public void setHasMultiBlock(boolean has) {
		hasMultiBlock = has ? readyForMultiBlock : has;
		readyForMultiBlock = false;
	}

	public final boolean hasMultiBlock() {
		return hasMultiBlock;
	}

	private StepTimer soundTimer = new StepTimer(41);

	private int stage;

	private final StepTimer lubeTimer = new StepTimer((int) (20 / DifficultyEffects.LUBEUSAGE.getChance()));

	public int getDamage() {
		return damage;
	}

	public boolean needsMultiblock() {
		return false;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.TURBINECORE;
	}

	public Direction getFacing() {
		return this.getBlockState().getValue(BlockReactorMachine.FACING);
	}

	private void updateIO() {
		Direction f = this.getFacing();
		writePos = this.getBlockPos().relative(f);
		readPos = this.getBlockPos().relative(f.getOpposite());
	}

	@Override
	public final void updateEntity(Level world, BlockPos pos) {
		this.updateIO();

		if (!hasMultiBlock) {
			omega = 0;
			phi = 0;
			steam = 0;
			return;
		}

		thermalTicker.update();
		soundTimer.update();

		stage = this.calcStage();
		this.intakeLubricant(world, pos);
		this.distributeLubricant(world, pos);
		this.readSurroundings(world, pos);
		this.followHead(world, pos);
		if (this.canCollideCheck())
			this.enviroTest(world, pos);

		readyForMultiBlock = false;

		if (steam > 0) {
			this.dumpSteam(world, pos);
			if (thermalTicker.checkCap()) {
				steam -= this.getConsumedSteam();
			}
		}

		if (omega == 0) {
			phi = 0;
			steam = 0;
		}
		else {
			if (stage == 0) {
				if (soundTimer.checkCap()) {
					ReactorSounds.TURBINE.playSoundAtBlock(world, pos.getX(), pos.getY(), pos.getZ(), 2F, 1F);
				}
			}
			lubeTimer.update();
			if (!tank.isEmpty() && !world.isClientSide() && lubeTimer.checkCap())
				tank.removeLiquid(this.getConsumedLubricant());
		}

		steam *= this.getDamageEfficiency();

		if (this.getGenPower() >= 1000000000L) {
			ReactorAchievements.GIGATURBINE.triggerAchievement(this.getPlacer());
		}

		BlockEntity tg = world.getBlockEntity(writePos);
		if (tg instanceof ShaftPowerReceiver) {
			ShaftPowerReceiver rec = (ShaftPowerReceiver) tg;
			rec.setOmega(this.getOmega());
			rec.setTorque(this.getTorque());
			rec.setPower(this.getPower());
		}
	}

	protected boolean checkForMultiblock(Level world, BlockPos pos) {
		return false;
	}

	protected int getConsumedLubricant() {
		return 20;
	}

	private void distributeLubricant(Level world, BlockPos pos) {
		BlockPos behind = pos.relative(this.getSteamMovement().getOpposite());
		ReactorTiles r = ReactorTiles.getTE(world, behind);
		if (r == this.getTile()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore) world.getBlockEntity(behind);
			int max = Math.min(tank.getRemainingSpace(), 1000);
			int dl = te.tank.getFluidLevel() - tank.getFluidLevel();
			if (dl > 1) {
				int rem = Math.min(dl / 2, max);
				tank.addLiquid(rem, ReactorFluids.getLegacyFluid("rc lubricant"));
				te.tank.removeLiquid(rem);
			}
		}
	}

	protected void intakeLubricant(Level world, BlockPos pos) {

	}

	protected void dumpSteam(Level world, BlockPos pos) {

	}

	protected int getMaxStage() {
		return 4;
	}

	protected final int getConsumedSteam() {
		return steam / 32 + 1;
	}

	public Direction getSteamMovement() {
		return this.getFacing();
	}

	public int getMaxTorque() {
		return 32768;
	}

	public int getMaxSpeed() {
		return 65536;
	}

	private void updateSpeed(boolean up) {
		if (!DragonAPI.debugtest) {
			if (tank.isEmpty())
				up = false;
		}
		if (up) {
			int max = this.getMaxSpeed();
			if (omega < max) {
				omega += 4 * ReikaMathLibrary.logbase(max + 1, 2);
				if (omega > max)
					omega = max;
			}
		}
		else {
			if (omega > 0) {
				omega -= omega / 256 + 1;
			}
		}
	}

	public final boolean isAtEndOFLine() {
		if (ReactorTiles.getTE(level, readPos) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore) level.getBlockEntity(readPos);
			if (this.getBlockPos().equals(tile.writePos)) {
				return false;
			}
		}
		return true;
	}

	private int getAccelDelay() {
		return 1 + (int) ReikaMathLibrary.logbase(omega + 1, 2) / 20;
	}

	protected final int getGenTorque() {
		int torque = steam > 0 ? (int) (steam * 24 * this.getTorqueFactor()) : omega / 16 + 1;
		int ret = omega > 0 ? (int) (torque * this.getEfficiency()) : 0;
		return Math.min(ret, this.getMaxTorque());
	}

	protected float getTorqueFactor() {
		return 1;
	}

	private float getDamageEfficiency() {
		return damage > 0 ? 1F / (damage + 1) : 1;
	}

	protected final long getGenPower() {
		return (long) this.getGenTorque() * (long) omega;
	}

	protected double getEfficiency() {
		switch (this.getNumberStagesTotal()) {
			case 1:
				return 0.025;
			case 2:
				return 0.1;
			case 3:
				return 0.25;
			case 4:
				return 0.5;
			case 5:
				return 1;
			default:
				return 0;
		}
	}

	public final int getStage() {
		return stage;
	}

	private final int calcStage() {
		if (ReactorTiles.getTE(level, readPos) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore) level.getBlockEntity(readPos);
			if (this.getBlockPos().equals(tile.writePos)) {
				int stage = tile.calcStage();
				if (stage == this.getMaxStage())
					return this.getMaxStage();
				else
					return stage + 1;
			}
		}
		return 0;
	}

	protected AABB getBoundingBox(Level world, BlockPos pos) {
		AABB box = new AABB(pos);
		int r = 2 + stage;
		if (this.getFacing().getAxis() == Direction.Axis.Z)
			box = box.inflate(r / 2.0, r / 2.0, 0);
		else if (this.getFacing().getAxis() == Direction.Axis.X)
			box = box.inflate(0, r / 2.0, r / 2.0);
		return box;
	}

	/** Return true if turbine is to accelerate */
	protected boolean intakeSteam(Level world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		boolean canAccel = false;
		// Drive off un-moved, turbine-capable steam directly below; ammonia steam gives 2x.
		if (below.is(ReactorBlocks.STEAM.get()) && stage == 0
				&& below.getValue(BlockSteam.POWERED) && !below.getValue(BlockSteam.MOVED)) {
			if (below.getValue(BlockSteam.AMMONIA)) {
				steam += 2;
				ammonia = true;
			}
			else {
				steam++;
				ammonia = false;
			}
			canAccel = true;
		}
		return canAccel;
	}

	public boolean isAmmonia() {
		return ammonia;
	}

	private void readSurroundings(Level world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if (this.canCollideCheck()) {
			contact.clear();
			if (contact.isEmpty()) {
				this.fillSurroundings(world, pos);
			}
			inter = null;
			for (int i = 0; i < contact.getSize(); i++) {
				BlockPos c = contact.getNthBlock(i);
				if (ReikaMathLibrary.py3d(x - c.getX(), y - c.getY(), z - c.getZ()) <= this.getRadius()) {
					BlockState bs = world.getBlockState(c);
					Block id2 = bs.getBlock();
					if (!ReikaWorldHelper.softBlocks(world, c) && !c.equals(pos) && id2 != ReactorBlocks.TURBINEMULTI.get()) {
						phi = 0;
						omega = 0;
						if (inter == null || inter.maxSpeed > Interference.JAM.maxSpeed)
							inter = Interference.JAM;
					}
					else if (!bs.getFluidState().isEmpty()) {
						if (inter == null || inter.maxSpeed > Interference.FLUID.maxSpeed)
							inter = Interference.FLUID;
					}
				}
			}
		}
		if (this.getStage() == 0) {
			boolean accel = this.enabled(world, pos) && this.intakeSteam(world, pos);
			if (!world.isClientSide())
				this.updateSpeed(accel);
		}
	}

	protected boolean canCollideCheck() {
		return true;
	}

	protected boolean enabled(Level world, BlockPos pos) {
		return enabled;
	}

	protected double getRadius() {
		return 1.5 + stage / 2.0;
	}

	private void fillSurroundings(Level world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		int r = 3;
		if (this.getFacing().getAxis() == Direction.Axis.Z) {
			for (int i = x - r; i <= x + r; i++) {
				for (int j = y - r; j <= y + r; j++) {
					if (x != i || y != j)
						contact.addBlockCoordinate(i, j, z);
				}
			}
		}
		else {
			for (int i = z - r; i <= z + r; i++) {
				for (int j = y - r; j <= y + r; j++) {
					if (z != i || y != j)
						contact.addBlockCoordinate(x, j, i);
				}
			}
		}
	}

	private void enviroTest(Level world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		AABB box = this.getBoundingBox(world, pos);
		int r = 2 + stage / 2;
		List<LivingEntity> li = world.getEntitiesOfClass(LivingEntity.class, box);
		for (LivingEntity e : li) {
			if (this.getOmega() > 0 && ReikaMathLibrary.py3d(e.getX() - x - 0.5, e.getY() - y - 0.5, e.getZ() - z - 0.5) < r) {
				if (this.canDamageTurbine(e)) {
					if (world instanceof ServerLevel sl) {
						sl.explode(null, e.getX(), e.getY() + e.getEyeHeight(), e.getZ(), 2, Level.ExplosionInteraction.BLOCK);
						e.hurtServer(sl, sl.damageSources().generic(), 2);
						this.breakTurbine();
					}
					Vec3 v = e.getDeltaMovement().add(
							0.4 * (e.getX() - x - 0.5 + 0.1) + rand.nextDouble() * 0.1,
							0.4 * (e.getY() - y - 0.5 + 0.1),
							0.4 * (e.getZ() - z - 0.5 + 0.1) + rand.nextDouble() * 0.1);
					e.setDeltaMovement(v);
					e.hurtMarked = true;
					if (inter == null || inter.maxSpeed > Interference.MOB.maxSpeed)
						inter = Interference.MOB;
				}
			}
		}

		if (inter != null) {
			omega = Math.min(omega, inter.maxSpeed);
		}
	}

	public static boolean canDamageTurbine(Entity e) {
		if (e instanceof Player) {
			return !((Player) e).isCreative();
		}
		return ReikaEntityHelper.isSolidEntity(e);
	}

	protected void breakTurbine() {
		damage++;
	}

	public final int getNumberStagesTotal() {
		if (this.needsMultiblock() && !this.hasMultiBlock())
			return 0;
		if (ReactorTiles.getTE(level, writePos) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore) level.getBlockEntity(writePos);
			if (this.getBlockPos().equals(tile.readPos)) {
				if (tile.hasMultiBlock() || !tile.needsMultiblock())
					return tile.getNumberStagesTotal();
			}
		}
		return this.calcStage() + 1;
	}

	private void followHead(Level world, BlockPos pos) {
		if (ReactorTiles.getTE(level, readPos) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore) world.getBlockEntity(readPos);
			if (pos.equals(tile.writePos)) {
				this.copyDataFrom(tile);
			}
		}
		if (ReactorTiles.getTE(level, writePos) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore) level.getBlockEntity(writePos);
			if (pos.equals(tile.readPos)) {
				if (tile.inter != null)
					inter = tile.inter;
			}
		}
	}

	protected void copyDataFrom(TileEntityTurbineCore tile) {
		omega = tile.omega;
		phi = tile.phi;
		steam = tile.steam;
		ammonia = tile.ammonia;
	}

	@Override
	protected final void animateWithTick(Level world, BlockPos pos) {
		iotick -= 8;
		if (!this.isInWorld()) {
			phi = 0;
			return;
		}
		phi += this.getAnimationSpeed() * ReikaMathLibrary.doubpow(ReikaMathLibrary.logbase(omega + 1, 2), 1.05);
	}

	protected double getAnimationSpeed() {
		return 0.2F;
	}

	@Override
	public final int getOmega() {
		return this.isEmitting() ? omega : 0;
	}

	@Override
	public final int getTorque() {
		return this.getGenTorque();
	}

	@Override
	public final long getPower() {
		return this.getGenPower();
	}

	@Override
	public final int getIORenderAlpha() {
		return iotick;
	}

	@Override
	public final void setIORenderAlpha(int io) {
		iotick = io;
	}

	@Override
	public final boolean canWriteTo(Direction from) {
		return this.getBlockPos().relative(from).equals(writePos);
	}

	@Override
	public final boolean isEmitting() {
		return this.getGenPower() > 0;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		omega = NBT.getIntOr("speed", 0);
		steam = NBT.getIntOr("steamlevel", 0);

		inter = Interference.get(NBT.getIntOr("blocked", 0));

		damage = NBT.getIntOr("dmg", 0);
		ammonia = NBT.getBooleanOr("ammonia", false);

		if (this.needsMultiblock() && NBT.contains("multi"))
			hasMultiBlock = NBT.getBooleanOr("multi", false);

		tank.readFromNBT(NBT);

		stage = NBT.getIntOr("stage", 0);

		if (NBT.contains("t_enable"))
			enabled = NBT.getBooleanOr("t_enable", false);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("speed", omega);
		NBT.putInt("steamlevel", steam);

		NBT.putInt("dmg", damage);
		NBT.putBoolean("ammonia", ammonia);

		if (inter != null)
			NBT.putInt("blocked", inter.ordinal());
		else
			NBT.putInt("blocked", -1);

		if (this.needsMultiblock())
			NBT.putBoolean("multi", hasMultiBlock);

		tank.writeToNBT(NBT);

		NBT.putInt("stage", stage);

		NBT.putBoolean("t_enable", enabled);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(this.getBlockPos()).inflate(6, 6, 6);
	}

	private static enum Interference {
		JAM(0),
		FLUID(512),
		MOB(4096);

		public final int maxSpeed;

		public static final Interference[] list = values();

		private Interference(int max) {
			maxSpeed = max;
		}

		public static Interference get(int o) {
			if (o < 0)
				return null;
			return list[o];
		}
	}

	@Override
	public final long getMaxPower() {
		return this.getGenPower();
	}

	@Override
	public final long getCurrentPower() {
		return this.getGenPower();
	}

	@Override
	public final BlockPos getEmittingPos(BlockPos pos) {
		return writePos;
	}

	@Override
	public final boolean onShiftRightClick(Level world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public final boolean onRightClick(Level world, BlockPos pos, Direction side) {
		Direction cur = this.getFacing();
		int idx = 0;
		for (int i = 0; i < ORIENT.length; i++) {
			if (ORIENT[i] == cur) {
				idx = i;
				break;
			}
		}
		int max = this.canOrientVertically() ? 5 : 3;
		idx = idx < max ? idx + 1 : 0;
		world.setBlock(this.getBlockPos(), this.getBlockState().setValue(BlockReactorMachine.FACING, ORIENT[idx]), 3);
		return true;
	}

	protected boolean canOrientVertically() {
		return false;
	}

	@Override
	public final boolean canConnectToPipe(MachineRegistry m) {
		return m == MachineRegistry.HOSE || m == MachineRegistry.BEDPIPE;
	}

	@Override
	public final boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p);
	}

	@Override
	public final BlockEntityPiping.Flow getFlowForSide(Direction side) {
		return side == this.getSteamMovement().getOpposite() ? BlockEntityPiping.Flow.INPUT : BlockEntityPiping.Flow.NONE;
	}

	// --- NeoForge IFluidHandler (lubricant tank) ---
	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int t) {
		return tank.getFluid();
	}

	@Override
	public int getTankCapacity(int t) {
		return tank.getCapacity();
	}

	@Override
	public boolean isFluidValid(int t, FluidStack stack) {
		return stack.getFluid().equals(ReactorFluids.getLegacyFluid("rc lubricant"));
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !this.isFluidValid(0, resource))
			return 0;
		return tank.fill(resource, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return FluidStack.EMPTY;
	}

	@Override
	public int fillPipe(Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
		return from == this.getSteamMovement().getOpposite() ? this.fill(resource, action) : 0;
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction doDrain) {
		return FluidStack.EMPTY;
	}

	public final int getLubricant() {
		return tank.getFluidLevel();
	}

	public int getLubricantToDrop() {
		return Math.abs(forcedlube - tank.getFluidLevel()) < 25 ? forcedlube : tank.getFluidLevel();
	}

	@Override
	public void breakBlock() {
		Direction dir = this.getSteamMovement();
		BlockEntity te1 = this.getAdjacentBlockEntity(dir);
		if (te1 instanceof TileEntityTurbineCore tc)
			tc.forcedlube = this.getLubricantToDrop();
		BlockEntity te2 = this.getAdjacentBlockEntity(dir.getOpposite());
		if (te2 instanceof TileEntityTurbineCore tc)
			tc.forcedlube = this.getLubricantToDrop();
	}

	public final void addLubricant(int amt) {
		tank.addLiquid(amt, ReactorFluids.getLegacyFluid("rc lubricant"));
	}

	public final boolean canAcceptLubricant(int amt) {
		return tank.canTakeIn(amt);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enable) {
		enabled = enable;
		this.syncAllData(false);
	}

	@Override
	public PowerSourceList getPowerSources(PowerSourceTracker io, ShaftMerger caller) {
		PowerSourceList p = new PowerSourceList();
		if (omega > 0) {
			p.addSource(this);
		}
		return p;
	}

	@Override
	public void getAllOutputs(Collection<BlockEntity> c, Direction dir) {
		c.add(this.getAdjacentBlockEntity(this.getSteamMovement()));
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

	public void repairCC(int tier) {
		if (damage > 0 && rand.nextFloat() < tier * 0.1F)
			damage--;
	}

}
