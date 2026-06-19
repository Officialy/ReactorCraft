/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import reika.reactorcraft.auxiliary.ReactorPowerReceiver;
import reika.rotarycraft.api.power.PowerTransferHelper;


public abstract class TankedReactorPowerReceiver extends TileEntityTankedReactorMachine implements ReactorPowerReceiver {

	private long power;
	private int omega;
	private int torque;

	private int iotick;

	public TankedReactorPowerReceiver(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (!PowerTransferHelper.checkPowerFrom(this, Direction.DOWN)) {
			this.noInputMachine();
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		if (iotick > 0)
			iotick -= 8;
	}

	@Override
	public final int getOmega() {
		return omega;
	}

	@Override
	public final int getTorque() {
		return torque;
	}

	@Override
	public final long getPower() {
		return power;
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
	public final void setOmega(int omega) {
		this.omega = omega;
	}

	@Override
	public final void setTorque(int torque) {
		this.torque = torque;
	}

	@Override
	public final void setPower(long power) {
		this.power = power;
	}

	@Override
	public final boolean isReceiving() {
		return true;
	}

	@Override
	public final void noInputMachine() {
		torque = omega = 0;
		power = 0;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		omega = NBT.getIntOr("speed", 0);
		torque = NBT.getIntOr("trq", 0);
		power = NBT.getLongOr("pwr", 0);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("speed", omega);
		NBT.putInt("trq", torque);
		NBT.putLong("pwr", power);
	}

	public final boolean sufficientPower() {
		return power >= this.getMinPower() && omega >= this.getMinSpeed() && torque >= this.getMinTorque();
	}
}
