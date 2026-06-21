/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import java.util.ArrayList;
import java.util.Locale;

import net.minecraft.block.material.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.Level;

import reika.dragonapi.base.BlockMultiBlock;
import reika.reactorcraft.ReactorCraft;

public abstract class BlockReCMultiBlock extends BlockMultiBlock<Boolean> {

	public BlockReCMultiBlock(Material par2Material) {
		super(par2Material);
		this.setResistance(10);
		this.setHardness(2);
		this.setCreativeTab(ReactorCraft.getInstance().isLocked() ? null : ReactorCraft.tabRctrMultis);
	}

	@Override
	protected final String getFullIconPath(int i) {
		return "reactorcraft:multi/"+this.getIconBaseName()+"_"+i;
	}

	@Override
	public final ArrayList<String> getMessages(Level world, int x, int y, int z, int side) {
		BlockEntity te = this.getTileEntityForPosition(world, x, y, z);
		return te instanceof TileEntityReactorBase ? ((TileEntityReactorBase)te).getMessages(world, x, y, z, side) : new ArrayList();
	}

	public final String getName(int meta) {
		return I18n.get("multiblock."+this.getIconBaseName().toLowerCase(Locale.ENGLISH)+"."+(meta&7));
	}

	protected abstract String getIconBaseName();

	@Override
	public final boolean isOpaqueCube() {
		return false;
	}

	@Override
	public final boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isNormalCube() {
		return true;
	}

}
