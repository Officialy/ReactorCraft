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
import reika.reactorcraft.tileentities.processing.TileEntityUProcessor;

/** 26.2 port of {@code ContainerProcessor}: 3 input slots at (44,22/40/58) + player inventory. */
public class MenuProcessor extends CoreContainer<TileEntityUProcessor> {

    public MenuProcessor(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityUProcessor) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuProcessor(int id, Inventory inv, TileEntityUProcessor te) {
        super(ReactorMenus.PROCESSOR.get(), id, inv, te);

        this.addSlot(0, 44, 22);
        this.addSlot(1, 44, 40);
        this.addSlot(2, 44, 58);

        this.addPlayerInventoryWithOffset(inv, 0, 9);
    }
}
