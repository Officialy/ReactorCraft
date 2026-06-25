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
import reika.reactorcraft.container.MenuWasteContainer;
import reika.reactorcraft.tileentities.waste.TileEntityWasteContainer;

/** 26.2 port of {@code GuiWasteContainer}. Plain grid screen; texture {@code wastecontainer2.png}. */
public class ScreenWasteContainer extends ReactorGuiBase<TileEntityWasteContainer, MenuWasteContainer> {

    public ScreenWasteContainer(MenuWasteContainer container, Inventory inv, Component title) {
        super(container, inv, title, 176, 175);
    }

    @Override
    protected String getGuiTexture() {
        return "wastecontainer2";
    }
}
