/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import reika.dragonapi.base.CoreContainer;
import reika.reactorcraft.registry.ReactorMenus;
import reika.reactorcraft.tileentities.processing.TileEntityCentrifuge;

/**
 * 26.2 port of {@code ContainerCentrifuge}. Two item slots (input/output) + player inventory.
 *
 * No {@code ContainerData}: the centrifuge's {@code time} and fluid {@code tank} are persisted in
 * {@link TileEntityCentrifuge#writeSyncTag} and shipped to the client by the BlockEntityBase sync, so
 * the screen reads {@code getProcessingScaled}/{@code getFluid} off the tile directly.
 */
public class MenuCentrifuge extends CoreContainer<TileEntityCentrifuge> {

    public MenuCentrifuge(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityCentrifuge) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuCentrifuge(int id, Inventory inv, TileEntityCentrifuge te) {
        super(ReactorMenus.CENTRIFUGE.get(), id, inv, te);

        this.addSlot(0, 44, 62);
        this.addSlot(1, 116, 62);

        this.addPlayerInventoryWithOffset(inv, 0, 0);
    }
}
