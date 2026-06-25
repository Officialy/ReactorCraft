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
import reika.reactorcraft.tileentities.processing.TileEntityElectrolyzer;

/** 26.2 port of {@code ContainerElectrolyzer}: single input slot at (44,41) + player inventory. */
public class MenuElectrolyzer extends CoreContainer<TileEntityElectrolyzer> {

    public MenuElectrolyzer(int id, Inventory inv, FriendlyByteBuf data) {
        this(id, inv, (TileEntityElectrolyzer) inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public MenuElectrolyzer(int id, Inventory inv, TileEntityElectrolyzer te) {
        super(ReactorMenus.ELECTROLYZER.get(), id, inv, te);

        this.addSlot(0, 44, 41);

        this.addPlayerInventoryWithOffset(inv, 0, 9);
    }
}
