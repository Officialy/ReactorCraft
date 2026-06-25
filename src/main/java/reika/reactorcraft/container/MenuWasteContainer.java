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
import reika.reactorcraft.tileentities.waste.TileEntityWasteContainer;

/**
 * 26.2 port of {@code ContainerWasteContainer}: a {@code WIDTH × HEIGHT} grid (9×3 for this tile),
 * horizontally centred via {@code dx}. The dimensions are static finals on the tile, so the client
 * (reconstructing the tile from the BlockPos) sees the same values — no buf payload needed.
 */
public class MenuWasteContainer extends CoreContainer<TileEntityWasteContainer> {

    public MenuWasteContainer(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityWasteContainer) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuWasteContainer(int id, Inventory inv, TileEntityWasteContainer te) {
        super(ReactorMenus.WASTE_CONTAINER.get(), id, inv, te);

        int w = TileEntityWasteContainer.WIDTH;
        int h = TileEntityWasteContainer.HEIGHT;

        int dx = 0;
        if (w > 5)
            dx = -(w - 5) * 9;
        if (w < 5)
            dx = (w - 5) * 9;
        for (int i = 0; i < h; i++) {
            for (int k = 0; k < w; k++) {
                this.addSlot(i * w + k, 44 + k * 18 + dx, 22 + i * 18);
            }
        }

        this.addPlayerInventoryWithOffset(inv, 0, 9);
    }
}
