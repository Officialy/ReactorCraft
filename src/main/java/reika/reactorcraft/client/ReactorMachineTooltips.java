/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.client;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import reika.dragonapi.libraries.mathsci.ReikaEngLibrary;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.ReactorPowerReceiver;
import reika.reactorcraft.registry.ReactorTiles;

/**
 * Adds the "power data" hover tooltip to ReactorCraft power-receiver machine items (the solenoid
 * magnet and any other {@link ReactorPowerReceiver}). Port of the legacy {@code ItemReactorPlacer.
 * addInformation}: without Shift it shows a "Hold Shift for power data" hint; with Shift held it lists
 * the machine's minimum power / torque / speed (each only when that value is a real requirement, i.e.
 * &gt; 1). Values are read from a cached dummy TE instance -- the min* getters return constants.
 */
@EventBusSubscriber(modid = ReactorCraft.MODID, value = Dist.CLIENT)
public final class ReactorMachineTooltips {

	private static final Map<ReactorTiles, ReactorPowerReceiver> RECEIVERS = new EnumMap<>(ReactorTiles.class);

	private ReactorMachineTooltips() {}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		Block b = Block.byItem(event.getItemStack().getItem());
		ReactorTiles r = ReactorTiles.getMachineMapping(b);
		if (r == null || !r.isPowerReceiver())
			return;

		ReactorPowerReceiver te = receiver(r);
		if (te == null)
			return;

		long pow = te.getMinPower();
		int trq = te.getMinTorque();
		int spd = te.getMinSpeed();
		boolean minp = pow > 1, mint = trq > 1, mins = spd > 1;
		if (!minp && !mint && !mins)
			return;

		List<Component> li = event.getToolTip();
		if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
			if (minp)
				li.add(Component.literal(String.format("Minimum Power: %.3f %sW",
						ReikaMathLibrary.getThousandBase(pow), ReikaEngLibrary.getSIPrefix(pow))));
			if (mint)
				li.add(Component.literal(String.format("Minimum Torque: %.3f %sNm",
						ReikaMathLibrary.getThousandBase(trq), ReikaEngLibrary.getSIPrefix(trq))));
			if (mins)
				li.add(Component.literal(String.format("Minimum Speed: %.3f %srad/s",
						ReikaMathLibrary.getThousandBase(spd), ReikaEngLibrary.getSIPrefix(spd))));
		} else {
			li.add(Component.literal("Hold " + ChatFormatting.GREEN + "Shift" + ChatFormatting.GRAY + " for power data"));
		}
	}

	/** A cached dummy TE for a machine type, used only to read its constant min* power requirements. */
	private static ReactorPowerReceiver receiver(ReactorTiles r) {
		return RECEIVERS.computeIfAbsent(r, tile -> {
			BlockEntity be = tile.createBlockEntity(BlockPos.ZERO, tile.getBlock().defaultBlockState());
			return be instanceof ReactorPowerReceiver rp ? rp : null;
		});
	}
}
