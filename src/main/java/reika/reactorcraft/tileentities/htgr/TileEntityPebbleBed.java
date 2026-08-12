/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.htgr;
import net.minecraft.core.BlockPos;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.container.MenuPebbleBed;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.interfaces.blockentity.BreakAction;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.Feedable;
import reika.reactorcraft.auxiliary.PebbleBedArrangement;
import reika.reactorcraft.auxiliary.ReactorBlock;
import reika.reactorcraft.auxiliary.TemperaturedReactorTyped;
import reika.reactorcraft.base.TileEntityInventoriedReactorBase;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;

public class TileEntityPebbleBed extends TileEntityInventoriedReactorBase implements TemperaturedReactorTyped, Feedable, ReactorBlock, BreakAction {
	public TileEntityPebbleBed(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.PEBBLEBED.get(), pos, state);
	}


	protected StepTimer tempTimer = new StepTimer(20);

	private PebbleBedArrangement reactor;

	public static final int MINTEMP = 800;
	public static final int OVERTEMP = 1200;
	public static final int FAILTEMP = 4400;

	private int damage = 0;

	private int cycleCooldown = 0;

	@Override
	public int getContainerSize() {
		return 47;
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new MenuPebbleBed(id, inv, this);
	}

	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (!world.isClientSide() && this.isFissile() && ReikaRandomHelper.doWithChance(this.getFissionChance()/100D))
			this.runDecayCycle();

		if (DragonAPI.debugtest) {
			ReikaInventoryHelper.clearInventory(this);
			ReikaInventoryHelper.addToIInv(ReactorItems.PELLET.getStackOf(), this);
		}

		//ReikaJavaLibrary.pConsole(temperature, Dist.DEDICATED_SERVER);
		if (!world.isClientSide())
			this.feed();

		tempTimer.update();
		if (tempTimer.checkCap()) {
			this.updateTemperature(world, pos);
		}

		if (damage > 0 && rand.nextInt(800) == 0) {
			damage--;
		}

		if (cycleCooldown > 0) {
			cycleCooldown--;
		}
	}

	@Override
	protected void onFirstTick(Level world, BlockPos pos) {
		this.checkAndJoinArrangement(world, pos);
	}

	private void checkAndJoinArrangement(Level world, BlockPos pos) {
		reactor = new PebbleBedArrangement(this);
		int r = 3;
		for (int i = -r; i <= r; i++) {
			for (int j = -r; j <= r; j++) {
				for (int k = -r; k <= r; k++) {
					BlockEntity te = world.getBlockEntity(pos.offset(i, j, k));
					if (te instanceof TileEntityPebbleBed) {
						reactor.merge(((TileEntityPebbleBed)te).reactor);
					}
				}
			}
		}
	}

	private double getFissionChance() {
		int size = this.getReactorSize();
		if (size >= 128)
			return 1; //20
		else if (size >= 72)
			return 2; //12
		else if (size >= 48)
			return 3; //8
		else if (size >= 36)
			return 4; //8
		else if (size >= 24)
			return 6;
		else if (size >= 12)
			return 4;
		else if (size >= 6)
			return 2;
		else
			return 1;
	}

	private int getReactorSize() {
		return reactor.getSize();
	}

	public void setReactorObject(PebbleBedArrangement pba) {
		reactor = pba;
	}

	@Override
	public void breakBlock() {
		reactor.remove(this);
	}

	private void runDecayCycle() {
		if (!level.isClientSide()) {
			int slot = -1;
			for (int i = itemHandler.getSlots()-1; i >= 0; i--) {
				ItemStack is = itemHandler.getStackInSlot(i);
				if (!is.isEmpty() && is.getItem() == ReactorItems.PELLET.getItemInstance()) {
					slot = i;
					i = -1;
				}
			}
			if (slot != -1) {
				if (ReikaRandomHelper.doWithChance(3)) {
					ItemStack is = itemHandler.getStackInSlot(slot);
					itemHandler.setStackInSlot(slot, this.getFissionProduct(is));
				}
				temperature += 20;
			}
		}
	}

	private ItemStack getFissionProduct(ItemStack is) {
		if (is.getDamageValue() == ReactorItems.PELLET.getNumberMetadatas()-1)
			return ReactorItems.OLDPELLET.getStackOf();
		return ReactorItems.PELLET.getStackOfMetadata(is.getDamageValue()+1);
	}

	@Override
	protected void updateTemperature(Level world, BlockPos pos) {
		super.updateTemperature(world, pos);
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, pos);
		int dT = temperature-Tamb;

		if (dT != 0) {
			int f = ReikaWorldHelper.isExposedToAir(world, pos.getX(), pos.getY(), pos.getZ()) ? 24 : 96;
			temperature -= (1+dT/f);
		}

		if (dT > 0) {
			for (int i = 2; i < 6; i++) {
				Direction dir = dirs[i];
				BlockPos p = pos.relative(dir);
				ReactorTiles r = ReactorTiles.getTE(world, p);

				if (r == this.getTile()) {
					TileEntityPebbleBed te = (TileEntityPebbleBed)world.getBlockEntity(p);
					int dTemp = temperature-te.temperature;
					if (dTemp > 0) {
						temperature -= dTemp/16;
						te.temperature += dTemp/16;
					}
				}
			}
		}

		if (temperature >= this.getMaxTemperature()) {
			world.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
		}
		else if (temperature >= OVERTEMP) {
			int chance = 5+(FAILTEMP-temperature)/10/this.getReactorSize();
			if (rand.nextInt(chance) == 0) {
				ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.FIRE_EXTINGUISH, 1, 0.5F);
				ReikaParticleHelper.SMOKE.spawnAroundBlockWithOutset(world, pos, 9, 0.0625);
				damage++;
				if (damage >= 100) {
					this.melt(world, pos);
				}
			}
		}
	}

	private void melt(Level world, BlockPos pos) {
		ReactorAchievements.PEBBLEFAIL.triggerAchievement(this.getPlacer());
		this.delete();
		world.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
		ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.FIRE_EXTINGUISH, 2, 0.1F);
		ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.GENERIC_EXPLODE.value(), 1, 0.2F);
		ReikaParticleHelper.LAVA.spawnAroundBlockWithOutset(world, pos, 12, 0.0625);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack is) {
		return is.getItem() == ReactorItems.PELLET.getItemInstance() || is.getItem() == ReactorItems.OLDPELLET.getItemInstance();
	}

	@Override
	public boolean canItemEnterFromSide(Direction dir) {
		return dir == Direction.UP;
	}

	@Override
	public boolean canItemExitToSide(Direction dir) {
		return dir == Direction.DOWN;
	}

	@Override
	public boolean canRemoveItem(int slot, ItemStack is) {
		if (is.getItem() == ReactorItems.OLDPELLET.getItemInstance())
			return true;
		if (slot == 0 && cycleCooldown == 0 && this.getTicksExisted()%80 < 40) {
			cycleCooldown = 10;
			return true;
		}
		return false;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.PEBBLEBED;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	public boolean isFissile() {
		return ReikaInventoryHelper.locateInInventory(ReactorItems.PELLET.getItemInstance(), itemHandler) != -1;
	}

	public boolean feed() {
		BlockEntity tile = this.getAdjacentBlockEntity(Direction.DOWN);
		if (tile instanceof TileEntityPebbleBed) {
			if (((Feedable)tile).feedIn(itemHandler.getStackInSlot(itemHandler.getSlots()-1))) {
				for (int i = itemHandler.getSlots()-1; i > 0; i--)
					itemHandler.setStackInSlot(i, itemHandler.getStackInSlot(i-1));

				tile = this.getAdjacentBlockEntity(Direction.UP);
				if (tile instanceof TileEntityPebbleBed) {
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
				if (itemHandler.getStackInSlot(k).isEmpty() && !itemHandler.getStackInSlot(k-1).isEmpty()) {
					itemHandler.setStackInSlot(k, itemHandler.getStackInSlot(k-1));
					itemHandler.setStackInSlot(k-1, ItemStack.EMPTY);
					return;
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
		return FAILTEMP;
	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return false;
	}

	@Override
	public final int getTextureState(Direction side) {
		if (side.getStepY() != 0)
			return 4;
		ReactorTiles src = this.getTile();
		ReactorTiles r = ReactorTiles.getTE(level, this.getBlockPos().below());
		ReactorTiles r2 = ReactorTiles.getTE(level, this.getBlockPos().above());
		if (r2 == src && r == src)
			return 2;
		else if (r2 == src)
			return 1;
		else if (r == src)
			return 3;
		return 0;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		damage = NBT.getIntOr("dmg", 0);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("dmg", damage);
	}

	@Override
	public ReactorType getReactorType() {
		return ReactorType.HTGR;
	}

}
