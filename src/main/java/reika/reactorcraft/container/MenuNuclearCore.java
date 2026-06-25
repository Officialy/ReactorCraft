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
import reika.reactorcraft.base.TileEntityNuclearCore;
import reika.reactorcraft.registry.ReactorMenus;

/**
 * 26.2 port of {@code ContainerNuclearCore}. Used by both the fission FUEL core and the BREEDER core.
 *
 * Slot layout (12 inventory slots, fed top→bottom):
 * <ul>
 *   <li>0-3 : fuel column   (x=80, y=23/41/59/77)</li>
 *   <li>4-7 : output left   (x=53, y=23/41/59/77)</li>
 *   <li>8-11: output right  (x=107, y=23/41/59/77)</li>
 * </ul>
 * The output-only behaviour of the old {@code SlotFurnace}s is enforced by the tile's
 * {@code canPlaceItem}, so plain handler slots suffice. {@link CoreContainer#addSlot(int, int, int)}
 * pulls each slot from the tile's {@code ManagedItemHandler}.
 */
public class MenuNuclearCore extends CoreContainer<TileEntityNuclearCore> {

    // Client-side factory (IContainerFactory): reconstruct from the BlockPos written by openMenu.
    public MenuNuclearCore(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityNuclearCore) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuNuclearCore(int id, Inventory inv, TileEntityNuclearCore te) {
        super(ReactorMenus.NUCLEAR_CORE.get(), id, inv, te);

        this.addSlot(0, 80, 23);
        this.addSlot(1, 80, 41);
        this.addSlot(2, 80, 59);
        this.addSlot(3, 80, 77);

        this.addSlot(4, 53, 23);
        this.addSlot(5, 53, 41);
        this.addSlot(6, 53, 59);
        this.addSlot(7, 53, 77);

        this.addSlot(8, 107, 23);
        this.addSlot(9, 107, 41);
        this.addSlot(10, 107, 59);
        this.addSlot(11, 107, 77);

        this.addPlayerInventoryWithOffset(inv, 0, 16);
    }
}
