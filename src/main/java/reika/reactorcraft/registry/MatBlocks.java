/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.registry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.IIcon;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.Level;

import reika.dragonapi.exception.RegistrationException;
import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.reactorcraft.ReactorCraft;

public enum MatBlocks {

	CONCRETE("block.concrete"),
	SLAG("block.slag"),
	CALCITE("block.calcite"),
	SCRUBBER("block.scrubber"),
	LODESTONE("block.lodestone"),
	GRAPHITE("block.graphite");

	private String name;
	public final Class tileClass;

	public static final MatBlocks[] matList = values();

	private MatBlocks(String n) {
		this(n, null);
	}

	private MatBlocks(String n, Class<? extends TileEntity> c) {
		name = n;
		tileClass = c;
	}

	public String getName() {
		return I18n.get(name);
	}

	public boolean isMultiSidedTexture() {
		if (this == SCRUBBER)
			return true;
		return false;
	}

	public ItemStack getStackOf() {
		return new ItemStack(ReactorBlocks.MATS.getBlockInstance(), 1, this.ordinal());
	}

	public ItemStack getStackOf(int size) {
		return new ItemStack(ReactorBlocks.MATS.getBlockInstance(), size, this.ordinal());
	}

	public BlockKey getBlock() {
		return new BlockKey(ReactorBlocks.MATS.getBlockInstance(), this.ordinal());
	}

	public IIcon getIcon() {
		return ReactorBlocks.MATS.getBlockInstance().getIcon(0, this.ordinal());
	}

	public TileEntity createTile(World world) {
		if (tileClass == null)
			return null;
		try {
			return (TileEntity)tileClass.newInstance();
		}
		catch (Exception e) {
			throw new RegistrationException(ReactorCraft.getInstance(), "Could not create TileEntity for MatBlock "+this+"!", e);
		}
	}

	public boolean hasTile() {
		return tileClass != ItemStack.EMPTY;
	}

}
