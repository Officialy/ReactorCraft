package reika.reactorcraft.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import reika.reactorcraft.registry.ReactorAchievements;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Generates {@code data/reactorcraft/advancement/<name>.json} for each {@link ReactorAchievements}
 * entry. "Obtain X" advancements use an inventory_changed (has-item) criterion so they grant
 * naturally; "do X" advancements (the gameplay-triggered set) use an impossible criterion and
 * are granted from code via {@link ReactorAchievements#triggerAchievement}. The single root is
 * MINEURANIUM; dependency-less entries hang off it so the whole set forms one tab.
 */
public class ReactorAdvancementProvider extends AdvancementProvider {

    public ReactorAdvancementProvider() {
        super(List.of(Generator::new));
    }

    private static final class Generator extends AdvancementSubProvider {

        Generator(BootstrapContext<Advancement> output) {
            super(output);
        }

        // Only actual item milestones use inventory criteria. Actions keep their gameplay hooks;
        // possessing a handbook, turbine or reactor icon never proves the action happened.
        private static final Set<ReactorAchievements> ITEM_MILESTONES = EnumSet.of(
                ReactorAchievements.PEBBLE, ReactorAchievements.DEPLETED);

        private static final Identifier ROOT_BACKGROUND =
                Identifier.fromNamespaceAndPath("minecraft", "textures/block/iron_block.png");

        private static final ReactorAchievements ROOT = ReactorAchievements.MINEURANIUM;

        @Override
        public void generate() {
            Map<ReactorAchievements, AdvancementHolder> built = new EnumMap<>(ReactorAchievements.class);
            List<ReactorAchievements> remaining = new ArrayList<>(List.of(ReactorAchievements.list));

            // Build parents before children; dependency-less entries (except the root) hang off ROOT.
            while (!remaining.isEmpty()) {
                boolean progressed = false;
                Iterator<ReactorAchievements> it = remaining.iterator();
                while (it.hasNext()) {
                    ReactorAchievements a = it.next();
                    AdvancementHolder parent;
                    if (a == ROOT) {
                        parent = null;
                    } else if (a.dependency != null) {
                        if (!built.containsKey(a.dependency)) continue;
                        parent = built.get(a.dependency);
                    } else {
                        if (!built.containsKey(ROOT)) continue;
                        parent = built.get(ROOT);
                    }

                    String key = a.name().toLowerCase(Locale.ENGLISH);
                    var displayIcon = a.getIcon();

                    Advancement.Builder b = Advancement.Builder.advancement();
                    if (parent != null)
                        b.parent(parent);
                    Component title = Component.translatable("advancements.reactorcraft." + key + ".title");
                    Component description = Component.translatable("advancements.reactorcraft." + key + ".description");
                    AdvancementType type = a.isSpecial ? AdvancementType.CHALLENGE : AdvancementType.TASK;
                    if (parent == null)
                        b.rootDisplay(displayIcon, title, description, ROOT_BACKGROUND, type, true, true, false);
                    else
                        b.display(displayIcon, title, description, type, true, true, false);
                    Criterion<?> crit = !ITEM_MILESTONES.contains(a)
                            ? new Criterion<>(CriteriaTriggers.IMPOSSIBLE, new ImpossibleTrigger.TriggerInstance())
                            : reika.rotarycraft.data.RoCAdvancementsEnabled.hasItem(displayIcon.item().value());
                    b.addCriterion("trigger", crit);
                    built.put(a, b.save(this.output, "reactorcraft:" + key));
                    it.remove();
                    progressed = true;
                }
                if (!progressed) throw new IllegalStateException("Cyclic ReactorCraft advancement parents: " + remaining);
            }
        }
    }
}
