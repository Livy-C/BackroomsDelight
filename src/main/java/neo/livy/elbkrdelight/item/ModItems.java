package neo.livy.elbkrdelight.item;

import neo.livy.elbkrdelight.EndlessBackroomsDelight;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Registers this mod's items.
 *
 * <p>{@link RoyalRationStewedMothJellyItem} handles the stew's own behaviour, including the bowl
 * return and the withdrawal effect it shares with its ingredients.
 */
public final class ModItems {
	/**
	 * Hunger points this meal tries to restore.
	 *
	 * <p>Note that vanilla clamps the player's food level to 20, so anything above 20 is wasted:
	 * eating this at zero hunger still leaves the player at 20. The value is kept at the requested
	 * 30 anyway, and the bonus shows up as saturation instead, which is clamped separately.
	 */
	private static final int NUTRITION = 30;

	/** Saturation multiplier; saturation gained is {@code nutrition * this * 2}. */
	private static final float SATURATION_MODIFIER = 0.9F;

	/** How long the regeneration lasts, in ticks (20 ticks = 1 second). */
	private static final int EFFECT_DURATION_TICKS = 200;

	/** Regeneration amplifier: 0 is level I, 1 is level II. */
	private static final int EFFECT_AMPLIFIER = 1;

	/** Chance the effect is applied, from 0.0 to 1.0. */
	private static final float EFFECT_CHANCE = 1.0F;

	/**
	 * Saturation, matching what royal rations and moth jelly each grant. 15 seconds is the
	 * duration moth jelly uses; royal rations uses a longer 120 seconds.
	 */
	private static final int SATURATION_DURATION_TICKS = 300;

	/**
	 * Moth pheromone, as granted by moth jelly. Endless Backrooms' deathmoths check this effect in
	 * {@code DeathmothEntity.shouldIgnoreTarget} and refuse to attack the player while it is active,
	 * so this is the "deathmoths leave you alone" buff, not a debuff.
	 */
	private static final int MOTH_PHEROMONE_DURATION_TICKS = 6000;

	public static final RoyalRationStewedMothJellyItem ROYAL_RATION_STEWED_MOTH_JELLY;

	static {
		// 1.20.1 Item.Properties is consumed by the Item constructor and cannot be applied
		// afterwards, so the properties are built here and passed straight in.
		ROYAL_RATION_STEWED_MOTH_JELLY = new RoyalRationStewedMothJellyItem(new Item.Properties()
				.food(new FoodProperties.Builder()
						.nutrition(NUTRITION)
						.saturationMod(SATURATION_MODIFIER)
						.effect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER), EFFECT_CHANCE)
						.effect(new MobEffectInstance(MobEffects.SATURATION, SATURATION_DURATION_TICKS, 0), EFFECT_CHANCE)
						.effect(new MobEffectInstance(mothPheromone(), MOTH_PHEROMONE_DURATION_TICKS, 0), EFFECT_CHANCE)
						.build())
				.stacksTo(16)
				.craftRemainder(Items.BOWL));
	}

	/**
	 * Looks up the moth pheromone effect from the registry instead of referencing
	 * {@code ModEffects.MOTH_PHEROMONE} directly.
	 *
	 * <p>Endless Backrooms ships no sources, so compiling against its classes would bind this mod to
	 * their exact internal layout. The mob effect registry is a stable, public contract, and the
	 * effect is only read at runtime, after their initializer has registered it.
	 *
	 * @throws IllegalStateException if the effect is missing, which means the required
	 *                               Endless Backrooms version is not the one this mod expects
	 */
	private static MobEffect mothPheromone() {
		MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation("endless_backrooms", "moth_pheromone"));

		if (effect == null) {
			throw new IllegalStateException(
					"Missing mob effect endless_backrooms:moth_pheromone - Endless Backrooms must be installed.");
		}

		return effect;
	}

	/** Farmer's Delight registers its creative tab under this id (see its {@code ModCreativeTabs}). */
	private static final ResourceKey<CreativeModeTab> FARMERS_DELIGHT_TAB = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB,
			new ResourceLocation("farmersdelight", "farmersdelight"));

	private ModItems() {
	}

	/** Called from the mod initializer; touching this class is what triggers the static block. */
	public static void initialize() {
		Registry.register(
				BuiltInRegistries.ITEM,
				EndlessBackroomsDelight.id("royal_ration_stewed_moth_jelly"),
				ROYAL_RATION_STEWED_MOTH_JELLY);

		// Show it next to the other Farmer's Delight meals.
		ItemGroupEvents.modifyEntriesEvent(FARMERS_DELIGHT_TAB)
				.register(entries -> entries.accept(ROYAL_RATION_STEWED_MOTH_JELLY));
	}
}
