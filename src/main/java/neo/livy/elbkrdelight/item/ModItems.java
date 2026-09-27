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

	/** Night vision from blue almond water. Three minutes, long enough to cross a dark level. */
	private static final int NIGHT_VISION_TICKS = 3600;

	/**
	 * The dark stew's own effect: the room goes dark around the eater. Vanilla's own darkness, the
	 * one the Warden inflicts, so it is a real handicap rather than a flavour text.
	 */
	private static final int DARKNESS_TICKS = 300;

	/** Brief nausea, the "that was a mistake" beat that follows eating something rotten. */
	private static final int NAUSEA_TICKS = 200;

	/**
	 * Wretched cycle, on the dark stew. Endless Backrooms uses this as a punishment: every second it
	 * teleports the victim back to where they were and spawns a hostile wretch, and notably the
	 * mod's own almond water deliberately refuses to cure it (see {@code AlmondWater}). Two minutes
	 * of that is a serious cost, which is the point - the dark stew hands out the most nutrition of
	 * anything here and then makes you pay for it.
	 */
	private static final int WRETCHED_CYCLE_TICKS = 2400;

	/** Chance the effects are applied, from 0.0 to 1.0. */
	private static final float EFFECT_CHANCE = 1.0F;

	/** Amount of a bowl meal that fits in a stack, matching Farmer's Delight. */
	private static final int MEAL_STACK_SIZE = 16;

	private static final ResourceLocation MOTH_PHEROMONE_ID =
			new ResourceLocation("endless_backrooms", "moth_pheromone");

	/** Endless Backrooms' punishment effect, used by the dark stew. */
	private static final ResourceLocation WRETCHED_CYCLE_ID =
			new ResourceLocation("endless_backrooms", "wretched_cycle");

	/** Farmer's Delight registers its creative tab under this id (see its {@code ModCreativeTabs}). */
	private static final ResourceKey<CreativeModeTab> FARMERS_DELIGHT_TAB = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB,
			new ResourceLocation("farmersdelight", "farmersdelight"));

	/**
	 * Every bowl meal this mod adds.
	 *
	 * <p>Note that vanilla clamps the player's food level to 20, so nutrition above 20 is partly
	 * wasted; the remainder shows up as saturation, which is clamped separately.
	 */
	private static final List<Meal> MEALS = List.of(
			// Royal rations + moth jelly + sugar. The strongest of them, and the only one that
			// grants moth pheromone, because moth jelly is one of its ingredients.
			new Meal("royal_ration_stewed_moth_jelly", 30, 0.9F, 7200, 3, effects -> List.of(
					effect(MobEffects.REGENERATION, REGEN_TICKS, REGEN_AMPLIFIER),
					effect(MobEffects.SATURATION, SATURATION_TICKS, 0),
					effect(effects.mothPheromone(), MOTH_PHEROMONE_TICKS, 0))),
			// Corruption liquid + almond water + carpet + wallpaper + onion + minced beef. Six
			// ingredients, and the only genuine trade in the set: it hands out the most nutrition of
			// anything here, then takes your sight away for fifteen seconds and makes the Backrooms
			// itself hunt you for two minutes.
			new Meal("dark_stew", 16, 1.0F, 7200, 3, effects -> List.of(
					effect(MobEffects.REGENERATION, REGEN_TICKS, REGEN_AMPLIFIER),
					effect(MobEffects.SATURATION, SATURATION_TICKS, 0),
					effect(MobEffects.DARKNESS, DARKNESS_TICKS, 0),
					effect(MobEffects.CONFUSION, NAUSEA_TICKS, 0),
					effect(effects.wretchedCycle(), WRETCHED_CYCLE_TICKS, 0))),
			// Cooked scit + almond water + onion + cooked rice + ham + potato. A full meal in the
			// sense that it is rice with the stew poured over it, hence the high nutrition for a
			// dish with no special effect.
			new Meal("dried_shrimp_rice_bowl", 18, 0.9F, 3600, 2, effects -> List.of(
					effect(MobEffects.REGENERATION, REGEN_TICKS, REGEN_AMPLIFIER),
					effect(MobEffects.SATURATION, SATURATION_TICKS, 0))),
			// Cooked scit + onion + ham + minced beef + egg + pie crust. Six slots of a real pie
			// rather than a bowl of stew, so the richest of the non-dark meals.
			new Meal("dried_shrimp_pie", 20, 0.8F, 3600, 3, effects -> List.of(
					effect(MobEffects.REGENERATION, REGEN_TICKS, REGEN_AMPLIFIER),
					effect(MobEffects.SATURATION, SATURATION_TICKS, 0))),
			// Raw scit + mushroom stew + almond water + onion. Milder and cheaper than the royal
			// version, so a shorter withdrawal with a lower cap.
			new Meal("dried_shrimp_mushroom_stew", 14, 0.8F, 3600, 2, effects -> List.of(
					effect(MobEffects.REGENERATION, REGEN_TICKS, REGEN_AMPLIFIER),
					effect(MobEffects.SATURATION, SATURATION_TICKS, 0))),
			// Carrot + blue almond water. The blue bottle is the one that sees in the dark, so the
			// stew grants night vision.
			new Meal("carrot_blue_almond_water_stew", 12, 0.6F, 3600, 2, effects -> List.of(
					effect(MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0))));

	/**
	 * Fried carpet: an edible repurposing of Level 0's flooring. Deliberately gives no beneficial
	 * effect - it is a greasy snack, not a meal - but it is still addictive like everything else
	 * made from these ingredients.
	 */
	private static final int FRIED_CARPET_NUTRITION = 4;
	private static final float FRIED_CARPET_SATURATION = 0.3F;
	private static final int FRIED_CARPET_WITHDRAWAL_TICKS = 1800;
	private static final int FRIED_CARPET_WITHDRAWAL_MAX_AMPLIFIER = 1;

	/** Set once registration has run, so a re-synced registry cannot register everything twice. */
	private static boolean registered;

	private ModItems() {
	}

	public static void initialize() {
		// Endless Backrooms registers its effects through Porting Lib's LazyRegistrar, so they may
		// not all be in the registry yet. Try straight away first.
		BackroomsEffects effects = BackroomsEffects.lookUp();

		if (effects != null) {
			registerItems(effects);
			return;
		}

		// Not there yet, so wait for the last one to appear. Note this callback only fires for
		// entries added *after* registration, which is why the direct lookup above is the normal
		// path, and why the lookup is retried on every effect that appears rather than only once.
		EndlessBackroomsDelight.LOGGER.warn(
				"{} is not registered yet; deferring item registration until it appears.", MOTH_PHEROMONE_ID);

		RegistryEntryAddedCallback.event(BuiltInRegistries.MOB_EFFECT).register((rawId, id, effect) -> {
			BackroomsEffects appeared = BackroomsEffects.lookUp();

			if (appeared != null) {
				registerItems(appeared);
			}
		});
	}

	/** Builds and registers every item, now that the effects they need exist. */
	private static void registerItems(BackroomsEffects effects) {
		if (registered) {
			return;
		}

		registered = true;

		List<Item> items = new ArrayList<>(MEALS.size() + 1);

		for (Meal meal : MEALS) {
			items.add(Registry.register(
					BuiltInRegistries.ITEM,
					EndlessBackroomsDelight.id(meal.path()),
					meal.create(effects)));
		}

		items.add(Registry.register(
				BuiltInRegistries.ITEM,
				EndlessBackroomsDelight.id("fried_carpet"),
				new AddictiveSnackItem(new Item.Properties()
						.food(new FoodProperties.Builder()
								.nutrition(FRIED_CARPET_NUTRITION)
								.saturationMod(FRIED_CARPET_SATURATION)
								.build())
						.stacksTo(64),
						FRIED_CARPET_WITHDRAWAL_TICKS,
						FRIED_CARPET_WITHDRAWAL_MAX_AMPLIFIER)));

		// Show them next to the other Farmer's Delight meals.
		ItemGroupEvents.modifyEntriesEvent(FARMERS_DELIGHT_TAB).register(entries -> items.forEach(entries::accept));

		items.forEach(item -> EndlessBackroomsDelight.LOGGER.info(
				"Registered {}", BuiltInRegistries.ITEM.getKey(item)));
	}

	private static MobEffectInstance effect(MobEffect effect, int durationTicks, int amplifier) {
		return new MobEffectInstance(effect, durationTicks, amplifier);
	}

	/**
	 * Definition of one bowl meal.
	 *
	 * @param path                    registry path, without the namespace
	 * @param nutrition               hunger points to restore
	 * @param saturationModifier      saturation gained is {@code nutrition * this * 2}
	 * @param withdrawalDurationTicks how long the withdrawal lasts after eating
	 * @param withdrawalMaxAmplifier  highest withdrawal amplifier it can build up to
	 * @param effects                 builds the effect list; meals that grant an Endless Backrooms
	 *                                effect read it from the argument
	 */
	private record Meal(
			String path,
			int nutrition,
			float saturationModifier,
			int withdrawalDurationTicks,
			int withdrawalMaxAmplifier,
			EffectsFactory effects) {

		Item create(BackroomsEffects backroomsEffects) {
			FoodProperties.Builder food = new FoodProperties.Builder()
					.nutrition(nutrition)
					.saturationMod(saturationModifier);

			for (MobEffectInstance effect : effects.of(backroomsEffects)) {
				food.effect(effect, EFFECT_CHANCE);
			}

			return new AddictiveBowlFoodItem(
					new Item.Properties()
							.food(food.build())
							.stacksTo(MEAL_STACK_SIZE)
							.craftRemainder(Items.BOWL),
					withdrawalDurationTicks,
					withdrawalMaxAmplifier);
		}
	}

	/** Builds a meal's effect list, with access to the Endless Backrooms effects it may need. */
	@FunctionalInterface
	private interface EffectsFactory {
		List<MobEffectInstance> of(BackroomsEffects effects);
	}

	/**
	 * The Endless Backrooms effects this mod grants. Resolved by resource location rather than by
	 * referencing that mod's {@code ModEffects} class, because it ships no sources and compiling
	 * against its internals would be fragile.
	 *
	 * @param mothPheromone makes deathmoths ignore the player
	 * @param wretchedCycle teleports the victim back and spawns a wretch every second
	 */
	private record BackroomsEffects(MobEffect mothPheromone, MobEffect wretchedCycle) {
		/**
		 * Reads both effects from the registry.
		 *
		 * @return the effects, or null if either is not registered yet
		 */
		static BackroomsEffects lookUp() {
			MobEffect mothPheromone = BuiltInRegistries.MOB_EFFECT.get(MOTH_PHEROMONE_ID);
			MobEffect wretchedCycle = BuiltInRegistries.MOB_EFFECT.get(WRETCHED_CYCLE_ID);

			if (mothPheromone == null || wretchedCycle == null) {
				return null;
			}

			return new BackroomsEffects(mothPheromone, wretchedCycle);
		}
	}
}
