package reika.reactorcraft.registry;

import java.util.HashSet;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.reactorcraft.ReactorCraft;

/**
 * Single creative tab for the ReactorCraft content slice, auto-populated from {@link ReactorBlocks}
 * and {@link ReactorItems} (mirrors RotaryCraft's catch-all tab) so new content shows up without a
 * hand-curated list.
 */
@EventBusSubscriber(modid = ReactorCraft.MODID)
public final class ReactorTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ReactorCraft.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> REACTORCRAFT =
            CREATIVE_MODE_TABS.register("reactorcraft", () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab.reactorcraft"))
                    .icon(() -> new ItemStack(ReactorItems.URANIUM_INGOT.get()))
                    .build());

    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != REACTORCRAFT.get())
            return;
        HashSet<Item> seen = new HashSet<>();
        for (var holder : ReactorBlocks.BLOCKS.getEntries()) {
            Item asItem = holder.get().asItem();
            if (asItem != Items.AIR && seen.add(asItem))
                event.accept(asItem);
        }
        for (var holder : ReactorItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (seen.add(item))
                event.accept(item);
        }
    }

    private ReactorTabs() {}
}
