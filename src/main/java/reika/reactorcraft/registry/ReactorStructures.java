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

import java.util.Locale;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import reika.dragonapi.base.StructureBase;
import reika.dragonapi.instantiable.data.blockstruct.FilledBlockArray;
import reika.dragonapi.instantiable.rendering.StructureRenderer;
import reika.dragonapi.interfaces.registry.StructureEnum;
import reika.reactorcraft.auxiliary.structure.FlywheelStructure;
import reika.reactorcraft.auxiliary.structure.GeneratorStructure;
import reika.reactorcraft.auxiliary.structure.InjectorStructure;
import reika.reactorcraft.auxiliary.structure.PreheaterStructure;
import reika.reactorcraft.auxiliary.structure.SolenoidStructure;
import reika.reactorcraft.auxiliary.structure.TurbineStructure;
import reika.reactorcraft.base.ReactorStructureBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public enum ReactorStructures implements StructureEnum {

	HEATER(ReactorTiles.HEATER, new PreheaterStructure()),
	GENERATOR(ReactorTiles.GENERATOR, new GeneratorStructure()),
	INJECTOR(ReactorTiles.INJECTOR, new InjectorStructure()),
	SOLENOID(ReactorTiles.SOLENOID, new SolenoidStructure()),
	HPTURBINE(ReactorTiles.BIGTURBINE, new TurbineStructure()),
	FLYWHEEL(ReactorTiles.FLYWHEEL, new FlywheelStructure());

	public static ReactorStructures[] structureList = values();

	private StructureRenderer render;
	private final ReactorTiles tile;
	public final ReactorStructureBase structure;

	private ReactorStructures(ReactorTiles r, ReactorStructureBase struct) {
		tile = r;
		structure = struct;
	}

	@SideOnly(Side.CLIENT)
	public StructureRenderer getRenderer() {
		if (render == null || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			FilledBlockArray f = this.getStructure(Minecraft.getMinecraft().theWorld, 0, 0, 0, ForgeDirection.EAST);
			render = new StructureRenderer(f);

			this.addOverrides();
		}
		return render;
	}

	private void addOverrides() {
		render.addOverride(new ItemStack(tile.getBlock(), 1, tile.getBlockMetadata()), tile.getCraftedProduct());

		render.addOverride(new ItemStack(ReactorTiles.STEAMLINE.getBlock(), 1, ReactorTiles.STEAMLINE.getBlockMetadata()), ReactorTiles.STEAMLINE.getCraftedProduct());
	}

	public FilledBlockArray getStructure(World world, int x, int y, int z, ForgeDirection dir) {
		structure.dir = dir;
		return structure.getArray(world, x, y, z);
	}

	public String getName() {
		return StatCollector.translateToLocal("reactorstruct."+this.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public StructureBase getStructure() {
		return structure;
	}

	@Override
	public boolean isNatural() {
		return false;
	}

}
