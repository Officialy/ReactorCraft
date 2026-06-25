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
import reika.reactorcraft.container.MenuWasteDecayer;
import reika.reactorcraft.tileentities.processing.TileEntityWasteDecayer;

/** 26.2 port of {@code GuiWasteDecayer}. Plain inventory screen; texture {@code wastedecayer.png}. */
public class ScreenWasteDecayer extends ReactorGuiBase<TileEntityWasteDecayer, MenuWasteDecayer> {

    public ScreenWasteDecayer(MenuWasteDecayer container, Inventory inv, Component title) {
        super(container, inv, title, 176, 175);
    }

    @Override
    protected String getGuiTexture() {
        return "wastedecayer";
    }
}
