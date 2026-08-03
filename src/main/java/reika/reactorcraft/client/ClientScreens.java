package reika.reactorcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import reika.reactorcraft.guis.GuiReactorBook;

/**
 * Opens ReactorCraft's client screens.
 *
 * <p>Kept out of the item classes: those are loaded during item registration on a dedicated server,
 * and a {@code Screen} type named in one makes the class unloadable there, failing mod construction.
 */
public final class ClientScreens {

    private ClientScreens() {}

    public static void openReactorBook(Player player, Level level) {
        Minecraft.getInstance().gui.setScreen(new GuiReactorBook(player, level, 0, 0));
    }
}
