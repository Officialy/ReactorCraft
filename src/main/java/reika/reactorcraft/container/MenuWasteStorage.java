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
import reika.reactorcraft.auxiliary.SlotNuclearWaste;
import reika.reactorcraft.registry.ReactorMenus;
import reika.reactorcraft.tileentities.waste.TileEntityWasteStorage;

/**
 * 26.2 port of {@code ContainerWasteStorage}: 12 {@link SlotNuclearWaste} slots in a diamond + player
 * inventory. The custom slots enforce the waste-only filter (Container-backed, like the original).
 */
public class MenuWasteStorage extends CoreContainer<TileEntityWasteStorage> {

    public MenuWasteStorage(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityWasteStorage) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuWasteStorage(int id, Inventory inv, TileEntityWasteStorage te) {
        super(ReactorMenus.WASTE_STORAGE.get(), id, inv, te);

        this.addSlot(new SlotNuclearWaste(te, 0, 70, 20));
        this.addSlot(new SlotNuclearWaste(te, 1, 90, 20));

        this.addSlot(new SlotNuclearWaste(te, 2, 50, 40));
        this.addSlot(new SlotNuclearWaste(te, 3, 70, 40));
        this.addSlot(new SlotNuclearWaste(te, 4, 90, 40));
        this.addSlot(new SlotNuclearWaste(te, 5, 110, 40));

        this.addSlot(new SlotNuclearWaste(te, 6, 50, 60));
        this.addSlot(new SlotNuclearWaste(te, 7, 70, 60));
        this.addSlot(new SlotNuclearWaste(te, 8, 90, 60));
        this.addSlot(new SlotNuclearWaste(te, 9, 110, 60));

        this.addSlot(new SlotNuclearWaste(te, 10, 70, 80));
        this.addSlot(new SlotNuclearWaste(te, 11, 90, 80));

        this.addPlayerInventoryWithOffset(inv, 0, 20);
    }
}
