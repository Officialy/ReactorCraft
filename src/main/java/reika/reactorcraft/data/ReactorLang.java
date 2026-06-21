package reika.reactorcraft.data;

import java.util.Locale;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;

/**
 * en_us language provider — humanises every block / item registry id (mirrors GeoLang). Block-items
 * share the {@code block.reactorcraft.*} key {@code addBlock} already emits, so they're filtered out
 * of the item pass to avoid a duplicate-key crash.
 */
public class ReactorLang extends LanguageProvider {

    public ReactorLang(PackOutput output, String locale) {
        super(output, ReactorCraft.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("tab.reactorcraft", "ReactorCraft");

        ReactorBlocks.BLOCKS.getEntries().forEach(holder ->
                addBlock(holder, prettify(holder.getId().getPath())));

        ReactorBlocks.ITEMS.getEntries().forEach(holder -> {
            if (holder.get() instanceof BlockItem) return;
            addItem(holder, prettify(holder.getId().getPath()));
        });

        ReactorItems.ITEMS.getEntries().forEach(holder ->
                addItem(holder, prettify(holder.getId().getPath())));
    }

    private static String prettify(String path) {
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            String p = parts[i];
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
