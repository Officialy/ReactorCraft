package reika.reactorcraft.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Blocks implementing this show up on the Magnetic Ore Finder HUD regardless of the built-in ore
 * list. 26.2 port: the legacy {@code getRenderIcons} (IIcon array) is gone -- the HUD draws the
 * block's item icon instead.
 */
public interface MagneticOreOverride {

	boolean showOnHUD(Level world, BlockPos pos, Player ep);

}
