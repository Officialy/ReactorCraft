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
import reika.reactorcraft.tileentities.processing.TileEntityWasteDecayer;

/** 26.2 port of {@code ContainerWasteDecayer}: fixed 5×3 slot grid at (44,22) + player inventory. */
public class MenuWasteDecayer extends CoreContainer<TileEntityWasteDecayer> {

    public MenuWasteDecayer(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityWasteDecayer) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuWasteDecayer(int id, Inventory inv, TileEntityWasteDecayer te) {
        super(ReactorMenus.WASTE_DECAYER.get(), id, inv, te);

        int w = 5;
        int h = 3;
        for (int i = 0; i < h; i++) {
            for (int k = 0; k < w; k++) {
                this.addSlot(i * w + k, 44 + k * 18, 22 + i * 18);
            }
        }

        this.addPlayerInventoryWithOffset(inv, 0, 9);
    }
}
