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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.interfaces.blockentity.ChunkLoadingTile;
import reika.dragonapi.instantiable.data.immutable.WorldLocation;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.Feedable;
import reika.reactorcraft.auxiliary.HydrogenExplosion;
import reika.reactorcraft.auxiliary.LinkableReactorCore;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.auxiliary.WasteManager;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.event.ReactorMeltdownEvent;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorOptions;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import reika.rotarycraft.api.interfaces.EMPControl;

public abstract class TileEntityNuclearCore extends TileEntityInventoriedReactorBase implements LinkableReactorCore, Feedable, ChunkLoadingTile, EMPControl {

	protected int hydrogen = 0;
	private int activeTimer = 0;

	private static final int MAX_HYDROGEN = 200;

	public static final int CLADDING = 800;
	public static final int HYDROGEN = 1400;
	public static final int MELTDOWN = 1800;

	private WorldLocation CPU;

	public TileEntityNuclearCore(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public void link(TileEntityCPU te) {
		CPU = new WorldLocation(te);
	}

	@Override
	protected void onFirstTick(Level world, BlockPos pos) {

	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (!world.isClientSide() && this.isFissile() && rand.nextInt(this.getDecayNeutronChance()) == 0)
			world.addFreshEntity(new EntityNeutron(world, pos, this.getRandomDirection(false), NeutronType.DECAY));

		if (DragonAPI.debugtest) {
			ReikaInventoryHelper.clearInventory(this);
			ReikaInventoryHelper.addToIInv(ReactorItems.FUEL.getStackOf(), this);
		}

		if (!world.isClientSide()) {
			this.feedWaste(world, pos);
			this.feed();
		}

		if (activeTimer > 0) {
			activeTimer--;
			if (activeTimer == 0)
				this.onActivityChange(false);
		}

		thermalTicker.update();
		if (thermalTicker.checkCap()) {
			this.updateTemperature(world, pos);
		}
		if (temperature > CLADDING) {
			if (rand.nextInt(20) == 0)
				ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.FIRE_EXTINGUISH);
			ReikaParticleHelper.SMOKE.spawnAroundBlockWithOutset(world, pos, 9, 0.0625);
		}
		else if (temperature > this.getWarningTemperature() && ReikaRandomHelper.doWithChance(20)) {
			if (rand.nextInt(20) == 0)
				ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.FIRE_EXTINGUISH);
			ReikaParticleHelper.SMOKE.spawnAroundBlockWithOutset(world, pos, 4, 0.0625);
		}
	}

	protected int getDecayNeutronChance() {
		return 20;
	}

	protected int getWarningTemperature() {
		return 500;
	}

	private void onActivityChange(boolean active) {
		if (!level.isClientSide() && ReactorOptions.CHUNKLOADING.getState()) {
			if (active) {
				// CHUNKLOAD-PORT: ChunkManager.instance.loadChunks(this); — DragonAPI chunkloading
				// manager is not ported yet (RC has it commented out too); re-enable when it lands.
			}
			else {
				this.unload();
			}
		}
		level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
	}

	private void unload() {
		// CHUNKLOAD-PORT: ChunkManager.instance.unloadChunks(this); — see onActivityChange.
	}

	@Override
	public Collection<ChunkPos> getChunksToLoad() {
		Set<ChunkPos> set = new HashSet();
		int cx = getBlockPos().getX() >> 4;
		int cz = getBlockPos().getZ() >> 4;
		for (int i = -1; i <= 1; i++) {
			for (int k = -1; k <= 1; k++) {
				set.add(new ChunkPos(cx+i, cz+k));
			}
		}
		return set;
	}

	private void feedWaste(Level world, BlockPos pos) {
		BlockEntity te = this.getAdjacentBlockEntity(Direction.DOWN);
		if (te instanceof TileEntityNuclearCore) {
			for (int i = 4; i < 12; i++) {
				if (!this.getItem(i).isEmpty()) {
					for (int k = 4; k < 12; k++) {
						if (((TileEntityNuclearCore) te).getItem(k).isEmpty()) {
							((TileEntityNuclearCore) te).setItem(k, this.getItem(i));
							this.setItem(i, ItemStack.EMPTY);
						}
					}
				}
			}
		}
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public final int getContainerSize() {
		return 12;
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new reika.reactorcraft.container.MenuNuclearCore(id, inv, this);
	}

	@Override
	public final int getTemperature() {
		return temperature;
	}

	@Override
	public final void setTemperature(int T) {
		temperature = T;
	}

	public boolean feed() {
		BlockEntity tile = this.getAdjacentBlockEntity(Direction.DOWN);
		if (tile instanceof Feedable) {
			if (((Feedable)tile).feedIn(this.getItem(3))) {
				this.setItem(3, this.getItem(2));
				this.setItem(2, this.getItem(1));
				this.setItem(1, this.getItem(0));

				tile = this.getAdjacentBlockEntity(Direction.UP);
				if (tile instanceof Feedable) {
					this.setItem(0, ((Feedable) tile).feedOut());
				}
				else
					this.setItem(0, ItemStack.EMPTY);
			}
		}
		this.collapseInventory();
		return false;
	}

	private void collapseInventory() {
		for (int i = 0; i < 4; i++) {
			for (int k = 3; k > 0; k--) {
				if (this.getItem(k).isEmpty() && !this.getItem(k-1).isEmpty()) {
					this.setItem(k, this.getItem(k-1));
					this.setItem(k-1, ItemStack.EMPTY);
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
		if (this.getItem(0).isEmpty()) {
			this.setItem(0, is.copy());
			return true;
		}
		return false;
	}

	@Override
	public ItemStack feedOut() {
		if (this.getItem(3).isEmpty())
			return ItemStack.EMPTY;
		else {
			ItemStack is = this.getItem(3).copy();
			this.setItem(3, ItemStack.EMPTY);
			return is;
		}
	}

	protected final void tryPushSpentFuel(int slot) {
		for (int i = 4; i < 12; i++) {
			if (this.getItem(i).isEmpty()) {
				this.setItem(i, this.getItem(slot));
				this.setItem(slot, ItemStack.EMPTY);
				return;
			}
		}
	}

	public abstract boolean isFissile();

	@Override
	public final boolean canItemEnterFromSide(Direction dir) {
		return dir == Direction.UP;
	}

	@Override
	public final boolean canItemExitToSide(Direction dir) {
		return dir == Direction.DOWN;
	}

	protected boolean checkPoisonedChance() {
		int count = 0;
		for (int i = 4; i < 12; i++) {
			ItemStack is = this.getItem(i);
			if (!is.isEmpty() && is.getItem() == ReactorItems.WASTE.getItemInstance())
				count++;
		}
		return rand.nextInt(9-count) == 0;
	}

	protected void addWaste() {
		boolean flag = false;
		ItemStack waste = WasteManager.getRandomWasteItem();
		for (int i = 4; i < 12 && !flag; i++) {
			ItemStack inslot = this.getItem(i);
			if (inslot.isEmpty()) {
				this.setItem(i, waste);
				flag = true;
			}
			else if (ItemStack.isSameItemSameComponents(waste, inslot) && inslot.getCount()+waste.getCount() <= waste.getMaxStackSize()) {
				inslot.grow(waste.getCount());
				flag = true;
			}
		}
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		boolean inactive = activeTimer <= 0;
		activeTimer = 2400; //2 min
		if (inactive)
			this.onActivityChange(true);
		return false;
	}

	public final boolean isActive() {
		return activeTimer > 0;
	}

	protected final void spawnNeutronBurst(Level world, BlockPos pos) {
		if (world.isClientSide())
			return;
		NeutronType n = this.getNeutronType();
		if (n == null) {
			ReactorCraft.LOGGER.error("Reactor core "+this+" has no neutron type and thus a null or invalid reactor type, but is still spawning neutrons!");
			return;
		}
		for (int i = 0; i < 3; i++) {
			world.addFreshEntity(new EntityNeutron(world, pos, this.getRandomDirection(ReactorOptions.VERTNEUTRONS.getState()), n));
		}
	}

	protected final NeutronType getNeutronType() {
		ReactorType r = this.getReactorType();
		return r != null ? r.getNeutronType() : null;
	}

	@Override
	public int getMaxTemperature() {
		return MELTDOWN;
	}

	protected void onMeltdown(Level world, BlockPos pos) {
		NeoForge.EVENT_BUS.post(new ReactorMeltdownEvent(world, pos.getX(), pos.getY(), pos.getZ()));
		if (world.isClientSide())
			return;
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		int r = 2;
		for (int i = x-r; i <= x+r; i++) {
			for (int j = y-r; j <= y+r; j++) {
				for (int k = z-r; k <= z+r; k++) {
					BlockPos ipos = new BlockPos(i, j, k);
					ReactorTiles src = this.getTile();
					ReactorTiles other = ReactorTiles.getTE(world, ipos);
					if (src == other)
						world.setBlockAndUpdate(ipos, ReactorBlocks.CORIUMFLOWING.get().defaultBlockState());
				}
			}
		}
		world.explode(null, x+0.5, y+0.5, z+0.5, 8, Level.ExplosionInteraction.BLOCK);

		double scatter = RadiationEffects.instance.contaminateArea(world, x, y, z, 32, 8, 2, true, RadiationIntensity.LETHAL);
		this.testAndDoHydrogenExplosion(world, pos, scatter);
	}

	private void testAndDoHydrogenExplosion(Level world, BlockPos pos, double scatter) {
		HydrogenExplosion ex = new HydrogenExplosion(world, null, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 7);
		ex.doExplosionA();
		ex.doExplosionB(false);
	}

	protected int getRestingTemperature(Level world, BlockPos pos) {
		return ReikaWorldHelper.getAmbientTemperatureAt(world, pos);
	}

	@Override
	protected void updateTemperature(Level world, BlockPos pos) {
		super.updateTemperature(world, pos);
		int Tamb = this.getRestingTemperature(world, pos);
		int dT = temperature-Tamb;

		if (dT != 0) {
			int d = ReikaWorldHelper.isExposedToAir(world, pos.getX(), pos.getY(), pos.getZ()) ? 32 : 64;
			d = this.getAmbientHeatLossFactor(world, pos, d, Tamb);
			temperature -= (1+dT/d);
		}

		if (dT > 0) {
			for (int i = 2; i < 6; i++) {
				Direction dir = dirs[i];
				BlockPos dpos = pos.relative(dir);
				ReactorTiles r = ReactorTiles.getTE(world, dpos);
				if (r == this.getTile()) {
					TileEntityNuclearCore te = (TileEntityNuclearCore)world.getBlockEntity(dpos);
					int dTemp = temperature-te.temperature;
					if (dTemp > 0) {
						int d = this.getSameCoreHeatConductionFraction();
						temperature -= dTemp/d;
						te.temperature += dTemp/d*this.getHeatConductionEfficiency(te);
					}
				}
			}
		}

		if (temperature >= this.getWarningTemperature()+100) {
			ReactorAchievements.HOTCORE.triggerAchievement(this.getPlacer());
		}

		if (temperature > MELTDOWN) {
			this.onMeltdown(world, pos);
			ReactorAchievements.MELTDOWN.triggerAchievement(this.getPlacer());
		}

		if (temperature > HYDROGEN) {
			hydrogen += 1;
			if (hydrogen > MAX_HYDROGEN) {
				this.testAndDoHydrogenExplosion(world, pos, 1);
			}
		}
		else if (hydrogen > 0) {
			hydrogen--;
		}
	}

	private int getSameCoreHeatConductionFraction() {
		return 16;
	}

	protected int getAmbientHeatLossFactor(Level world, BlockPos pos, int base, int Tamb) {
		return base;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		activeTimer = NBT.getIntOr("activetick", 0);
		hydrogen = NBT.getIntOr("h2", 0);

		if (NBT.contains("cpu"))
			CPU = WorldLocation.readTag(NBT.getCompoundOrEmpty("cpu"));
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("activetick", activeTimer);
		NBT.putInt("h2", hydrogen);

		if (CPU != null)
			NBT.put("cpu", CPU.writeToTag());
	}

	@Override
	public final void breakBlock() {
		if (!level.isClientSide())
			this.unload();
		if (CPU != null) {
			BlockEntity te = CPU.getBlockEntity();
			if (te instanceof TileEntityCPU) {
				((TileEntityCPU)te).removeTemperatureCheck(this);
			}
		}
	}

	@Override
	public final int getTextureState(Direction side) {
		if (side.getStepY() != 0)
			return 4;
		ReactorTiles src = this.getTile();
		ReactorTiles r = ReactorTiles.getTE(level, getBlockPos().below());
		ReactorTiles r2 = ReactorTiles.getTE(level, getBlockPos().above());
		if (r2 == src && r == src)
			return 2;
		else if (r2 == src)
			return 1;
		else if (r == src)
			return 3;
		return 0;
	}

	@Override
	public final void onHitWithEMP(BlockEntity te) {
		temperature += 500;
	}

}
