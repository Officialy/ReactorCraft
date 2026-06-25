package reika.reactorcraft.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import reika.dragonapi.libraries.java.ReikaJavaLibrary;

/**
 * Inter-mod API surface: reflectively bridges into ReactorCraft's radiation system so dependent mods
 * can query/apply radiation without a hard dependency. Ported to 26.2 types (LivingEntity/Level) and
 * the {@code reika.reactorcraft} package layout.
 */
public class RadiationHandler {

	private static Class radiationHandler;
	private static Object instance;
	private static Method hasSuit;
	private static Method applyPotion;
	private static Method applyToBlock;
	private static Method fillArea;

	private static Class intensityClass;
	private static RadiationLevel[] intensities;

	private static Class entityClass;

	public static boolean hasHazmatSuit(LivingEntity e) {
		try {
			return (boolean)hasSuit.invoke(instance, e);
		}
		catch (Exception e1) {
			ReikaJavaLibrary.pConsole("Error calling ReC radiation handler!");
			e1.printStackTrace();
			return false;
		}
	}

	public static boolean applyPotionEffectToEntity(LivingEntity e, RadiationLevel ri) {
		try {
			return (boolean)applyPotion.invoke(instance, e, ri);
		}
		catch (Exception e1) {
			ReikaJavaLibrary.pConsole("Error calling ReC radiation handler!");
			e1.printStackTrace();
			return false;
		}
	}

	public static void irradiateBlock(Level world, int x, int y, int z, RadiationLevel ri) {
		try {
			applyToBlock.invoke(instance, world, x, y, z, ri);
		}
		catch (Exception e1) {
			ReikaJavaLibrary.pConsole("Error calling ReC radiation handler!");
			e1.printStackTrace();
		}
	}

	public static void irradiateArea(Level world, int x, int y, int z, int range, float density, double force, boolean lineOfSight, RadiationLevel ri) {
		try {
			fillArea.invoke(instance, world, x, y, z, range, density, force, lineOfSight, ri);
		}
		catch (Exception e1) {
			ReikaJavaLibrary.pConsole("Error calling ReC radiation handler!");
			e1.printStackTrace();
		}
	}

	public static RadiationLevel getRadiationIntensity(int idx) {
		return intensities[idx];
	}

	public static RadiationLevel getMaxRadiationIntensity() {
		return intensities[intensities.length-1];
	}

	public static Class getRadiationClass() {
		return entityClass;
	}

	public static interface RadiationLevel {

		public String name();
		public int ordinal();

		/** Whether this radiation level is even harmful */
		public boolean causesHarm();

		/** Whether an entity is sufficiently armored to be immune to this radiation level */
		public boolean hasSufficientShielding(LivingEntity e);

	}

	static {
		try {
			intensityClass = Class.forName("reika.reactorcraft.auxiliary.RadiationEffects$RadiationIntensity");
			intensities = (RadiationLevel[])intensityClass.getEnumConstants();

			radiationHandler = Class.forName("reika.reactorcraft.auxiliary.RadiationEffects");

			Field f = radiationHandler.getDeclaredField("instance");
			f.setAccessible(true);
			instance = f.get(null);

			hasSuit = radiationHandler.getDeclaredMethod("hasHazmatSuit", LivingEntity.class);
			hasSuit.setAccessible(true);
			applyPotion = radiationHandler.getDeclaredMethod("applyEffects", LivingEntity.class, intensityClass);
			applyPotion.setAccessible(true);
			applyToBlock = radiationHandler.getDeclaredMethod("transformBlock", Level.class, int.class, int.class, int.class, intensityClass);
			applyToBlock.setAccessible(true);
			fillArea = radiationHandler.getDeclaredMethod("contaminateArea", Level.class, int.class, int.class, int.class, int.class, float.class, double.class, boolean.class, intensityClass);
			fillArea.setAccessible(true);

			entityClass = Class.forName("reika.reactorcraft.entities.EntityRadiation");
		}
		catch (Exception e) {
			ReikaJavaLibrary.pConsole("Could not read ReC class!");
		}
	}

}
