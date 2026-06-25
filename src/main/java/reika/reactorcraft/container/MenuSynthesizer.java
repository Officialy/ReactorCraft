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
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer;

/** 26.2 port of {@code ContainerSynthesizer}: 3 slots at (35,62)/(80,26)/(80,44) + player inventory. */
public class MenuSynthesizer extends CoreContainer<TileEntitySynthesizer> {

    public MenuSynthesizer(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntitySynthesizer) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuSynthesizer(int id, Inventory inv, TileEntitySynthesizer te) {
        super(ReactorMenus.SYNTHESIZER.get(), id, inv, te);

        this.addSlot(0, 35, 62);
        this.addSlot(1, 80, 26);
        this.addSlot(2, 80, 44);

        this.addPlayerInventoryWithOffset(inv, 0, 9);
    }
}
