/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.guis;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import reika.reactorcraft.base.ReactorGuiBase;
import reika.reactorcraft.container.MenuWasteStorage;
import reika.reactorcraft.tileentities.waste.TileEntityWasteStorage;

/** 26.2 port of {@code GuiWasteStorage}. Plain diamond-grid screen (176×186); texture {@code wastestorage.png}. */
public class ScreenWasteStorage extends ReactorGuiBase<TileEntityWasteStorage, MenuWasteStorage> {

    public ScreenWasteStorage(MenuWasteStorage container, Inventory inv, Component title) {
        super(container, inv, title, 176, 186);
    }

    @Override
    protected String getGuiTexture() {
        return "wastestorage";
    }
}
