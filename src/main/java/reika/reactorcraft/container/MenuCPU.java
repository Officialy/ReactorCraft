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
import reika.reactorcraft.tileentities.fission.TileEntityCPU;

/**
 * 26.2 port of the reactor-CPU container. The CPU panel has no item slots — it's a control-rod
 * management UI (insert/retract/toggle via packets). {@code setAlwaysInteractable} keeps it open
 * while managing the reactor.
 */
public class MenuCPU extends CoreContainer<TileEntityCPU> {

    public MenuCPU(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityCPU) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuCPU(int id, Inventory inv, TileEntityCPU te) {
        super(ReactorMenus.CPU.get(), id, inv, te);
        this.setAlwaysInteractable();
    }
}
