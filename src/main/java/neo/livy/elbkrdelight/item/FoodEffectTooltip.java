package neo.livy.elbkrdelight.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

/**
 * Renders a food item's potion effects into its tooltip, the way Farmer's Delight does for its
 * meals.
 *
 * <p>Vanilla only shows these lines for a few hard-coded items, so modded food has to add them
 * itself.
 */
public final class FoodEffectTooltip {
	private FoodEffectTooltip() {
	}

	/**
	 * Appends one line per food effect, in the format {@code "Effect Name II (1:30)"}, in blue like
	 * the vanilla potion tooltip.
	 *
	 * @param stack   the stack being described; its food properties supply the effects
	 * @param tooltip the list to append to
	 */
	public static void append(ItemStack stack, List<Component> tooltip) {
		// 1.20.1 has only the no-argument accessor; the ItemStack overload arrives with the food
		// component rework in 1.20.5.
		var food = stack.getItem().getFoodProperties();

		if (food == null) {
			return;
		}

		for (var pair : food.getEffects()) {
			MobEffectInstance effect = pair.getFirst();

			if (effect == null) {
				continue;
			}

			tooltip.add(describe(effect).withStyle(ChatFormatting.BLUE));
		}
	}

	/** Builds one line: the effect's name, its roman-numeral level, and its duration. */
	private static MutableComponent describe(MobEffectInstance effect) {
		MutableComponent line = Component.translatable(effect.getDescriptionId());

		if (effect.getAmplifier() > 0) {
			// Vanilla's own effect tooltip uses these keys, e.g. "enchantment.level.2" is "II".
			line.append(CommonComponents.SPACE)
					.append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
		}

		if (!effect.isInfiniteDuration()) {
			line.append(CommonComponents.SPACE)
					.append(Component.literal("(" + formatDuration(effect.getDuration()) + ")"));
		}

		return line;
	}

	/** Formats ticks as {@code m:ss}, or {@code h:mm:ss} past an hour. */
	private static String formatDuration(int ticks) {
		int totalSeconds = ticks / 20;
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;

		if (hours > 0) {
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		}

		return String.format("%d:%02d", minutes, seconds);
	}
}
