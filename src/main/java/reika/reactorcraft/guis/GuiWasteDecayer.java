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
import reika.reactorcraft.container.ContainerWasteDecayer;
import reika.reactorcraft.tileentities.processing.TileEntityWasteDecayer;

public class GuiWasteDecayer extends ReactorGuiBase {

	public GuiWasteDecayer(EntityPlayer player, TileEntityWasteDecayer fuel) {
		super(new ContainerWasteDecayer(player, fuel), player, fuel);
		ySize = 175;
	}

	@Override
	public String getGuiTexture() {
		return "wastedecayer";
	}
}
