/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fission;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.world.level.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.core.Direction;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.data.blockstruct.abstractsearch.PropagationCondition;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.instantiable.data.immutable.Coordinate;
import reika.dragonapi.libraries.reikanbthelper.NBTTypes;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.LinkableReactorCore;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.auxiliary.ReactorBlock;
import reika.reactorcraft.auxiliary.ReactorControlLayout;
import reika.reactorcraft.auxiliary.ReactorPowerReceiver;
import reika.reactorcraft.auxiliary.TemperaturedReactorTyped;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.event.ScramEvent;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorSounds;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;
import reika.rotarycraft.api.power.PowerTransferHelper;

public class TileEntityCPU extends TileEntityReactorBase implements ReactorPowerReceiver, TemperaturedReactorTyped, ReactorBlock, NeutronTile {
	public TileEntityCPU(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.CPU.get(), pos, state);
	}


	private ReactorControlLayout layout;
	private final BlockArray reactor = new BlockArray();
	private final ArrayList<TemperatureMonitor> temperatureChecks = new ArrayList();

	public static final int POWERPERROD = 1024;

	private int omega;
	private int torque;
	private long power;

	private int redstoneUpdate = 200;

	private final PropagationCondition reactorBlocks = new PropagationCondition() {

		@Override
		public boolean isValidLocation(Level world, int x, int y, int z, Coordinate from) {
			return world.getBlockEntity(x, y, z) instanceof ReactorBlock;
		}

	};

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		thermalTicker.update();
		if (thermalTicker.checkCap())
			this.updateTemperature(world, x, y, z);
		if (!world.isClientSide()) {
			if (world.getTotalWorldTime()%64 == 0)
				reactor.clear();

			if (reactor.isEmpty()) {
				layout.clear();
				int r = 12;
				reactor.recursiveAddCallbackWithBounds(world, x, y, z, x-r, y-4, z-r, x+r, y+4, z+r, reactorBlocks);
				for (int i = 0; i < reactor.getSize(); i++) {
					Coordinate c = reactor.getNthBlock(i);
					int dx = c.xCoord;
					int dy = c.yCoord;
					int dz = c.zCoord;
					Block idx = world.getBlock(dx, dy, dz);
					int metax = world.getBlockMetadata(dx, dy, dz);
					if (idx == ReactorTiles.CONTROL.getBlock() && metax == ReactorTiles.CONTROL) {
						TileEntityControlRod rod = (TileEntityControlRod)world.getBlockEntity(dx, dy, dz);
						layout.addControlRod(rod);
					}
				}
				this.syncAllData(true);
			}
		}

		//TileEntity te = this.getAdjacentTileEntity(Direction.DOWN);
		//if (te instanceof TileEntityCPU) {
		//	power = ((TileEntityCPU)te).power;
		//}
		if (DragonAPI.debugtest) {
			omega = 1024;
			torque = 1024;
			power = omega*torque;
		}
		else if (!PowerTransferHelper.checkPowerFromAllSides(this, true)) {
			this.noInputMachine();
		}

		if (world.isClientSide())
			return;

		if (power < this.getMinPower() && this.getTicksExisted() > 20)
			this.SCRAM();

		if (layout.getNumberRods() > 0) {
			if ((temperature > this.getMaxTemperature() || (!temperatureChecks.isEmpty() && temperatureChecks.get(rand.nextInt(temperatureChecks.size())).getTemperature(this) > this.getMaxTemperature())) && power >= this.getMinPower()*4) {
				ReactorAchievements.SCRAM.triggerAchievement(this.getPlacer());
				this.SCRAM();
			}
		}

		if (redstoneUpdate > 0) {
			redstoneUpdate--;
			if (redstoneUpdate <= 0) {
				world.markBlockForUpdate(x, y, z);
				ReikaWorldHelper.causeAdjacentUpdates(world, x, y, z);
			}
		}

	}

	@Override
	protected void onFirstTick(Level world, int x, int y, int z) {
		layout = new ReactorControlLayout(this);
	}

	public void SCRAM() {
		MinecraftForge.EVENT_BUS.post(new ScramEvent(this, temperature));
		layout.SCRAM();
		if (redstoneUpdate == 0)
			redstoneUpdate = 7;
		//TileEntity te = this.getAdjacentTileEntity(Direction.UP);
		//if (te instanceof TileEntityCPU)
		//	((TileEntityCPU)te).SCRAM();
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.CPU;
	}

	public ReactorControlLayout getLayout() {
		return layout;
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
		return 0;
	}

	@Override
	public void setIORenderAlpha(int io) {}

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
		return true;
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
	public int getTemperature() {
		return temperature;
	}

	@Override
	public void setTemperature(int T) {
		temperature = T;
	}

	@Override
	public int getMaxTemperature() {
		return 800;
	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return liq != LiquidStates.EMPTY;
	}

	public void lowerAllRods() {
		Collection<TileEntityControlRod> li = layout.getAllRods();
		for (TileEntityControlRod te : li) {
			te.setActive(true, false);
		}
		ReactorSounds.CONTROL.playSoundAtBlock(level, xCoord, yCoord, zCoord, 1, 1.3F);
		redstoneUpdate = 30;
	}

	public void raiseAllRods() {
		Collection<TileEntityControlRod> li = layout.getAllRods();
		for (TileEntityControlRod te : li) {
			te.setActive(false, false);
		}
		ReactorSounds.CONTROL.playSoundAtBlock(level, xCoord, yCoord, zCoord, 1, 1.3F);
		redstoneUpdate = 30;
	}

	@Override
	public int getMinTorque(int available) {
		return 1;
	}

	@Override
	public int getMinTorque() {
		return 1;
	}

	@Override
	public int getMinSpeed() {
		return 1;
	}

	@Override
	public long getMinPower() {
		long base = layout != null ? layout.getMinPower() : 0;
		//TileEntity te = this.getAdjacentTileEntity(Direction.UP);
		//if (te instanceof TileEntityCPU)
		//	base += ((TileEntityCPU)te).getMinPower();
		return base;
	}

	@Override
	public int getRedstoneOverride() {
		return layout.isEmpty() ? 0 : 15*layout.countLoweredRods()/layout.getNumberRods();
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		omega = NBT.getIntOr("speed", 0);
		torque = NBT.getIntOr("trq", 0);
		power = NBT.getLongOr("pwr", 0L);

		redstoneUpdate = NBT.getIntOr("redsu", 0);

		if (layout != null)
			layout.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("speed", omega);
		NBT.putInt("trq", torque);
		NBT.putLong("pwr", power);

		NBT.putInt("redsu", redstoneUpdate);

		if (layout != null)
			layout.writeToNBT(NBT);
	}

	@Override
	public void saveAdditional(/*PORT*/CompoundTag NBT) {
		super.saveAdditional(/*PORT*/NBT);

		ListTag li = new ListTag();
		for (TemperatureMonitor m : temperatureChecks) {
			li.add(m.writeTag());
		}
		NBT.setTag("checks", li);
	}

	@Override
	public void loadAdditional(/*PORT*/CompoundTag NBT) {
		super.loadAdditional(/*PORT*/NBT);

		temperatureChecks.clear();
		ListTag li = NBT.getTagList("checks", NBTTypes.COMPOUND.ID);
		for (Object o : li.tagList) {
			CompoundTag tag = (CompoundTag)o;
			temperatureChecks.add(TemperatureMonitor.readTag(tag));
		}
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

	public void addTemperatureCheck(LinkableReactorCore te) {
		temperatureChecks.add(new TemperatureMonitor(te));
		te.link(this);
	}

	public void removeTemperatureCheck(LinkableReactorCore te) {
		temperatureChecks.remove(new TemperatureMonitor(te));
	}

	@Override
	public ReactorType getReactorType() {
		return ReactorType.FISSION;
	}

	private static class TemperatureMonitor {

		private final Coordinate location;

		private TemperatureMonitor(LinkableReactorCore te) {
			location = new Coordinate((BlockEntity)te);
		}

		private TemperatureMonitor(Coordinate c) {
			location = c;
		}

		public CompoundTag saveAdditional(/*PORT*/) {
			return location.writeToTag();
		}

		public static TemperatureMonitor readTag(CompoundTag tag) {
			return new TemperatureMonitor(Coordinate.readTag(tag));
		}

		public int getTemperature(TileEntityCPU te) {
			return ((LinkableReactorCore)location.getBlockEntity(te.level)).getTemperature();
		}

		@Override
		public int hashCode() {
			return location.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof TemperatureMonitor && ((TemperatureMonitor)o).location.equals(location);
		}

	}

}
