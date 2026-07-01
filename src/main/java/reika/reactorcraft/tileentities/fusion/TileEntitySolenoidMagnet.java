/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fusion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.libraries.ReikaAABBHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.auxiliary.ReactorPowerReceiver;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.blocks.multi.BlockSolenoidCasing;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fusion.TileEntityToroidMagnet.Aim;
import reika.rotarycraft.api.power.PowerTransferHelper;

public class TileEntitySolenoidMagnet extends TileEntityReactorBase implements ReactorPowerReceiver, MultiBlockTile, NeutronTile {

	public TileEntitySolenoidMagnet(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.SOLENOID.get(), pos, state);
	}

	private boolean hasMultiBlock = false;
	private boolean checkForToroids = true;

	private int torque;
	private int omega;
	private long power;
	private int iotick;
	private float speed = 0;

	public static final int MINOMEGA = 256;
	public static final int MAX_SPEED = 8192;
	public static final int MINTORQUE = 32768;
	private static final int MAX_SAFE_SPEED = 30;

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.SOLENOID;
	}

	public boolean hasMultiBlock() {
		return hasMultiBlock;
	}

	public void setHasMultiBlock(boolean has) {
		if (hasMultiBlock && !has)
			this.testBreakageFailure();
		hasMultiBlock = has;
	}

	private void testBreakageFailure() {
		if (omega > 32) {
			this.fail(level, this.getBlockPos(), 6F);
		}
	}

	private void fail(Level world, BlockPos pos, float power) {
		world.removeBlock(pos, false);
		world.explode(null, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, power, Level.ExplosionInteraction.BLOCK);
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (!PowerTransferHelper.checkPowerFrom(this, Direction.DOWN)) {
			this.noInputMachine();
		}

		float v = 0.1F;
		if (this.canTurn()) {
			if (speed > MAX_SAFE_SPEED)
				v *= 3;
			speed = Math.min(speed+v, this.getMaxRenderSpeed());
		}
		else {
			speed = Math.max(0, speed-v);
		}

		if (DragonAPI.debugtest) {
			hasMultiBlock = true;
			torque = MINTORQUE*8;
			omega = 4096;
			power = (long)omega*(long)torque;
		}

		if (ReactorCraft.LOGGER.isDebugEnabled()) {
			if (world.isClientSide())
				ReactorCraft.LOGGER.debug("Clientside "+this+" receiving "+torque+" Nm @ "+omega+" rad/s. Phi="+phi);
			else
				ReactorCraft.LOGGER.debug("Serverside "+this+" receiving "+torque+" Nm @ "+omega+" rad/s.");
		}

		if (DragonAPI.debugtest || hasMultiBlock && checkForToroids && this.arePowerReqsMet()) {
			this.addToToroids();
		}
		if (!hasMultiBlock || !this.arePowerReqsMet()) {
			this.removeFromToroids();
		}
		if (hasMultiBlock && torque >= MINTORQUE && speed > MAX_SAFE_SPEED*4) { //violently fail
			this.fail(world, pos, 8F);
		}
	}

	@Override
	protected void onFirstTick(Level world, BlockPos pos) {
		if (!hasMultiBlock) {
			this.checkForMultiBlock(world, pos);
		}
	}

	private void checkForMultiBlock(Level world, BlockPos pos) {
		// The core IS the structure centre; tryAssemble scans out from here for the casing layout.
		// Runs regardless of placement order (unlike the legacy "casing directly below" check), so
		// placing the core last still forms the multiblock. Any casing instance carries the shared
		// validation logic -- SHELL is arbitrary.
		BlockSolenoidCasing b = (BlockSolenoidCasing) ReactorBlocks.SOLENOID_HUB.get();
		b.tryAssemble(world, pos.getX(), pos.getY(), pos.getZ(), null);
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		if (hasMultiBlock) {
			phi += speed;
		}
		else
			phi = 0;
	}

	private float getMaxRenderSpeed() {
		if (omega > MAX_SPEED)
			return 512;
		else if (omega >= 4096)
			return MAX_SAFE_SPEED;
		else if (omega >= 2048)
			return 20F;
		else if (omega >= 1024)
			return 7.5F;
		else
			return 4.5F;
	}

	public boolean canTurn() {
		return hasMultiBlock && power > 0 && torque >= MINTORQUE;
	}

	public boolean arePowerReqsMet() {
		return omega >= MINOMEGA && this.canTurn();
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putBoolean("multi", hasMultiBlock);

		NBT.putInt("omg", omega);
		NBT.putInt("tq", torque);
		NBT.putLong("pwr", power);

		NBT.putFloat("phi", phi);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		hasMultiBlock = NBT.getBooleanOr("multi", false);

		omega = NBT.getIntOr("omg", 0);
		torque = NBT.getIntOr("tq", 0);
		power = NBT.getLongOr("pwr", 0L);

		phi = NBT.getFloatOr("phi", 0F);
	}

	public void addToToroids() {
		Level world = level;
		int x = this.getBlockPos().getX();
		int y = this.getBlockPos().getY();
		int z = this.getBlockPos().getZ();

		x += 14; //radius of tokamak
		z -= 2;

		ReactorTiles r = ReactorTiles.getTE(world, new BlockPos(x, y, z));
		int c = 0;
		Aim a = Aim.W;
		while ((r == ReactorTiles.MAGNET || r == ReactorTiles.INJECTOR) && c <= 38) {
			if (r == ReactorTiles.MAGNET) {
				TileEntityToroidMagnet te = (TileEntityToroidMagnet)world.getBlockEntity(new BlockPos(x, y, z));
				te.hasSolenoid = true;
				a = te.getAim();
			}
			x += a.xOffset;
			z += a.zOffset;
			r = ReactorTiles.getTE(world, new BlockPos(x, y, z));
			c++;
		}
		checkForToroids = false;
	}

	public void removeFromToroids() {
		Level world = level;
		int x = this.getBlockPos().getX();
		int y = this.getBlockPos().getY();
		int z = this.getBlockPos().getZ();

		x += 14;
		z -= 2;

		ReactorTiles r = ReactorTiles.getTE(world, new BlockPos(x, y, z));
		int c = 0;
		Aim a = Aim.W;
		while ((r == ReactorTiles.MAGNET || r == ReactorTiles.INJECTOR) && c < 38) {
			if (r == ReactorTiles.MAGNET) {
				TileEntityToroidMagnet te = (TileEntityToroidMagnet)world.getBlockEntity(new BlockPos(x, y, z));
				te.hasSolenoid = false;
				a = te.getAim();
			}
			x += a.xOffset;
			z += a.zOffset;
			r = ReactorTiles.getTE(world, new BlockPos(x, y, z));
			c++;
		}
		checkForToroids = true;
	}

	public boolean canRenderCoil() {
		return hasMultiBlock;
	}

	// 1.21.5: BlockEntity.getRenderBoundingBox was removed; kept as a helper for the renderer's bounds.
	public AABB getRenderBoundingBox() {
		return ReikaAABBHelper.getBlockAABB(this.getBlockPos()).inflate(9, 2, 9);
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
	public int getIORenderAlpha() {
		return iotick;
	}

	@Override
	public void setIORenderAlpha(int io) {
		iotick = io;
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
	public boolean canReadFrom(Direction dir) {
		return dir == Direction.DOWN;
	}

	@Override
	public boolean isReceiving() {
		return true;
	}

	@Override
	public void noInputMachine() {
		torque = omega = 0;
		power = 0;
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
		return MINOMEGA;
	}

	@Override
	public long getMinPower() {
		return 1;
	}

	@Override
	public int getUpdatePacketRadius() {
		return 96; //much larger visually
	}

	@Override
	public void breakBlock() {
		if (!level.isClientSide()) {
			BlockPos pos = this.getBlockPos();
			for (int i = 0; i < 6; i++) {
				Direction dir = dirs[i];
				BlockPos p = pos.relative(dir);
				Block b = level.getBlockState(p).getBlock();
				if (b instanceof BlockReCMultiBlock) {
					((BlockReCMultiBlock)b).breakMultiBlock(level, p.getX(), p.getY(), p.getZ());
				}
			}
		}
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

}
