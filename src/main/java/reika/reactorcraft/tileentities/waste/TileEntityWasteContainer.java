/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.waste;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.mathsci.Isotopes;
import reika.dragonapi.libraries.mathsci.ReikaNuclearHelper;
import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.Feedable;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.base.TileEntityWasteUnit;
import reika.reactorcraft.container.MenuWasteContainer;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.TemperatureTE;

public class TileEntityWasteContainer extends TileEntityWasteUnit implements TemperatureTE, Feedable, NeutronTile {

	public TileEntityWasteContainer(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.WASTECONTAINER.get(), pos, state);
	}

	public static final int WIDTH = 9;
	public static final int HEIGHT = 3;

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.WASTECONTAINER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		thermalTicker.update();

		if (thermalTicker.checkCap()) {
			int waste = this.countWaste();
			temperature += waste*ReikaNuclearHelper.getWasteDecayHeat();
			this.updateTemperature(world, pos);
		}

		if (!world.isClientSide())
			this.decayWaste();

		if (!world.isClientSide())
			this.feed();
	}

	@Override
	protected boolean accountForOutGameTime() {
		return false;
	}

	private void distributeHeat(Level world, BlockPos pos) {
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, pos);
		if (temperature > Tamb) {
			Direction side = null;
			for (Direction d : Direction.values()) {
				if (world.getFluidState(pos.relative(d)).getType() == Fluids.WATER) {
					side = d;
					break;
				}
			}
			if (side != null) {
				temperature -= ReikaThermoHelper.getTemperatureIncrease(1, 15000, ReikaThermoHelper.WATER_BLOCK_HEAT);
				if (temperature > 100)
					ReikaWorldHelper.changeAdjBlock(world, pos, side, Blocks.AIR.defaultBlockState());
				else
					ReikaWorldHelper.changeAdjBlock(world, pos, side, Blocks.WATER.defaultBlockState());
			}
		}
		if (temperature < Tamb)
			temperature = Tamb;
		if (temperature > this.getMaxTemperature()) {
			this.overheat(world, pos);
		}
		else if (temperature > this.getMaxTemperature()/2 && rand.nextInt(6) == 0) {
			this.smokeAndFizz(world, pos);
		}
		else if (temperature > this.getMaxTemperature()/4 && rand.nextInt(20) == 0) {
			this.smokeAndFizz(world, pos);
		}
	}

	private void smokeAndFizz(Level world, BlockPos pos) {
		if (world instanceof ServerLevel sl)
			sl.sendParticles(ParticleTypes.SMOKE, pos.getX()+rand.nextDouble(), pos.getY()+1, pos.getZ()+rand.nextDouble(), 1, 0, 0, 0, 0);
		ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.FIRE_EXTINGUISH);
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public boolean canRemoveItem(int i, ItemStack itemstack) {
		return isLongLivedWaste(itemstack);
	}

	@Override
	public int getContainerSize() {
		return WIDTH*HEIGHT;
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new MenuWasteContainer(id, inv, this);
	}

	@Override
	public int getTemperature() {
		return temperature;
	}

	@Override
	public void addTemperature(int T) {
		temperature += T;
	}

	public int getMaxTemperature() {
		return 600;
	}

	public void onMeltdown(Level world, BlockPos pos) {
		world.explode(null, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 9, Level.ExplosionInteraction.BLOCK);
		RadiationEffects.instance.contaminateArea(world, pos.getX(), pos.getY(), pos.getZ(), 9, 4, 1.5, true, RadiationIntensity.LETHAL);
		ReactorAchievements.WASTELEAK.triggerAchievement(this.getPlacer());
	}

	@Override
	public void updateTemperature(Level world, BlockPos pos) {
		this.distributeHeat(world, pos);
	}

	@Override
	public int getThermalDamage() {
		return temperature/100;
	}

	@Override
	public void overheat(Level world, BlockPos pos) {
		if (!world.isClientSide())
			this.onMeltdown(world, pos);
	}

	@Override
	public boolean leaksRadiation() {
		return true;
	}

	@Override
	public boolean isValidIsotope(Isotopes i) {
		return !isLongLivedWaste(i);
	}

	public boolean feed() {
		BlockEntity tile = this.getAdjacentBlockEntity(Direction.DOWN);
		if (tile instanceof TileEntityWasteContainer) {
			if (((Feedable)tile).feedIn(itemHandler.getStackInSlot(itemHandler.getSlots()-1))) {
				for (int i = itemHandler.getSlots()-1; i > 0; i--)
					itemHandler.setStackInSlot(i, itemHandler.getStackInSlot(i-1));

				tile = this.getAdjacentBlockEntity(Direction.UP);
				if (tile instanceof TileEntityWasteContainer) {
					itemHandler.setStackInSlot(0, ((Feedable) tile).feedOut());
				}
				else
					itemHandler.setStackInSlot(0, ItemStack.EMPTY);
			}
		}
		this.collapseInventory();
		return false;
	}

	private void collapseInventory() {
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			for (int k = itemHandler.getSlots()-1; k > 0; k--) {
				if (itemHandler.getStackInSlot(k).isEmpty()) {
					itemHandler.setStackInSlot(k, itemHandler.getStackInSlot(k-1));
					itemHandler.setStackInSlot(k-1, ItemStack.EMPTY);
				}
			}
		}
	}

	@Override
	public boolean feedIn(ItemStack is) {
		if (is.isEmpty())
			return true;
		if (!this.isItemValidForSlot(0, is))
			return false;
		if (itemHandler.getStackInSlot(0).isEmpty()) {
			itemHandler.setStackInSlot(0, is.copy());
			return true;
		}
		return false;
	}

	@Override
	public ItemStack feedOut() {
		ItemStack last = itemHandler.getStackInSlot(itemHandler.getSlots()-1);
		if (last.isEmpty())
			return ItemStack.EMPTY;
		ItemStack is = last.copy();
		itemHandler.setStackInSlot(itemHandler.getSlots()-1, ItemStack.EMPTY);
		return is;
	}

	public void setTemperature(int temp) {
		temperature = temp;
	}

	@Override
	public boolean allowExternalHeating() {
		return false;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

	@Override
	protected boolean canBeAccelerated() {
		return false;
	}

	@Override
	protected double getBaseDecayRate() {
		return 1;
	}

	@Override
	public boolean hasAnInventory() {
		return true;
	}

	@Override
	public boolean hasATank() {
		return false;
	}

}
