package neo.livy.elbkrdelight.item;

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
 * <p>The stew is built lazily, once Endless Backrooms has actually put its moth pheromone effect
 * into the registry. That mod registers through Porting Lib's {@code LazyRegistrar}, which defers
 * the real registration, so its effects do not exist yet while this mod's initializer runs.
 * Building the item's food properties eagerly therefore fails at class-initialisation time; waiting
 * for the registry callback is what makes the pheromone effect resolvable.
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

	/** Chance the effects are applied, from 0.0 to 1.0. */
	private static final float EFFECT_CHANCE = 1.0F;

	/**
	 * Saturation, matching what royal rations and moth jelly each grant. 15 seconds is the duration
	 * moth jelly uses; royal rations uses a longer 120 seconds.
	 */
	private static final int SATURATION_DURATION_TICKS = 300;

	/**
	 * Moth pheromone, as granted by moth jelly. Endless Backrooms' deathmoths check this effect in
	 * {@code DeathmothEntity.shouldIgnoreTarget} and refuse to attack the player while it is active,
	 * so this is the "deathmoths leave you alone" buff, not a debuff.
	 */
	private static final int MOTH_PHEROMONE_DURATION_TICKS = 6000;

	private static final ResourceLocation MOTH_PHEROMONE_ID =
			new ResourceLocation("endless_backrooms", "moth_pheromone");

	private static final String ITEM_PATH = "royal_ration_stewed_moth_jelly";

	/** Farmer's Delight registers its creative tab under this id (see its {@code ModCreativeTabs}). */
	private static final ResourceKey<CreativeModeTab> FARMERS_DELIGHT_TAB = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB,
			new ResourceLocation("farmersdelight", "farmersdelight"));

	/** Null until Endless Backrooms has registered the moth pheromone effect. */
	private static Item royalRationStewedMothJelly;

	private ModItems() {
	}

	public static void initialize() {
		// Endless Backrooms registers its effects through Porting Lib's LazyRegistrar. By the time
		// this runs they are normally already in the registry, so try straight away.
		MobEffect mothPheromone = BuiltInRegistries.MOB_EFFECT.get(MOTH_PHEROMONE_ID);

		if (mothPheromone != null) {
			createStew(mothPheromone);
			return;
		}

		// Not there yet, so wait for it. Note this callback only fires for entries added *after*
		// registration, which is why the direct lookup above is the normal path.
		EndlessBackroomsDelight.LOGGER.warn(
				"{} is not registered yet; deferring item registration until it appears.", MOTH_PHEROMONE_ID);

		RegistryEntryAddedCallback.event(BuiltInRegistries.MOB_EFFECT).register((rawId, id, effect) -> {
			if (MOTH_PHEROMONE_ID.equals(id)) {
				createStew(effect);
			}
		});
	}

	/**
	 * Builds and registers the stew, now that every effect it needs exists.
	 *
	 * <p>Guarded against running twice, because a registry can be re-synced and fire the callback
	 * again.
	 */
	private static void createStew(MobEffect mothPheromone) {
		if (royalRationStewedMothJelly != null) {
			return;
		}

		royalRationStewedMothJelly = Registry.register(
				BuiltInRegistries.ITEM,
				EndlessBackroomsDelight.id(ITEM_PATH),
				new RoyalRationStewedMothJellyItem(new Item.Properties()
						.food(new FoodProperties.Builder()
								.nutrition(NUTRITION)
								.saturationMod(SATURATION_MODIFIER)
								.effect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER), EFFECT_CHANCE)
								.effect(new MobEffectInstance(MobEffects.SATURATION, SATURATION_DURATION_TICKS, 0), EFFECT_CHANCE)
								.effect(new MobEffectInstance(mothPheromone, MOTH_PHEROMONE_DURATION_TICKS, 0), EFFECT_CHANCE)
								.build())
						.stacksTo(16)
						.craftRemainder(Items.BOWL)));

		// Show it next to the other Farmer's Delight meals.
		ItemGroupEvents.modifyEntriesEvent(FARMERS_DELIGHT_TAB)
				.register(entries -> entries.accept(royalRationStewedMothJelly));

		EndlessBackroomsDelight.LOGGER.info("Registered {}", EndlessBackroomsDelight.id(ITEM_PATH));
	}
}
