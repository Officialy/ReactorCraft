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
import reika.reactorcraft.container.MenuThoriumCore;
import reika.reactorcraft.tileentities.fission.thorium.TileEntityThoriumCore;

/**
 * 26.2 port of {@code GuiThoriumCore}. Small fluid-only screen (176×100, no item slots): three salt
 * tanks (fuel / heated fuel / waste). The live fluid-fill + hover tooltips are deferred (same 26.2
 * still-sprite gap as the other tank screens); the tank levels still sync to the client.
 */
public class ScreenThoriumCore extends ReactorGuiBase<TileEntityThoriumCore, MenuThoriumCore> {

    public ScreenThoriumCore(MenuThoriumCore container, Inventory inv, Component title) {
        super(container, inv, title, 176, 100);
    }

    @Override
    protected String getGuiTexture() {
        return "fuelpool";
    }

    @Override
    protected boolean showInventoryLabel() {
        return false;
    }
}
