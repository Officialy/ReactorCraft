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

import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import reika.dragonapi.DragonAPICore;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.instantiable.data.immutable.Coordinate;
import reika.dragonapi.interfaces.tileentity.BreakAction;
import reika.dragonapi.interfaces.tileentity.ToggleTile;
import reika.dragonapi.libraries.ReikaEntityHelper;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.dragonapi.libraries.world.ReikaWorldHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorSounds;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.interfaces.Screwdriverable;
import reika.rotarycraft.api.power.ShaftMerger;
import reika.rotarycraft.api.power.ShaftPowerReceiver;
import reika.rotarycraft.auxiliary.OldTextureLoader;
import reika.rotarycraft.auxiliary.PowerSourceList;
import reika.rotarycraft.auxiliary.ShaftPowerEmitter;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.auxiliary.interfaces.PowerSourceTracker;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.DifficultyEffects;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityTurbineCore extends TileEntityReactorBase implements ShaftPowerEmitter, Screwdriverable, IFluidHandler, PipeConnector,
MultiBlockTile, BreakAction, ToggleTile, PowerSourceTracker {

	protected int steam;

	protected int omega;
	private int iotick;

	private int readx;
	private int ready;
	private int readz;
	private int writex;
	private int writey;
	private int writez;

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

	private final StepTimer lubeTimer = new StepTimer((int)(20/DifficultyEffects.LUBEUSAGE.getChance()));

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

	@Override
	public final void updateEntity(Level world, int x, int y, int z, int meta) {
		this.getIOSides(world, x, y, z, meta);

		if (!hasMultiBlock) {
			//boolean checkMulti = this.getTicksExisted() < 5 || world.getTotalWorldTime()%32 == 0;
			//if (!checkMulti && !this.checkForMultiblock(world, x, y, z, meta)) {
			omega = 0;
			phi = 0;
			steam = 0;
			return;
			//}
		}

		if (ReactorCraft.logger.shouldDebug()) {
			if (world.isRemote)
				ReactorCraft.logger.log("Clientside "+this+" has "+steam+" steam, spinning @ "+omega+" rad/s. Phi="+phi);
			else
				ReactorCraft.logger.log("Serverside "+this+" has "+steam+" steam, spinning @ "+omega+" rad/s.");
		}

		thermalTicker.update();
		soundTimer.update();

		stage = this.calcStage();
		this.intakeLubricant(world, x, y, z, meta);
		this.distributeLubricant(world, x, y, z, meta);
		this.readSurroundings(world, x, y, z, meta);
		this.followHead(world, x, y, z, meta);
		if (this.canCollideCheck())
			this.enviroTest(world, x, y, z, meta);

		readyForMultiBlock = false;

		//ReikaJavaLibrary.pConsole(steam, stage == 6 && this.getSide() == Side.SERVER);
		if (steam > 0) {
			this.dumpSteam(world, x, y, z, meta);
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
				if (OldTextureLoader.instance.loadOldTextures()) {
					if (this.getTicksExisted()%4 == 0)
						ReikaSoundHelper.playSoundFromServerAtBlock(world, x, y, z, "mob.villager.idle", 1, 1, true);
				}
				else if (soundTimer.checkCap()) {
					ReactorSounds.TURBINE.playSoundAtBlock(world, x, y, z, 2F, 1F);
				}
			}
			lubeTimer.update();
			if (!tank.isEmpty() && !world.isRemote && lubeTimer.checkCap())
				tank.removeLiquid(this.getConsumedLubricant());
		}

		steam *= this.getDamageEfficiency();

		if (this.getGenPower() >= 1000000000L) {
			ReactorAchievements.GIGATURBINE.triggerAchievement(this.getPlacer());
		}

		BlockEntity tg = this.getTileEntity(writex, writey, writez);
		if (tg instanceof ShaftPowerReceiver) {
			ShaftPowerReceiver rec = (ShaftPowerReceiver)tg;
			rec.setOmega(this.getOmega());
			rec.setTorque(this.getTorque());
			rec.setPower(this.getPower());
		}
	}

	protected boolean checkForMultiblock(Level world, int x, int y, int z, int meta) {
		return false;
	}

	protected int getConsumedLubricant() {
		return 20;
	}

	private void distributeLubricant(Level world, int x, int y, int z, int meta) {
		ForgeDirection dir = this.getSteamMovement().getOpposite();
		int dx = x+dir.offsetX;
		int dy = y+dir.offsetY;
		int dz = z+dir.offsetZ;
		ReactorTiles r = ReactorTiles.getTE(world, dx, dy, dz);
		if (r == this.getTile()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore)world.getTileEntity(dx, dy, dz);
			int max = Math.min(tank.getRemainingSpace(), 1000);
			int dl = te.tank.getLevel()-tank.getLevel();
			if (dl > 1) {
				int rem = Math.min(dl/2, max);
				tank.addLiquid(rem, FluidRegistry.getFluid("rc lubricant"));
				te.tank.removeLiquid(rem);
			}
		}
	}

	protected void intakeLubricant(Level world, int x, int y, int z, int meta) {

	}

	protected void dumpSteam(Level world, int x, int y, int z, int meta) {

	}

	protected int getMaxStage() {
		return 4;
	}

	protected final int getConsumedSteam() {
		return steam/32+1;
	}

	public ForgeDirection getSteamMovement() {
		switch(this.getBlockMetadata()) {
			case 0:
				return ForgeDirection.WEST;
			case 1:
				return ForgeDirection.EAST;
			case 2:
				return ForgeDirection.NORTH;
			case 3:
				return ForgeDirection.SOUTH;
			default:
				return ForgeDirection.DOWN;
		}
	}

	private void getIOSides(Level world, int x, int y, int z, int meta) {
		switch(meta) {
			case 0:
				readx = x+1;
				ready = y;
				readz = z;
				writex = x-1;
				writey = y;
				writez = z;
				break;
			case 1:
				readx = x-1;
				ready = y;
				readz = z;
				writex = x+1;
				writey = y;
				writez = z;
				break;
			case 2:
				readx = x;
				ready = y;
				readz = z+1;
				writex = x;
				writey = y;
				writez = z-1;
				break;
			case 3:
				readx = x;
				ready = y;
				readz = z-1;
				writex = x;
				writey = y;
				writez = z+1;
				break;
			case 4:
				readx = x;
				ready = y-1;
				readz = z;
				writex = x;
				writey = y+1;
				writez = z;
				break;
			case 5:
				readx = x;
				ready = y+1;
				readz = z;
				writex = x;
				writey = y-1;
				writez = z;
				break;
		}
	}

	public int getMaxTorque() {
		return 32768;
	}

	public int getMaxSpeed() {
		return 65536;
	}

	private void updateSpeed(boolean up) {
		if (!DragonAPICore.debugtest) {
			if (tank.isEmpty())
				up = false;
		}
		if (up) {
			int max = this.getMaxSpeed();
			if (omega < max) {
				omega += 4*ReikaMathLibrary.logbase(max+1, 2);
				if (omega > max)
					omega = max;
			}
		}
		else {
			if (omega > 0) {
				omega -= omega/256+1;
			}
		}
	}

	public final boolean isAtEndOFLine() {
		if (ReactorTiles.getTE(worldObj, readx, ready, readz) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore)worldObj.getTileEntity(readx, ready, readz);
			if (tile.writex == xCoord && tile.writey == yCoord && tile.writez == zCoord) {
				return false;
			}
		}
		return true;
	}

	private int getAccelDelay() {
		return 1+(int)ReikaMathLibrary.logbase(omega+1, 2)/20;
	}

	protected final int getGenTorque() {
		int torque = steam > 0 ? (int)(steam*24*this.getTorqueFactor()) : omega/16+1;
		int ret = omega > 0 ? (int)(torque*this.getEfficiency()) : 0;
		return Math.min(ret, this.getMaxTorque());
	}

	protected float getTorqueFactor() {
		return 1;
	}

	private float getDamageEfficiency() {
		return damage > 0 ? 1F/(damage+1) : 1;
	}

	protected final long getGenPower() {
		return (long)this.getGenTorque()*(long)omega;
	}

	protected double getEfficiency() {
		switch(this.getNumberStagesTotal()) {
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
		if (ReactorTiles.getTE(worldObj, readx, ready, readz) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore)worldObj.getTileEntity(readx, ready, readz);
			if (tile.writex == xCoord && tile.writey == yCoord && tile.writez == zCoord) {
				int stage = tile.calcStage();
				if (stage == this.getMaxStage())
					return this.getMaxStage();
				else
					return stage+1;
			}
		}
		return 0;
	}

	protected AABB getBoundingBox(Level world, int x, int y, int z, int meta) {
		AABB box = AABB.getBoundingBox(x, y, z, x+1, y+1, z+1);
		int r = 2+stage;
		switch(meta) {
			case 2:
			case 3:
				box = box.expand(r/2, r/2, 0);
				break;
			case 0:
			case 1:
				box = box.expand(0, r/2, r/2);
				break;
		}
		return box;
	}

	/** Return true if turbine is to accelerate */
	protected boolean intakeSteam(Level world, int x, int y, int z, int meta) {
		Block id = world.getBlock(x, y-1, z);
		int meta2 = world.getBlockMetadata(x, y-1, z);
		boolean canAccel = false;
		if (id == ReactorBlocks.STEAM.getBlockInstance() && stage == 0) {
			if ((meta2&2) != 0 && (meta2&8) == 0) {
				int newmeta = 1+(meta2&4);
				if ((meta2&4) != 0) {
					steam += 2;
					ammonia = true;
				}
				else {
					steam++;
					ammonia = false;
				}
				canAccel = true;
			}
		}
		if (canAccel && world.isRemote && world.getClosestPlayer(x+0.5, y+0.5, z+0.5, 64) != null) {
			ForgeDirection dir = this.getSteamMovement();
			for (int i = 0; i < this.getNumberStagesTotal(); i++) {
				BlockEntity te = world.getTileEntity(x+dir.offsetX, y, z+dir.offsetZ);
				if (!(te instanceof TileEntityTurbineCore))
					break;
				double r = ((TileEntityTurbineCore)te).getRadius()*2.4-2;
				for (int n = 0; n < 2; n++) {
					double v = ReikaRandomHelper.getRandomBetween(0.03125, 0.25);
					double dx = x+0.5+dir.offsetX*i;
					double dy = y+0.5;
					double dz = z+0.5+dir.offsetZ*i;
					dy = ReikaRandomHelper.getRandomPlusMinus(dy, r);
					if (dir.offsetX != 0) {
						dz = ReikaRandomHelper.getRandomPlusMinus(dz, r);
					}
					else if (dir.offsetZ != 0) {
						dx = ReikaRandomHelper.getRandomPlusMinus(dx, r);
					}
					ReikaParticleHelper.CLOUD.spawnAt(world, dx, dy, dz, dir.offsetX*v, 0, dir.offsetZ*v);
				}
			}
		}
		return canAccel;
	}

	public boolean isAmmonia() {
		return ammonia;
	}

	private void readSurroundings(Level world, int x, int y, int z, int meta) {
		if (this.canCollideCheck()) {
			contact.clear();
			if (contact.isEmpty()) {
				this.fillSurroundings(world, x, y, z, meta);
			}
			inter = null;
			for (int i = 0; i < contact.getSize(); i++) {
				Coordinate c = contact.getNthBlock(i);
				if (ReikaMathLibrary.py3d(x-c.xCoord, y-c.yCoord, z-c.zCoord) <= this.getRadius()) {
					Block id2 = c.getBlock(world);
					int meta2 = c.getBlockMetadata(world);
					if (!ReikaWorldHelper.softBlocks(world, c.xCoord, c.yCoord, c.zCoord) && !c.equals(x, y, z) && id2 != ReactorBlocks.TURBINEMULTI.getBlockInstance()) {
						phi = 0;
						omega = 0;
						if (inter == null || inter.maxSpeed > Interference.JAM.maxSpeed)
							inter = Interference.JAM;
					}
					else if (id2 instanceof BlockLiquid || id2 instanceof BlockFluidBase) {
						if (inter == null || inter.maxSpeed > Interference.FLUID.maxSpeed)
							inter = Interference.FLUID;
					}
				}
			}
		}
		if (this.getStage() == 0) {
			boolean accel = this.enabled(world, x, y, z) && this.intakeSteam(world, x, y, z, meta);
			if (!world.isRemote)
				this.updateSpeed(accel);
		}
	}

	protected boolean canCollideCheck() {
		return true;
	}

	protected boolean enabled(Level world, int x, int y, int z) {
		return enabled;
	}

	protected double getRadius() {
		return 1.5+stage/2;
	}

	private void fillSurroundings(Level world, int x, int y, int z, int meta) {
		AABB box = AABB.getBoundingBox(x, y, z, x+1, y+1, z+1);
		int r = 3;
		switch(meta) {
			case 2:
			case 3:
				for (int i = x-r; i <= x+r; i++) {
					for (int j = y-r; j <= y+r; j++) {
						if (x != i || y != j)
							contact.addBlockCoordinate(i, j, z);
					}
				}
				break;
			case 0:
			case 1:
				for (int i = z-r; i <= z+r; i++) {
					for (int j = y-r; j <= y+r; j++) {
						if (z != i || y != j)
							contact.addBlockCoordinate(x, j, i);
					}
				}
				break;
		}

	}

	private void enviroTest(Level world, int x, int y, int z, int meta) {
		AABB box = this.getBoundingBox(world, x, y, z, meta);
		int r = 2+stage/2;
		List<LivingEntity> li = world.getEntitiesWithinAABB(LivingEntity.class, box);
		for (LivingEntity e : li) {
			if (this.getOmega() > 0 && ReikaMathLibrary.py3d(e.posX-x-0.5, e.posY-y-0.5, e.posZ-z-0.5) < r) {
				if (this.canDamageTurbine(e)) {
					if (!world.isRemote) {
						Explosion exp = world.createExplosion(null, e.posX, e.posY+e.getEyeHeight()/1F, e.posZ, 2, false);
						e.attackEntityFrom(DamageSource.setExplosionSource(exp), 2);
						this.breakTurbine();
					}
					e.motionX += 0.4*(e.posX-x-0.5+0.1)+rand.nextDouble()*0.1;
					e.motionY += 0.4*(e.posY-y-0.5+0.1);
					e.motionZ += 0.4*(e.posZ-z-0.5+0.1)+rand.nextDouble()*0.1;
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
			return !((Player)e).capabilities.isCreativeMode;
		}
		return ReikaEntityHelper.isSolidEntity(e);
	}

	protected void breakTurbine() {
		damage++;
	}

	public final int getNumberStagesTotal() {
		if (this.needsMultiblock() && !this.hasMultiBlock())
			return 0;
		if (ReactorTiles.getTE(worldObj, writex, writey, writez) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore)worldObj.getTileEntity(writex, writey, writez);
			if (tile.readx == xCoord && tile.ready == yCoord && tile.readz == zCoord) {
				if (tile.hasMultiBlock() || !tile.needsMultiblock())
					return tile.getNumberStagesTotal();
			}
		}
		return this.calcStage()+1;
	}

	private void followHead(Level world, int x, int y, int z, int meta) {
		if (ReactorTiles.getTE(worldObj, readx, ready, readz) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore)world.getTileEntity(readx, ready, readz);
			if (tile.writex == x && tile.writey == y && tile.writez == z) {
				this.copyDataFrom(tile);
			}
		}
		if (ReactorTiles.getTE(worldObj, writex, writey, writez) == this.getTile()) {
			TileEntityTurbineCore tile = (TileEntityTurbineCore)worldObj.getTileEntity(writex, writey, writez); //write!
			if (tile.readx == x && tile.ready == y && tile.readz == z) {
				if (tile.inter != null)
					inter = tile.inter;
			}
		}
	}

	protected void copyDataFrom(TileEntityTurbineCore tile) {
		//omega = (omega+tile.omega)/2;
		omega = tile.omega;
		phi = tile.phi;
		steam = tile.steam;
		ammonia = tile.ammonia;
	}

	@Override
	protected final void animateWithTick(Level world, int x, int y, int z) {
		iotick -= 8;
		if (!this.isInWorld()) {
			phi = 0;
			return;
		}
		phi += this.getAnimationSpeed()*ReikaMathLibrary.doubpow(ReikaMathLibrary.logbase(omega+1, 2), 1.05);
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
	public final boolean canWriteTo(ForgeDirection from) {
		return xCoord+from.offsetX == writex && yCoord+from.offsetY == writey && zCoord+from.offsetZ == writez;
	}

	@Override
	public final boolean isEmitting() {
		return this.getGenPower() > 0;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		omega = NBT.getInteger("speed");
		steam = NBT.getInteger("steamlevel");

		inter = Interference.get(NBT.getInteger("blocked"));

		damage = NBT.getInteger("dmg");
		ammonia = NBT.getBoolean("ammonia");

		if (this.needsMultiblock() && NBT.hasKey("multi"))
			hasMultiBlock = NBT.getBoolean("multi");

		tank.readFromNBT(NBT);

		stage = NBT.getInteger("stage");

		if (NBT.hasKey("t_enable"))
			enabled = NBT.getBoolean("t_enable");
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		NBT.setInteger("speed", omega);
		NBT.setInteger("steamlevel", steam);

		NBT.setInteger("dmg", damage);
		NBT.setBoolean("ammonia", ammonia);

		if (inter != null)
			NBT.setInteger("blocked", inter.ordinal());
		else
			NBT.setInteger("blocked", -1);

		if (this.needsMultiblock())
			NBT.setBoolean("multi", hasMultiBlock);

		tank.writeToNBT(NBT);

		NBT.setInteger("stage", stage);

		NBT.setBoolean("t_enable", enabled);
	}

	@Override
	public AABB getRenderBoundingBox() {
		return AABB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+1, zCoord+1).expand(6, 6, 6);
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
	public final int getEmittingX() {
		return writex;
	}

	@Override
	public final int getEmittingY() {
		return writey;
	}

	@Override
	public final int getEmittingZ() {
		return writez;
	}

	@Override
	public final boolean onShiftRightClick(Level world, int x, int y, int z, ForgeDirection side) {
		return false;
	}

	@Override
	public final boolean onRightClick(Level world, int x, int y, int z, ForgeDirection side) {
		int meta = this.getBlockMetadata();
		this.setBlockMetadata(meta < (this.canOrientVertically() ? 5 : 3) ? meta+1 : 0);
		return true;
	}

	protected boolean canOrientVertically() {
		return false;
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 4*super.getMaxRenderDistanceSquared();
	}

	@Override
	public final boolean canConnectToPipe(MachineRegistry m) {
		return m == MachineRegistry.HOSE || m == MachineRegistry.BEDPIPE;
	}

	@Override
	public final boolean canConnectToPipeOnSide(MachineRegistry p, ForgeDirection side) {
		return this.canConnectToPipe(p);
	}

	@Override
	public final Flow getFlowForSide(ForgeDirection side) {
		return side == this.getSteamMovement().getOpposite() ? Flow.INPUT : Flow.NONE;
	}

	@Override
	public final int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return this.canFill(from, resource.getFluid()) ? tank.fill(resource, doFill) : 0;
	}

	@Override
	public final FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public final FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public final boolean canFill(ForgeDirection from, Fluid fluid) {
		return from == this.getSteamMovement().getOpposite() && fluid.equals(FluidRegistry.getFluid("rc lubricant"));
	}

	@Override
	public final boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public final FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}

	public final int getLubricant() {
		return tank.getLevel();
	}

	public int getLubricantToDrop() {
		return Math.abs(forcedlube-tank.getLevel()) < 25 ? forcedlube : tank.getLevel();
	}

	public void breakBlock() {
		ForgeDirection dir = this.getSteamMovement();
		int dx = xCoord+dir.offsetX;
		int dy = yCoord+dir.offsetY;
		int dz = zCoord+dir.offsetZ;
		ReactorTiles m = ReactorTiles.getTE(worldObj, dx, dy, dz);
		if (m == this.getTile()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore)this.getAdjacentTileEntity(dir);
			te.forcedlube = this.getLubricantToDrop();
		}
		dir = dir.getOpposite();
		dx = xCoord+dir.offsetX;
		dy = yCoord+dir.offsetY;
		dz = zCoord+dir.offsetZ;
		m = ReactorTiles.getTE(worldObj, dx, dy, dz);
		if (m == this.getTile()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore)this.getAdjacentTileEntity(dir);
			te.forcedlube = this.getLubricantToDrop();
		}
	}

	public final void setLubricant(ItemStack is) {
		if (ReikaItemHelper.matchStacks(this.getTile().getCraftedProduct(), is)) {
			if (is.stackTagCompound != null) {
				int lube = is.stackTagCompound.getInteger("lube");
				tank.setContents(lube, FluidRegistry.getFluid("rc lubricant"));
			}
		}
	}

	public final void addLubricant(int amt) {
		tank.addLiquid(amt, FluidRegistry.getFluid("rc lubricant"));
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
	public void getAllOutputs(Collection<BlockEntity> c, ForgeDirection dir) {
		c.add(this.getAdjacentTileEntity(this.getSteamMovement()));
	}

	@Override
	public Level getWorld() {
		return worldObj;
	}

	@Override
	public int getX() {
		return xCoord;
	}

	@Override
	public int getY() {
		return yCoord;
	}

	@Override
	public int getZ() {
		return zCoord;
	}

	@Override
	public int getIoOffsetX() {
		return 0;
	}

	@Override
	public int getIoOffsetY() {
		return 0;
	}

	@Override
	public int getIoOffsetZ() {
		return 0;
	}

	public void repairCC(int tier) {
		if (damage > 0 && rand.nextFloat() < tier*0.1F)
			damage--;
	}

}
