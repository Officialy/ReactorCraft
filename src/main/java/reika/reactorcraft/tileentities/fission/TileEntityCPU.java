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
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.data.blockstruct.AbstractSearch;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.*;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.container.MenuCPU;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.event.ScramEvent;
import reika.reactorcraft.registry.*;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;
import reika.rotarycraft.api.power.PowerTransferHelper;

import java.util.ArrayList;
import java.util.Collection;

public class TileEntityCPU extends TileEntityReactorBase implements ReactorPowerReceiver, TemperaturedReactorTyped, ReactorBlock, NeutronTile {

	public TileEntityCPU(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.CPU.get(), pos, state);
		// NOTE: do NOT create the ReactorControlLayout here — it builds a WorldLocation that requires a
		// non-null level, which the BE does not have at construction time (NPE -> CPU fails to load).
		// The layout is created lazily in getLayout() once the level is set (see below).
	}

	private ReactorControlLayout layout;
	private final BlockArray reactor = new BlockArray();
	private final ArrayList<TemperatureMonitor> temperatureChecks = new ArrayList();

	public static final int POWERPERROD = 1024;

	private int omega;
	private int torque;
	private long power;

	private int redstoneUpdate = 200;

	private final AbstractSearch.PropagationCondition reactorBlocks = (world, pos, from) -> world.getBlockEntity(pos) instanceof ReactorBlock;

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		thermalTicker.update();
		if (thermalTicker.checkCap())
			this.updateTemperature(world, pos);
		if (!world.isClientSide()) {
			if (world.getGameTime()%64 == 0)
				reactor.clear();

			if (reactor.isEmpty()) {
				layout.clear();
				int r = 12;
				reactor.recursiveAddCallbackWithBounds(world, x, y, z, x-r, y-4, z-r, x+r, y+4, z+r, reactorBlocks);
				for (int i = 0; i < reactor.getSize(); i++) {
					BlockPos c = reactor.getNthBlock(i);
					if (ReactorTiles.getTE(world, c) == ReactorTiles.CONTROL) {
						TileEntityControlRod rod = (TileEntityControlRod)world.getBlockEntity(c);
						layout.addControlRod(rod);
					}
				}
				this.syncAllData(true);
			}
		}

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
				this.triggerBlockUpdate();
				ReikaWorldHelper.causeAdjacentUpdates(world, pos);
			}
		}

	}

	@Override
	protected void onFirstTick(Level world, BlockPos pos) {
		layout = new ReactorControlLayout(this);
	}

	public void SCRAM() {
		NeoForge.EVENT_BUS.post(new ScramEvent(this, temperature));
		layout.SCRAM();
		if (redstoneUpdate == 0)
			redstoneUpdate = 7;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.CPU;
	}

	public ReactorControlLayout getLayout() {
		// Lazy init: the layout needs a non-null level (WorldLocation), which isn't available in the
		// constructor. Creating it on first access (server tick OR client sync) keeps both sides working
		// without the constructor NPE.
		if (layout == null && level != null)
			layout = new ReactorControlLayout(this);
		return layout;
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new MenuCPU(id, inv, this);
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
		ReactorSounds.CONTROL.playSoundAtBlock(level, this.getBlockPos(), 1, 1.3F);
		redstoneUpdate = 30;
	}

	public void raiseAllRods() {
		Collection<TileEntityControlRod> li = layout.getAllRods();
		for (TileEntityControlRod te : li) {
			te.setActive(false, false);
		}
		ReactorSounds.CONTROL.playSoundAtBlock(level, this.getBlockPos(), 1, 1.3F);
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
		return layout != null ? layout.getMinPower() : 0;
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

		// Lazy-init on the client so the synced control-rod grid is received (level is set by sync time).
		// Pass this.level so the rod cache resolves on the CLIENT level on a client sync read — otherwise
		// WorldLocation.getWorld() resolves to the integrated server level and the cross-thread lookup
		// returns null, leaving the grid empty.
		ReactorControlLayout l = this.getLayout();
		if (l != null)
			l.readFromNBT(NBT, this.level);

		temperatureChecks.clear();
		ListTag li = NBT.getListOrEmpty("checks");
		for (int j = 0; j < li.size(); j++) {
			temperatureChecks.add(TemperatureMonitor.readTag(li.getCompoundOrEmpty(j)));
		}
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

		ListTag li = new ListTag();
		for (TemperatureMonitor m : temperatureChecks) {
			li.add(m.writeTag());
		}
		NBT.put("checks", li);
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

		private final BlockPos location;

		private TemperatureMonitor(LinkableReactorCore te) {
			location = ((BlockEntity)te).getBlockPos();
		}

		private TemperatureMonitor(BlockPos c) {
			location = c;
		}

		public CompoundTag writeTag() {
			CompoundTag t = new CompoundTag();
			t.putLong("pos", location.asLong());
			return t;
		}

		public static TemperatureMonitor readTag(CompoundTag tag) {
			return new TemperatureMonitor(BlockPos.of(tag.getLongOr("pos", 0)));
		}

		public int getTemperature(TileEntityCPU te) {
			return ((LinkableReactorCore)te.level.getBlockEntity(location)).getTemperature();
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
