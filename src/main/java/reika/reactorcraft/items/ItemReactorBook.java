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

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import reika.reactorcraft.base.ReactorItemBase;

public class ItemReactorBook extends ReactorItemBase {

	public ItemReactorBook(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide() && hand.equals(InteractionHand.MAIN_HAND)) {
			reika.reactorcraft.client.ClientScreens.openReactorBook(player, level);
		}
		return super.use(level, player, hand);
	}
}
