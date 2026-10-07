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
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import reika.dragonapi.instantiable.storage.ManagedItemHandler;
import reika.dragonapi.interfaces.blockentity.HasItemHandler;
import reika.dragonapi.libraries.ReikaInventoryHelper;

import java.util.Optional;

public abstract class TileEntityInventoriedReactorBase extends TileEntityReactorBase implements net.minecraft.world.WorldlyContainer, HasItemHandler {

	protected final ManagedItemHandler itemHandler = new ManagedItemHandler(getContainerSize()) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};

	public TileEntityInventoriedReactorBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public final ManagedItemHandler getItemHandler() {
		return itemHandler;
	}

	@Override
	public abstract int getContainerSize();

	@Override
	public final boolean isEmpty() {
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			if (!itemHandler.getStackInSlot(i).isEmpty())
				return false;
		}
		return true;
	}

	@Override
	public final ItemStack getItem(int slot) {
		return itemHandler.getLiveStack(slot);
	}

	@Override
	public final ItemStack removeItem(int slot, int amount) {
		return ReikaInventoryHelper.decrStackSize(itemHandler, slot, amount);
	}

	@Override
	public final ItemStack removeItemNoUpdate(int slot) {
		ItemStack is = itemHandler.getStackInSlot(slot);
		itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
		return is;
	}

	@Override
	public final void setItem(int slot, ItemStack is) {
		itemHandler.setStackInSlot(slot, is);
	}

	@Override
	public final boolean stillValid(Player ep) {
		return this.isPlayerAccessible(ep);
	}

	@Override
	public final void clearContent() {
		for (int i = 0; i < itemHandler.getSlots(); i++)
			itemHandler.setStackInSlot(i, ItemStack.EMPTY);
	}

	@Override
	public final boolean canPlaceItem(int slot, ItemStack is) {
		return this.isItemValidForSlot(slot, is);
	}

	public abstract boolean isItemValidForSlot(int slot, ItemStack is);

	/** Sided I/O rules, enforced by the block's item capability wrapper. */
	public abstract boolean canItemEnterFromSide(Direction dir);

	public abstract boolean canItemExitToSide(Direction dir);

	public abstract boolean canRemoveItem(int slot, ItemStack is);

    @Override
    public int[] getSlotsForFace(Direction side) {
        return java.util.stream.IntStream.range(0, getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return isItemValidForSlot(slot, stack) && (side == null || canItemEnterFromSide(side));
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return canRemoveItem(slot, stack) && (side == null || canItemExitToSide(side));
    }

	// 1.21.5: inventory contents bridged via ManagedItemHandler.serialize, like RC's
	// InventoriedRCBlockEntity. Sync/save of temperature etc. is handled by the superclass.
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		itemHandler.serialize(output.child("ItemsRaw"));
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		itemHandler.deserialize(input.childOrEmpty("ItemsRaw"));
	}

}
