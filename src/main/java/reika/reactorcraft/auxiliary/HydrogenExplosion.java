package reika.reactorcraft.auxiliary;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Hydrogen fireball: delegates to vanilla {@link Level#explode} in 26.2 (old {@code Explosion} subclass removed). */
public final class HydrogenExplosion {

	private final Level level;
	private final double x;
	private final double y;
	private final double z;
	private final float power;

	public HydrogenExplosion(Level level, Entity source, double x, double y, double z, float power) {
		this(level, x, y, z, power);
	}

	public HydrogenExplosion(Level level, Entity source, double x, double y, double z, float power, double scatterFraction) {
		this(level, x, y, z, power);
	}

	private HydrogenExplosion(Level level, double x, double y, double z, float power) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.z = z;
		this.power = power;
	}

	public void doExplosionA() {
		// Block scatter from the 1.7.10 falling-block loop deferred; radiation contamination is separate.
	}

	public void doExplosionB(boolean fire) {
		if (!level.isClientSide()) {
			level.explode(null, x, y, z, power, fire, Level.ExplosionInteraction.BLOCK);
		}
	}
}
