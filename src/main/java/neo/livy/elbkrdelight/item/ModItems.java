package neo.livy.elbkrdelight.item;

import java.util.ArrayList;
import java.util.List;

import neo.livy.elbkrdelight.EndlessBackroomsDelight;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
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
 * <p>The meals are built lazily, once Endless Backrooms has actually put its effects into the
 * registry. That mod registers through Porting Lib's {@code LazyRegistrar}, which defers the real
 * registration, so its effects do not exist yet while this mod's initializer runs.
 */
public final class ModItems {
	/** Regen duration in ticks (20 ticks = 1 second). */
	private static final int REGEN_TICKS = 200;

	/** Regeneration amplifier: 0 is level I, 1 is level II. */
	private static final int REGEN_AMPLIFIER = 1;

	/** Saturation duration, matching moth jelly. Royal rations uses a longer 120 seconds. */
	private static final int SATURATION_TICKS = 300;

	/**
	 * Moth pheromone, as granted by moth jelly. Endless Backrooms' deathmoths check this effect in
	 * {@code DeathmothEntity.shouldIgnoreTarget} and refuse to attack the player while it is active,
	 * so this is the "deathmoths leave you alone" buff, not a debuff.
	 */
	private static final int MOTH_PHEROMONE_TICKS = 6000;

	/** Chance the effects are applied, from 0.0 to 1.0. */
	private static final float EFFECT_CHANCE = 1.0F;

	private static final ResourceLocation MOTH_PHEROMONE_ID =
			new ResourceLocation("endless_backrooms", "moth_pheromone");

	/** Farmer's Delight registers its creative tab under this id (see its {@code ModCreativeTabs}). */
	private static final ResourceKey<CreativeModeTab> FARMERS_DELIGHT_TAB = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB,
			new ResourceLocation("farmersdelight", "farmersdelight"));

	/**
	 * Every meal this mod adds.
	 *
	 * <p>Note that vanilla clamps the player's food level to 20, so nutrition above 20 is partly
	 * wasted; the remainder shows up as saturation, which is clamped separately.
	 */
	private static final List<Meal> MEALS = List.of(
			// Royal rations + moth jelly + sugar. The strongest of the two, and the only one that
			// grants moth pheromone, because moth jelly is one of its ingredients.
			new Meal(
					"royal_ration_stewed_moth_jelly",
					30, 0.9F,
					true,
					7200, 3),
			// Raw scit + mushroom + almond water + onion. Milder and cheaper than the royal
			// version, so a shorter withdrawal with a lower cap.
			new Meal(
					"dried_shrimp_mushroom_stew",
					14, 0.8F,
					false,
					3600, 2));

	/** Set once registration has run, so a re-synced registry cannot register everything twice. */
	private static boolean registered;

	private ModItems() {
	}

	public static void initialize() {
		// Endless Backrooms registers its effects through Porting Lib's LazyRegistrar. By the time
		// this runs they are normally already in the registry, so try straight away.
		MobEffect mothPheromone = BuiltInRegistries.MOB_EFFECT.get(MOTH_PHEROMONE_ID);

		if (mothPheromone != null) {
			registerMeals(mothPheromone);
			return;
		}

		// Not there yet, so wait for it. Note this callback only fires for entries added *after*
		// registration, which is why the direct lookup above is the normal path.
		EndlessBackroomsDelight.LOGGER.warn(
				"{} is not registered yet; deferring item registration until it appears.", MOTH_PHEROMONE_ID);

		RegistryEntryAddedCallback.event(BuiltInRegistries.MOB_EFFECT).register((rawId, id, effect) -> {
			if (MOTH_PHEROMONE_ID.equals(id)) {
				registerMeals(effect);
			}
		});
	}

	/** Builds and registers every meal, now that the effects they need exist. */
	private static void registerMeals(MobEffect mothPheromone) {
		if (registered) {
			return;
		}

		registered = true;

		List<Item> items = new ArrayList<>(MEALS.size());

		for (Meal meal : MEALS) {
			items.add(Registry.register(
					BuiltInRegistries.ITEM,
					EndlessBackroomsDelight.id(meal.path()),
					meal.create(mothPheromone)));
		}

		// Show them next to the other Farmer's Delight meals.
		ItemGroupEvents.modifyEntriesEvent(FARMERS_DELIGHT_TAB).register(entries -> items.forEach(entries::accept));

		items.forEach(item -> EndlessBackroomsDelight.LOGGER.info(
				"Registered {}", BuiltInRegistries.ITEM.getKey(item)));
	}

	/**
	 * Definition of one bowl meal.
	 *
	 * @param path                    registry path, without the namespace
	 * @param nutrition               hunger points to restore
	 * @param saturationModifier      saturation gained is {@code nutrition * this * 2}
	 * @param grantsMothPheromone     whether eating it makes deathmoths ignore the player
	 * @param withdrawalDurationTicks how long the withdrawal lasts after eating
	 * @param withdrawalMaxAmplifier  highest withdrawal amplifier it can build up to
	 */
	private record Meal(
			String path,
			int nutrition,
			float saturationModifier,
			boolean grantsMothPheromone,
			int withdrawalDurationTicks,
			int withdrawalMaxAmplifier) {

		Item create(MobEffect mothPheromone) {
			FoodProperties.Builder food = new FoodProperties.Builder()
					.nutrition(nutrition)
					.saturationMod(saturationModifier)
					.effect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, REGEN_AMPLIFIER), EFFECT_CHANCE)
					.effect(new MobEffectInstance(MobEffects.SATURATION, SATURATION_TICKS, 0), EFFECT_CHANCE);

			if (grantsMothPheromone) {
				food.effect(new MobEffectInstance(mothPheromone, MOTH_PHEROMONE_TICKS, 0), EFFECT_CHANCE);
			}

			return new AddictiveBowlFoodItem(
					new Item.Properties()
							.food(food.build())
							.stacksTo(16)
							.craftRemainder(Items.BOWL),
					withdrawalDurationTicks,
					withdrawalMaxAmplifier);
		}
	}
}
