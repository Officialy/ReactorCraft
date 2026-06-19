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
import reika.reactorcraft.container.ContainerWasteContainer;
import reika.reactorcraft.tileentities.waste.TileEntityWasteContainer;

public class GuiWasteContainer extends ReactorGuiBase {

	public GuiWasteContainer(EntityPlayer player, TileEntityWasteContainer fuel) {
		super(new ContainerWasteContainer(player, fuel), player, fuel);
		ySize = 175;
	}

	@Override
	public String getGuiTexture() {
		return "wastecontainer2";
	}
}
