/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.guis;

import net.minecraft.entity.player.EntityPlayer;

import reika.reactorcraft.base.ReactorGuiBase;
import reika.reactorcraft.container.ContainerPebbleBed;
import reika.reactorcraft.tileentities.htgr.TileEntityPebbleBed;

public class GuiPebbleBed extends ReactorGuiBase {

	public GuiPebbleBed(EntityPlayer player, TileEntityPebbleBed fuel) {
		super(new ContainerPebbleBed(player, fuel), player, fuel);
		ySize = 237;
		xSize = 240;
	}

	@Override
	public String getGuiTexture() {
		return "pebblegui";
	}
}
