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
import reika.reactorcraft.container.ContainerWasteStorage;
import reika.reactorcraft.tileentities.waste.TileEntityWasteStorage;

public class GuiWasteStorage extends ReactorGuiBase {

	public GuiWasteStorage(EntityPlayer player, TileEntityWasteStorage fuel) {
		super(new ContainerWasteStorage(player, fuel), player, fuel);
		ySize = 186;
	}

	@Override
	public String getGuiTexture() {
		return "wastestorage";
	}
}
