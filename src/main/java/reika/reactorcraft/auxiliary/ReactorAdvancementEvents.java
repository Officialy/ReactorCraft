package reika.reactorcraft.auxiliary;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlocks;

/** V33a BlockReactorOre.onHarvested, for the modern independently registered ore blocks. */
@EventBusSubscriber(modid = "reactorcraft")
public final class ReactorAdvancementEvents {
    private ReactorAdvancementEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void harvested(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player) || player.isCreative()) return;
        var block = event.getState().getBlock();
        if (block == ReactorBlocks.PITCHBLENDE_ORE.get() || block == ReactorBlocks.END_PITCHBLENDE_ORE.get())
            ReactorAchievements.MINEURANIUM.triggerAchievement(player);
        else if (block == ReactorBlocks.CADMIUM_ORE.get())
            ReactorAchievements.MINECADMIUM.triggerAchievement(player);
    }
}
