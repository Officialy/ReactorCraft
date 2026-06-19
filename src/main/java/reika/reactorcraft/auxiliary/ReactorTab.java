/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary;

import net.minecraft.world.item.ItemStack;

import reika.dragonapi.instantiable.gui.RegistryEnumCreativeTab;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ReactorTab extends RegistryEnumCreativeTab {

	private ItemStack icon;

	public ReactorTab(String tabID) {
		super(tabID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() {
		return icon;
	}

	public void setIcon(ItemStack is) {
		icon = is;
	}

}
