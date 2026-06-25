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
import reika.reactorcraft.container.MenuPebbleBed;
import reika.reactorcraft.tileentities.htgr.TileEntityPebbleBed;

/** 26.2 port of {@code GuiPebbleBed}. Large hex-grid screen (240×237); texture {@code pebblegui.png}. */
public class ScreenPebbleBed extends ReactorGuiBase<TileEntityPebbleBed, MenuPebbleBed> {

    public ScreenPebbleBed(MenuPebbleBed container, Inventory inv, Component title) {
        super(container, inv, title, 240, 237);
    }

    @Override
    protected String getGuiTexture() {
        return "pebblegui";
    }
}
