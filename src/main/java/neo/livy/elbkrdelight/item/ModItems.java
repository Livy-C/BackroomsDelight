package neo.livy.elbkrdelight.item;

import neo.livy.elbkrdelight.EndlessBackroomsDelight;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import vectorwing.farmersdelight.common.item.ConsumableItem;

/**
 * Registers this mod's items.
 *
 * <p>The stew extends Farmer's Delight's {@code ConsumableItem}, the base class of every bowl food
 * in that mod. That class hands the bowl back by reading the recipe remainder of the stack, so
 * {@code craftRemainder(Items.BOWL)} is what makes the bowl survive being eaten. Reusing it keeps
 * the eating animation, use duration and tooltip behaviour identical to the Farmer's Delight soups.
 */
public final class ModItems {
	public static final Item ROYAL_RATION_STEWED_MOTH_JELLY;

	static {
		// 1.20.1 Item.Properties is consumed by the Item constructor and cannot be applied
		// afterwards, so the properties are built here and passed straight in.
		ROYAL_RATION_STEWED_MOTH_JELLY = new ConsumableItem(new Item.Properties()
				.food(new FoodProperties.Builder()
						.nutrition(10)
						.saturationMod(0.9F)
						.build())
				.stacksTo(16)
				.craftRemainder(Items.BOWL));
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
