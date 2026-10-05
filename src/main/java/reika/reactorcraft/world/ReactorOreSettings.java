package reika.reactorcraft.world;

import java.util.Locale;
import reika.dragonapi.libraries.level.LegacyOreVeins;
import reika.reactorcraft.registry.ReactorOreType;
import reika.reactorcraft.registry.ReactorOptions;

/** Original force-generation options and equivalent-ore fallback, evaluated with loaded tags. */
public final class ReactorOreSettings {
    private ReactorOreSettings() {}
    public static boolean enabled(ReactorOreType ore) {
        boolean forced=switch (ore) {
            case CADMIUM -> ReactorOptions.CADMIUMORE.getState();
            case INDIUM -> ReactorOptions.INDIUMORE.getState();
            case SILVER -> ReactorOptions.SILVERORE.getState();
            case CALCITE -> ReactorOptions.CALCITEORE.getState();
            case MAGNETITE -> ReactorOptions.MAGNETORE.getState();
            default -> true;
        };
        return forced || !LegacyOreVeins.hasEquivalent(ore.getBlock(),ore.name().toLowerCase(Locale.ROOT));
    }
    public static int passes(int base, float density, int discrete, boolean rainbow) {
        int count=Math.max(1,(int)(base*density))*Math.max(1,discrete);
        return rainbow ? (int)(count/4F) : count;
    }
}
