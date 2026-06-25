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
import net.minecraft.world.inventory.Slot;

import reika.dragonapi.base.CoreContainer;
import reika.reactorcraft.registry.ReactorMenus;
import reika.reactorcraft.tileentities.htgr.TileEntityPebbleBed;

/**
 * 26.2 port of {@code ContainerPebbleBed}: hexagonal pebble grid (47 slots, row widths
 * {11,11,9,7,5,3,1}, each row inset by {@code 18*(11-width)/2}) + player inventory offset to
 * (dx=31, dy=77). The player-inventory layout is replicated by hand to keep the 4px hotbar offset
 * the legacy GUI used (CoreContainer's helper would place the hotbar 4px lower).
 */
public class MenuPebbleBed extends CoreContainer<TileEntityPebbleBed> {

    public MenuPebbleBed(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityPebbleBed) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuPebbleBed(int id, Inventory inv, TileEntityPebbleBed te) {
        super(ReactorMenus.PEBBLE_BED.get(), id, inv, te);

        int[] num = {11, 11, 9, 7, 5, 3, 1};
        int slot = 0;
        for (int f = 0; f < num.length; f++) {
            for (int i = 0; i < num[f]; i++) {
                int dx = 18 * (11 - num[f]) / 2;
                this.addSlot(slot, 21 + 18 * i + dx, 25 + 18 * f);
                slot++;
            }
        }

        int dx = 31;
        int dy = 77;
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 9; k++) {
                this.addSlot(new Slot(inv, k + i * 9 + 9, 8 + k * 18 + dx, 84 + i * 18 + dy));
            }
        }
        dy -= 4;
        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inv, j, 8 + j * 18 + dx, 142 + dy));
        }
    }
}
