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
import reika.reactorcraft.base.TileEntityNuclearCore;
import reika.reactorcraft.container.MenuNuclearCore;

/**
 * 26.2 port of {@code GuiNuclearCore}. Plain inventory screen (no progress bar, no tanks).
 * Background: {@code textures/gui/fuelrod.png}; the GUI is 176 wide × 182 tall.
 */
public class ScreenNuclearCore extends ReactorGuiBase<TileEntityNuclearCore, MenuNuclearCore> {

    public ScreenNuclearCore(MenuNuclearCore container, Inventory inv, Component title) {
        super(container, inv, title, 176, 182);
    }

    @Override
    protected String getGuiTexture() {
        return "fuelrod";
    }
}
