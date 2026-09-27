package reika.reactorcraft.modinterface.jei;

import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import reika.dragonapi.ModList;
import reika.reactorcraft.registry.ReactorRecipeTypes;

/** Shares ReactorCraft's datapack machine recipes with multiplayer JEI clients. */
public final class ReactorRecipeSync {
    private ReactorRecipeSync() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ReactorRecipeSync::onDatapackSync);
        if (FMLEnvironment.getDist() == Dist.CLIENT)
            Client.register();
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(ReactorRecipeTypes.PROCESSOR.get(), ReactorRecipeTypes.CENTRIFUGE.get());
    }

    public static final class Client {
        private static RecipeMap currentRecipes;

        private Client() {}

        private static void register() {
            NeoForge.EVENT_BUS.addListener(Client::onRecipesReceived);
        }

        private static void onRecipesReceived(net.neoforged.neoforge.client.event.RecipesReceivedEvent event) {
            currentRecipes = event.getRecipeMap();
            if (ModList.JEI.isLoaded())
                ReactorJEIPlugin.onRecipeMapReceived(currentRecipes);
        }

        public static RecipeMap getCurrentRecipes() {
            return currentRecipes;
        }
    }
}
