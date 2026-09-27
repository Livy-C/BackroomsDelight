package neo.livy.elbkrdelight.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A bowl food that leaves an empty bowl behind and is addictive, matching Endless Backrooms' own
 * royal rations and moth jelly.
 *
 * <p>Extends plain {@link Item} rather than Farmer's Delight's {@code ConsumableItem}: the bowl
 * comes back through {@code craftRemainder(Items.BOWL)}, which vanilla already honours in
 * {@code LivingEntity.eat} and therefore in {@link Item#finishUsingItem}, so this avoids depending
 * on Farmer's Delight's config for tooltip behaviour.
 */
public class AddictiveBowlFoodItem extends Item {
	/** How long the withdrawal lasts after eating, in ticks. */
	private final int withdrawalDurationTicks;

	/** Highest withdrawal amplifier this item can build up to. */
	private final int withdrawalMaxAmplifier;

	/** Whether to render the food effects in the tooltip, Farmer's Delight style. */
	private final boolean showFoodEffectsInTooltip;

	public AddictiveBowlFoodItem(Properties properties, int withdrawalDurationTicks, int withdrawalMaxAmplifier) {
		this(properties, withdrawalDurationTicks, withdrawalMaxAmplifier, true);
	}

	public AddictiveBowlFoodItem(Properties properties, int withdrawalDurationTicks, int withdrawalMaxAmplifier,
			boolean showFoodEffectsInTooltip) {
		super(properties);
		this.withdrawalDurationTicks = withdrawalDurationTicks;
		this.withdrawalMaxAmplifier = withdrawalMaxAmplifier;
		this.showFoodEffectsInTooltip = showFoodEffectsInTooltip;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		// Applies the FoodProperties effects and returns the empty bowl through the recipe
		// remainder. Must run first: the food effects are applied by LivingEntity.eat, which only
		// the superclass call reaches.
		ItemStack result = super.finishUsingItem(stack, level, entity);

		if (!level.isClientSide) {
			Withdrawal.apply(entity, withdrawalDurationTicks, withdrawalMaxAmplifier);
		}

		return result;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);

		if (showFoodEffectsInTooltip) {
			FoodEffectTooltip.append(stack, tooltip);
		}
	}
}
