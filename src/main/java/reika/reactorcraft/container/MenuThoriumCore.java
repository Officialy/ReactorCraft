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
import reika.reactorcraft.tileentities.fission.thorium.TileEntityThoriumCore;

/**
 * 26.2 port of {@code ContainerThoriumCore}. No item slots and no player inventory — the molten-salt
 * core is purely a fluid machine (three tanks). The tanks sync via {@link TileEntityThoriumCore#writeSyncTag},
 * so the screen reads them off the tile directly.
 */
public class MenuThoriumCore extends CoreContainer<TileEntityThoriumCore> {

    public MenuThoriumCore(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityThoriumCore) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuThoriumCore(int id, Inventory inv, TileEntityThoriumCore te) {
        super(ReactorMenus.THORIUM_CORE.get(), id, inv, te);
    }
}
