/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import reika.dragonapi.libraries.mathsci.Isotopes;
import reika.dragonapi.libraries.mathsci.isotopes.ElementGroup;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.auxiliary.WasteManager;
import reika.reactorcraft.base.ItemReactorMulti;
import reika.reactorcraft.entities.EntityNuclearWaste;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemNuclearWaste extends ItemReactorMulti {

	public ItemNuclearWaste(int tex) {
		super(tex);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack) {
		EntityNuclearWaste ei = new EntityNuclearWaste(world, location.posX, location.posY, location.posZ, itemstack);
		ei.motionX = location.motionX;
		ei.motionY = location.motionY;
		ei.motionZ = location.motionZ;
		ei.delayBeforeCanPickup = 10;
		return ei;
	}

	@Override
	public void onUpdate(ItemStack is, World world, Entity e, int p4, boolean p5) {
		if (e instanceof EntityLivingBase) {
			if (RadiationEffects.instance.applyEffects((EntityLivingBase)e, RadiationIntensity.HIGHLEVEL)) {
				if (e instanceof EntityPlayer) {
					if (!((EntityPlayer)e).capabilities.isCreativeMode)
						ReactorAchievements.HOLDWASTE.triggerAchievement((EntityPlayer)e);
				}
			}
		}
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer ep, List li, boolean adv) {
		if (is.getItemDamage() >= 1000) {
			ElementGroup g = ElementGroup.values()[is.getItemDamage()-1000];
			li.add("Mixed Waste: "+g.displayName);
		}
		else {
			//List<Isotopes> iso = WasteManager.getWasteList();
			Isotopes atom = Isotopes.getIsotope(is.getItemDamage());//iso.get(is.getItemDamage());
			li.add(atom.getDisplayName());
			li.add("Half Life: "+atom.getHalfLifeAsDisplay());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item ID, CreativeTabs cr, List li) {
		for (Isotopes iso : WasteManager.getWasteList()) {
			ItemStack item = new ItemStack(ID, 1, iso.ordinal());
			li.add(item);
		}
		for (ElementGroup g : ElementGroup.values()) {
			ItemStack item = new ItemStack(ID, 1, 1000+g.ordinal());
			li.add(item);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		return ReactorItems.WASTE.getBasicName();
	}

	@Override
	public int getTextureOffset(ItemStack is) {
		return is.getItemDamage() >= 1000 ? 14 : 0;
	}

}
